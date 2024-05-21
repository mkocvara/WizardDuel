package com.mk.wizardduel.services;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Choreographer;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.ViewModelProvider;

import com.mk.wizardduel.Game;
import com.mk.wizardduel.GameAttributes;
import com.mk.wizardduel.gameobjects.Fireball;
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
			// Log.i("DEBUG", "GameThread.frameTick() called; Frame " + ++mDebugFrameCount + "; deltaTime == " + deltaTime);
		}

		public void terminate() { mShouldStop = true; }
		public void setPaused(boolean paused) { mPaused = paused; }
	}

	private final GameThread mGameThread = new GameThread();
	private final IBinder mBinder = new GameBinder();
	private Runnable mGameTickCallback = null;
	private double mPreviousFrameTime = 0;
	private Game mGame;
	private boolean mInitialised = false; // TODO use properly
	private AnimHandler mAnimHandler;
	private ArrayList<AnimationDrawable> cachedAllAnims;
	final private Rect mViewBounds = new Rect();
	private GameAttributes mGameAttributes = null;
	private GameInputManager mGameInputManager;
	private Wizard mWizard1, mWizard2;
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
	public void bind(AppCompatActivity boundActivity)
	{
		mGame = new ViewModelProvider(boundActivity).get(Game.class);

		Log.i("DEBUG", "GameService.init() called; mGame.getNumObjects() == " + mGame.getNumObjects());
	}

	/** Called from <code>GameView</code> or its activity to supply <code>GameAttributes</code>.
	 * @param gameAttrs <code>GameAttributes</code> object populated with attributes for the game. */
	public void init(GameAttributes gameAttrs)
	{
		if (mGame == null)
		{
			Log.w("GameService", "GameService has to be bound using bind() before it can be initialised.");
			return;
		}

		// Set View Bounds -- must happen at the top
		mViewBounds.set(gameAttrs.viewBounds);

		// Only create wizards the first time this service is started to avoid duplicating them
		// every time activity is recreated.
		if (!mGame.hasStarted())
			createWizards(gameAttrs);

		// Unpack necessary attributes
		mGameAttributes = gameAttrs;
		float fireballRelativeHeight = mGameAttributes.fireballRelativeHeight;
		mFireballHeight = (int) ((fireballRelativeHeight != GameAttributes.NOT_SET) ? (mViewBounds.height() * fireballRelativeHeight) : (mWizard1.getHeight() * 0.3f));

		// Start game thread and choreographer time sync
		Choreographer.getInstance().postFrameCallback(this);
		mGameThread.start();

		// Start various components
		mAnimHandler = new AnimHandler(getLifecycle(), true); // TODO anim list doesn't update: fix
		getLifecycle().addObserver(mAnimHandler);
		cachedAllAnims = mGame.getAllAnims();
		handleAnims(cachedAllAnims);

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

	private void updateAnimHandler()
	{
		ArrayList<AnimationDrawable> currAllAnims = mGame.getAllAnims();

		ArrayList<AnimationDrawable> animsToRemove = new ArrayList<>(cachedAllAnims);
		animsToRemove.removeAll(currAllAnims);

		ArrayList<AnimationDrawable> animsToAdd = new ArrayList<>(currAllAnims);
		animsToAdd.removeAll(cachedAllAnims);

		if (!animsToRemove.isEmpty())
			mAnimHandler.removeAnims(animsToRemove);
		if (!animsToAdd.isEmpty())
			handleAnims(animsToAdd);
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

	private void createWizards(@NonNull GameAttributes gameAttrs)
	{
		mWizard1 = new Wizard();
		mWizard2 = new Wizard();

		// Set Wizard sizes and positions
		int scaledHeight1 = (int)(mViewBounds.height() * (gameAttrs.wizard1RelativeBounds.bottom - gameAttrs.wizard1RelativeBounds.top));
		mWizard1.setHeight(scaledHeight1, true);

		int scaledHeight2 = (int)(mViewBounds.height() * (gameAttrs.wizard2RelativeBounds.bottom - gameAttrs.wizard2RelativeBounds.top));
		mWizard2.setHeight(scaledHeight2, true);

		mWizard1.setPos((int)(mViewBounds.width() * gameAttrs.wizard1RelativeBounds.left),  (int)(mViewBounds.height() * gameAttrs.wizard1RelativeBounds.top));
		mWizard2.setPos((int)(mViewBounds.width() * gameAttrs.wizard2RelativeBounds.right), (int)(mViewBounds.height() * gameAttrs.wizard2RelativeBounds.top));

		mWizard2.setAnchor(1.f, 0.f);
		mWizard2.rotation = 180.f;

		mWizard1.setTint(Color.BLUE);
		mWizard2.setTint(Color.RED);

		mGame.addObject(mWizard1);
		mGame.addObject(mWizard2);
	}

	public void castFireball(Vector2D position, int id)
	{
		if (mUnreleasedFireballs.containsKey(id))
		{
			Log.e("DEBUG", "Attempted to cast fireball, but id is already associated with one.");
			return;
		}

		// TODO figure out who cast it based on pos
		Wizard caster;
		caster = mWizard1;

		Fireball fireball = Fireball.obtain();
		fireball.init(caster, position);
		fireball.setAnchor(0.5f, 0.5f);
		fireball.setHeight(mFireballHeight, true);

		mUnreleasedFireballs.put(id, fireball);
		mGame.addObject(fireball);
	}

	public void moveFireball(int id, float x, float y)
	{
		Fireball fireball = mUnreleasedFireballs.get(id);
		if (fireball != null)
			fireball.setPos(x, y);
	}

	public void cancelFireball(int id)
	{
		Fireball fireball = mUnreleasedFireballs.remove(id);
		if (fireball != null)
		{
			fireball.recycle();
		}
	}

	public void cancelAllFireballs()
	{
		for (Fireball fireball : mUnreleasedFireballs.values())
			fireball.recycle();

		mUnreleasedFireballs.clear();
	}

	public void releaseFireball(int id, Vector2D direction)
	{
		Fireball fireball = mUnreleasedFireballs.get(id);
		if (fireball == null)
			return;

		int speed = mGameAttributes.fireballSpeedPx != GameAttributes.NOT_SET
				? mGameAttributes.fireballSpeedPx
				: mGameAttributes.fireballSpeedDp; // only a backup, Px should always be set.

		fireball.release(direction, speed);
		mUnreleasedFireballs.remove(id);
	}

	/* TODO
	private void castShield(Wizard caster)
	{
		/* TODO
		* changing width
		* changing rot and pos
	 	* persists until touch stops
	 	*//*

		mGame.addObject(shield);
	}
	 */
}