package com.mk.wizardduel.services;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Choreographer;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.ViewModelProvider;

import com.mk.wizardduel.Game;
import com.mk.wizardduel.GameAttributes;
import com.mk.wizardduel.Player;
import com.mk.wizardduel.activities.GameActivity;
import com.mk.wizardduel.gameobjects.Boundary;
import com.mk.wizardduel.gameobjects.Fireball;
import com.mk.wizardduel.gameobjects.Shield;
import com.mk.wizardduel.gameobjects.Wizard;
import com.mk.wizardduel.utils.AnimHandler;
import com.mk.wizardduel.GameInputManager;
import com.mk.wizardduel.utils.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;

public class GameService extends LifecycleService implements Choreographer.FrameCallback
{
	public class GameBinder extends Binder
	{
		public GameService getService() { return GameService.this; }
	}

	public class GameThread extends Thread
	{
		private double mDeltaTime = 0;
		private boolean mShouldStop = false;
		private boolean mPaused = false;
		//private long mDebugFrameCount = 0;

		public GameThread()
		{
			super();
			setName("Game Thread");
		}

		@Override
		public void run()
		{
			while (!mShouldStop)
			{
				if (!mPaused && mDeltaTime != 0)
				{
					mGame.update(mDeltaTime);
					mDeltaTime = 0;
				}
			}
		}

		public void frameTick(double deltaTime)
		{
			if (!isAlive() || mShouldStop)
				return;

			mDeltaTime = deltaTime;
				// Game loop reads mDeltaTime and executes accordingly when != 0.
		}

		public void terminate() { mShouldStop = true; }
		public void setPaused(boolean paused) { mPaused = paused; }
	}

	private class ShieldInfo
	{
		private final Shield mShield;

		// If I ever add a 4 player mode, these would have to also be made into arrays.
		private int mPointerId1;
		private int mPointerId2;

		private final Vector2D mPoint1 = new Vector2D();
		private final Vector2D mPoint2 = new Vector2D();

		boolean mPoint1Updated = false;
		boolean mPoint2Updated = false;

		public ShieldInfo(Shield shield) { mShield = shield; }
		public void activate(int id1, int id2, Vector2D point1, Vector2D point2)
		{
			if (isShieldActive())
				Log.w("GameService.ShieldInfo", "Activating an already active shield.");

			mPointerId1 = id1;
			mPointerId2 = id2;

			mShield.cast(point1, point2);
		}

		public void updatePoint(int id, Vector2D point)
		{
			if (!isShieldActive())
				return;

			if (id == mPointerId1)
			{
				mPoint1.set(point);
				mPoint1Updated = true;
			}
			else if (id == mPointerId2)
			{
				mPoint2.set(point);
				mPoint2Updated = true;
			}

			if (mPoint1Updated && mPoint2Updated)
			{
				// TODO noting an issue here where sometimes it thinks the a shield's point is in neutral zone when it's not.
				// First going to do interping to casting area border, then investigate this.

				Wizard caster = mShield.getCaster();

				Wizard point1CastingArea = getCastingAreaOwner((int)mPoint1.x);
				Wizard point2CastingArea = getCastingAreaOwner((int)mPoint2.x);

				// Check that both points are still within the right casting area (and also not outside either)
				if (point1CastingArea != caster || point2CastingArea != caster)
				{
					// If both are out, just stop the spell.
					if (point1CastingArea != caster && point2CastingArea != caster)
					{
						mShield.stopCasting(); // TODO find bug which makes the shield not show ever again
					}
					else
					{
						// Interpolate the point on the shield line that is at the casting border and
						// use that instead to clip the shield at the border.
						Vector2D inside = (point1CastingArea == caster) ? mPoint1 : mPoint2;
						Vector2D outside = (point1CastingArea != caster) ? mPoint1 : mPoint2;
						float spanX = Math.abs(outside.x - inside.x);
						float spanY = Math.abs(outside.y - inside.y);

						float castBorderX = (inside.x < outside.x)
								? mGameAttributes.castingAreaWidth
								: mViewBounds.width() - mGameAttributes.castingAreaWidth;

						float inCastingAreaSpanX = castBorderX - inside.x;
						float inFraction = inCastingAreaSpanX / spanX;
						float yMulti = (inside.y < outside.y) ? 1f : -1f;
						yMulti *= (inside.x < outside.x) ? 1f : -1f;

						float newX = inside.x + inCastingAreaSpanX; // x works
						float newY = inside.y + spanY * inFraction * yMulti;

						outside.set(newX, newY);
					}
					return;
				}

				mShield.updatePosition(mPoint1, mPoint2);
				mPoint1Updated = false;
				mPoint2Updated = false;
			}
		}

		public void deactivate()
		{
			if (!isShieldActive())
				return;

			mShield.stopCasting();

			mPoint1Updated = false;
			mPoint2Updated = false;
		}

		public Shield getShield() { return mShield; }
		public boolean isShieldActive() { return mShield.isActive(); }
		public int getPointerId1() { return mPointerId1; }
		public int getPointerId2() { return mPointerId2; }

		public boolean usesPointer(int pointerId)
		{
			if (!isShieldActive())
				return false;

			return pointerId == mPointerId1 || pointerId == mPointerId2;
		}
	}

	private GameActivity mActivity;
	private final GameThread mGameThread = new GameThread();
	private final IBinder mBinder = new GameBinder();
	private Runnable mGameTickCallback = null;
	private double mPreviousFrameTime = 0;
	private Game mGame;
	private boolean mInitialised = false; // TODO use properly
	private AnimHandler mAnimHandler;
	private ArrayList<AnimationDrawable> mCachedAllAnims;
	final private Rect mViewBounds = new Rect();
	private GameAttributes mGameAttributes = null;
	private GameInputManager mGameInputManager;
	private final Wizard[] mWizards = new Wizard[2];
	private final HashMap<Wizard, ShieldInfo> mShields = new HashMap<>();
	private int mFireballHeight = 0;

	/** Dictionary of fireballs that are being cast (pointers down) keyed by pointer ID. */
	private final HashMap<Integer, Fireball> mUnreleasedFireballs = new HashMap<>();

	public GameService()
	{
		Log.i("DEBUG", "GameService constructor called.");
	}

	public GameInputManager getGameInputHandler() { return mGameInputManager; }

	public void setGameTickCallback(Runnable mGameTickCallback)
	{
		this.mGameTickCallback = mGameTickCallback;
	}

	// public void setBounds(Rect bounds) { mViewBounds.set(bounds); }

	public void draw(Canvas canvas) { mGame.draw(canvas); }

	/** Must be called by activity which is launching this service, passing itself.
	 * @param boundActivity The activity binding this service to it (and likely calling this method).*/
	public void bind(GameActivity boundActivity)
	{
		mActivity = boundActivity;
		mGame = new ViewModelProvider(boundActivity).get(Game.class);
	}

	/** Called from <code>GameView</code> or its activity, supplying <code>GameAttributes</code>.
	 * @param gameAttrs <code>GameAttributes</code> object populated with attributes for the game. */
	public void init(GameAttributes gameAttrs)
	{
		if (mGame == null)
		{
			Log.w("GameService", "GameService has to be bound using bind() before it can be initialised.");
			return;
		}

		mGameAttributes = gameAttrs;

		// Set View Bounds
		mViewBounds.set(mGameAttributes.viewBounds);

		createWizards();
		createShields();
		createBoundaries();

		// Unpack necessary attributes
		float fireballRelativeHeight = mGameAttributes.fireballRelativeHeight;
		mFireballHeight = (int) ((fireballRelativeHeight != GameAttributes.NOT_SET) ? (mViewBounds.height() * fireballRelativeHeight) : (mWizards[0].getHeight() * 0.3f));

		// Start game thread and choreographer time sync
		Choreographer.getInstance().postFrameCallback(this);
		mGameThread.start();

		// Start various components
		mAnimHandler = new AnimHandler(getLifecycle(), true); // TODO anim list doesn't update: fix
		getLifecycle().addObserver(mAnimHandler);
		mCachedAllAnims = new ArrayList<>(mGame.getAllAnims());
		handleAnims(mCachedAllAnims);

		mGameInputManager = new GameInputManager(this, ViewConfiguration.get(this));

		mInitialised = true;
	}

	@Override
	public void doFrame(long frameTimeNanos)
	{
		double deltaTime = ((double)frameTimeNanos - mPreviousFrameTime) / 1000000000.0;

		// Skip frames if there's too much of a delay.
		if (deltaTime <= 0.1)
		{
			updateAnimHandler();
			mGameThread.frameTick(deltaTime);
			mGameTickCallback.run();
		}
		else
		{
			Log.i("DEBUG", "GameService.doFrame() skipped a frame!");
		}

		mPreviousFrameTime = frameTimeNanos;
		Choreographer.getInstance().postFrameCallback(this);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i("DEBUG", "GameService.onCreate() called.");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		mWizards[0].setStatusListener(null);
		mWizards[1].setStatusListener(null);

		mGameThread.terminate();
		Log.i("DEBUG", "GameService.onDestroy() called.");
	}

	@Nullable
	@Override
	public IBinder onBind(@NonNull Intent intent)
	{
		super.onBind(intent);
		mGameThread.setPaused(false);
		Log.i("DEBUG", "GameService.onBind() called.");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		super.onUnbind(intent);
		Log.i("DEBUG", "GameService.onUnbind() called.");
		mGameThread.setPaused(true);
		return false;
	}

	public GameAttributes getGameAttributes() { return mGameAttributes; }

	private void updateAnimHandler()
	{
		ArrayList<AnimationDrawable> currAllAnims = mGame.getAllAnims();

		ArrayList<AnimationDrawable> animsToRemove = new ArrayList<>(mCachedAllAnims);
		animsToRemove.removeAll(currAllAnims);

		ArrayList<AnimationDrawable> animsToAdd = new ArrayList<>(currAllAnims);
		animsToAdd.removeAll(mCachedAllAnims);

		if (!animsToRemove.isEmpty())
		{
			mAnimHandler.removeAnims(animsToRemove);
			mCachedAllAnims.removeAll(animsToRemove);
		}
		if (!animsToAdd.isEmpty())
		{
			handleAnims(animsToAdd);
			mCachedAllAnims.addAll(animsToAdd);
		}
	}

	private void handleAnims(@NonNull ArrayList<AnimationDrawable> anims)
	{
		for (AnimationDrawable anim : anims)
		{
			anim.setCallback(mGameAttributes.gameView);
			anim.setVisible(true, false);
		}

		mAnimHandler.addAnims(anims);
	}

	private void createWizards()
	{
		if (!mGame.hasStarted())
		{
			mWizards[0] = new Wizard(mGameAttributes, Player.PLAYER_ONE);
			mWizards[1] = new Wizard(mGameAttributes, Player.PLAYER_TWO);

			// Set Wizard sizes and positions
			int scaledHeight1 = (int)(mViewBounds.height() * (mGameAttributes.wizard1RelativeBounds.bottom - mGameAttributes.wizard1RelativeBounds.top));
			mWizards[0].setHeight(scaledHeight1, true);

			int scaledHeight2 = (int)(mViewBounds.height() * (mGameAttributes.wizard2RelativeBounds.bottom - mGameAttributes.wizard2RelativeBounds.top));
			mWizards[1].setHeight(scaledHeight2, true);

			mWizards[0].setPos((int)(mViewBounds.width() * mGameAttributes.wizard1RelativeBounds.left),  (int)(mViewBounds.height() * mGameAttributes.wizard1RelativeBounds.top));
			mWizards[1].setPos((int)(mViewBounds.width() * mGameAttributes.wizard2RelativeBounds.right), (int)(mViewBounds.height() * mGameAttributes.wizard2RelativeBounds.top));

			mWizards[1].setAnchor(1.f, 0.f);
			mWizards[1].rotation = 180.f;

			mWizards[0].setTint(mGameAttributes.player1Colour);
			mWizards[1].setTint(mGameAttributes.player2Colour);

			mWizards[0].setStatusListener(mActivity);
			mWizards[1].setStatusListener(mActivity);

			mGame.addObject(mWizards[0]);
			mGame.addObject(mWizards[1]);

			mGame.gameData.registerWizards(mWizards[0], mWizards[1]);
		}
		else
		{
			mWizards[0] = mGame.gameData.getWizard1();
			mWizards[1] = mGame.gameData.getWizard2();

			mWizards[0].setStatusListener(mActivity);
			mWizards[1].setStatusListener(mActivity);
		}
	}

	private void createShields()
	{
		if (!mGame.hasStarted())
		{
			Shield shield1 = new Shield(mWizards[0]);
			Shield shield2 = new Shield(mWizards[1]);

			mGame.addObject(shield1);
			mGame.addObject(shield2);

			ShieldInfo shieldInfo1 = new ShieldInfo(shield1);
			ShieldInfo shieldInfo2 = new ShieldInfo(shield2);

			mShields.put(mWizards[0], shieldInfo1);
			mShields.put(mWizards[1], shieldInfo2);

			mGame.gameData.registerShields(shield1, shield2);
		}
		else
		{
			Shield shield1 = mGame.gameData.getShield1();
			Shield shield2 = mGame.gameData.getShield2();

			ShieldInfo shieldInfo1 = new ShieldInfo(shield1);
			ShieldInfo shieldInfo2 = new ShieldInfo(shield2);

			mShields.put(mWizards[0], shieldInfo1);
			mShields.put(mWizards[1], shieldInfo2);
		}
	}

	private void createBoundaries()
	{
		Rect leftBoundary = new Rect(-1, 0, 0, mViewBounds.bottom);
		Rect topBoundary = new Rect(0, -1, mViewBounds.right, 0);
		Rect rightBoundary = new Rect(mViewBounds.right, 0, mViewBounds.right + 1, mViewBounds.bottom);
		Rect bottomBoundary = new Rect(0, mViewBounds.bottom, mViewBounds.right, mViewBounds.bottom + 1);

		Vector2D faceLeft = new Vector2D(1, 0);
		Vector2D faceTop = new Vector2D(0, 1);
		Vector2D faceRight = new Vector2D(-1, 0);
		Vector2D faceBottom = new Vector2D(0, -1);

		Boundary left = new Boundary(leftBoundary, faceLeft, false);
		Boundary top = new Boundary(topBoundary, faceTop, true);
		Boundary right = new Boundary(rightBoundary, faceRight, false);
		Boundary bottom = new Boundary(bottomBoundary, faceBottom, true);

		mGame.addObject(left);
		mGame.addObject(top);
		mGame.addObject(right);
		mGame.addObject(bottom);
	}

	public void castFireball(Vector2D position, int id)
	{
		if (mUnreleasedFireballs.containsKey(id))
		{
			Log.e("DEBUG", "Attempted to cast fireball, but id is already associated with one.");
			return;
		}

		Wizard caster = getCastingAreaOwner((int)position.x);
		if (caster == null)
			return;

		Fireball fireball = Fireball.obtain();
		fireball.init(caster, position);
		fireball.setAnchor(0.5f, 0.5f);
		fireball.setHeight(mFireballHeight, true);

		mUnreleasedFireballs.put(id, fireball);
		mGame.addObject(fireball);
	}

	public void moveSpell(int id, Vector2D pos)
	{
		for (ShieldInfo si : mShields.values()	)
			si.updatePoint(id, pos);

		Fireball fireball = mUnreleasedFireballs.get(id);
		if (fireball == null)
			return;

		// Test if fireball hasn't left the correct caster's area
		Wizard caster = fireball.getCaster();
		Wizard areaOwner = getCastingAreaOwner((int)pos.x);

		if (caster != areaOwner)
		{
			// Limit x movement to the border.
			float castingAreaBorderX = (caster.getPlayer() == Player.PLAYER_ONE)
					? mGameAttributes.castingAreaWidth
					: mViewBounds.width() - mGameAttributes.castingAreaWidth;

			pos.setX(castingAreaBorderX);
		}

		fireball.setPos(pos);
	}

	public void cancelSpell(int id)
	{
		Fireball fireball = mUnreleasedFireballs.remove(id);
		if (fireball != null)
		{
			fireball.destroy();
			return;
		}

		for (ShieldInfo si : mShields.values())
		{
			if (si.usesPointer(id))
				si.deactivate();
		}
	}

	public void cancelAllSpells()
	{
		for (Fireball fireball : mUnreleasedFireballs.values())
			fireball.destroy();

		mUnreleasedFireballs.clear();

		for (ShieldInfo si : mShields.values()	)
			si.deactivate();
	}

	public void releaseSpell(int id, Vector2D direction)
	{
		for (ShieldInfo si : mShields.values())
		{
			if (si.usesPointer(id))
			{
				si.deactivate();
				return;
			}
		}

		Fireball fireball = mUnreleasedFireballs.get(id);
		if (fireball == null)
			return;

		// If it's not active, it's not in the correct casting zone.
		if (!fireball.isActive())
		{
			cancelSpell(id);
			return;
		}

		int speed = mGameAttributes.fireballSpeedPx != GameAttributes.NOT_SET
				? mGameAttributes.fireballSpeedPx
				: mGameAttributes.fireballSpeedDp; // only a backup, Px should always be set.

		fireball.release(direction, speed);
		mUnreleasedFireballs.remove(id);
	}

	/** Returns true if shield was cast, or false if it wasn't. */
	public boolean tryCastShield(int pointerId1, int pointerId2, Vector2D point1, Vector2D point2)
	{
		// If these pointers are already used to cast fireballs, don't let them cast shield.
		if (mUnreleasedFireballs.containsKey(pointerId1) || mUnreleasedFireballs.containsKey(pointerId2))
			return false;

		Wizard caster = getCastingAreaOwner((int)point1.x);

		// Check that both points are within the same casting area (and also not outside either)
		if (caster == null || caster != getCastingAreaOwner((int)point2.x))
			return false;

		Log.i("DEBUG:Touch", "SHIELD Cast! points: " + pointerId1 + " and " + pointerId2);
		ShieldInfo shieldInfo = mShields.get(caster);
		if (shieldInfo == null)
		{
			Log.e("DEBUG", "GameService.tryCastShield(): Shields have not been constructed properly.");
			return false;
		}

		shieldInfo.activate(pointerId1, pointerId2, point1, point2);

		return true;
	}

	/**
	 * Gets the owning Wizard of the casting area where the x falls under, or null if the area is neutral.
	 * @param x X coordinate to check
	 * @return <code>Wizard</code> reference to the Wizard associated with the area where x falls under, or null if it's a neutral area.
	 */
	private Wizard getCastingAreaOwner(int x)
	{
		Wizard caster = null;
		if (x <= mGameAttributes.castingAreaWidth)
			caster = mWizards[0];
		else if (x >= mGameAttributes.viewBounds.width() - mGameAttributes.castingAreaWidth)
			caster = mWizards[1];

		return caster;
	}
}