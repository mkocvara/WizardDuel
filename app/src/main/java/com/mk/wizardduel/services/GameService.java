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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.ViewModelProvider;

import com.mk.wizardduel.Game;
import com.mk.wizardduel.GameAttributes;
import com.mk.wizardduel.gameobjects.Wizard;
import com.mk.wizardduel.utils.AnimHandler;

import java.util.ArrayList;

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

	public GameService()
	{
		Log.i("DEBUG", "GameService constructor called.");
	}

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
		mGameAttributes = gameAttrs;

		if (mGame == null)
		{
			Log.w("GameService", "GameService has to be bound using bind() before it can be initialised.");
			return;
		}

		mViewBounds.set(gameAttrs.viewBounds);

		// Only create wizards the first time this service is stated to avoid duplicating them
		// every time activity is recreated.
		if (!mGame.hasStarted())
			createWizards(gameAttrs);

		mAnimHandler = new AnimHandler(getLifecycle(), true);
		getLifecycle().addObserver(mAnimHandler);
		cachedAllAnims = mGame.getAllAnims();
		handleAnims(cachedAllAnims);

		// Start game thread and choreographer time sync
		Choreographer.getInstance().postFrameCallback(this);
		mGameThread.start();

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


	private void createWizards(GameAttributes gameAttrs)
	{
		Wizard wizard1 = new Wizard();
		Wizard wizard2 = new Wizard();

		// Set Wizard sizes and positions
		int scaledHeight1 = (int)(mViewBounds.height() * (gameAttrs.wizard1RelativeBounds.bottom - gameAttrs.wizard1RelativeBounds.top));
		wizard1.setHeight(scaledHeight1, true);

		int scaledHeight2 = (int)(mViewBounds.height() * (gameAttrs.wizard2RelativeBounds.bottom - gameAttrs.wizard2RelativeBounds.top));
		wizard2.setHeight(scaledHeight2, true);

		wizard1.pos.set((int)(mViewBounds.width() * gameAttrs.wizard1RelativeBounds.left),  (int)(mViewBounds.height() * gameAttrs.wizard1RelativeBounds.top));
		wizard2.pos.set((int)(mViewBounds.width() * gameAttrs.wizard2RelativeBounds.right), (int)(mViewBounds.height() * gameAttrs.wizard2RelativeBounds.top));

		wizard2.anchor.set(0.f, 1.f);
		wizard2.rotation = 180.f;

		wizard1.setTint(Color.BLUE);
		wizard2.setTint(Color.RED);

		mGame.addObject(wizard1);
		mGame.addObject(wizard2);
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

	private void handleAnims(ArrayList<AnimationDrawable> anims)
	{
		for (AnimationDrawable anim : anims)
		{
			anim.setCallback(mGameAttributes.gameView);
			anim.setVisible(true, false);
		}

		mAnimHandler.addAnims(anims);
	}
}