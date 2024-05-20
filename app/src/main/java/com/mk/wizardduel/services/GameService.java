package com.mk.wizardduel.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Choreographer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.mk.wizardduel.Game;
import com.mk.wizardduel.gameobjects.GameObject;

public class GameService extends Service implements Choreographer.FrameCallback
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
		private long mDebugFrameCount = 0;

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
			Log.i("DEBUG", "GameThread.frameTick() called; Frame " + ++mDebugFrameCount + "; deltaTime == " + deltaTime);
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

	public GameService()
	{
		Log.i("DEBUG", "GameService constructor called.");
	}

	public void setGameTickCallback(Runnable mGameTickCallback)
	{
		this.mGameTickCallback = mGameTickCallback;
	}

	public void addObject(GameObject object) { mGame.addObject(object); }
	public void draw(Canvas canvas) { mGame.draw(canvas); }

	/** Must be called by activity which is launching this service. */
	public void init(AppCompatActivity boundActivity)
	{
		mGame = new ViewModelProvider(boundActivity).get(Game.class);
		mInitialised = true;

		Log.i("DEBUG", "GameService.init() called; mGame.getNumObjects() == " + mGame.getNumObjects());

		// Start game thread and choreographer sync
		Choreographer.getInstance().postFrameCallback(this);
		mGameThread.start();
	}

	@Override
	public void doFrame(long frameTimeNanos)
	{
		double deltaTime = ((double)frameTimeNanos - mPreviousFrameTime) / 1000000000.0;

		// Skip frames if there's too much of a delay.
		if (deltaTime <= 0.1)
		{
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
		Log.i("DEBUG", "GameService.onBind() called.");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		// TODO stop game thread?

		Log.i("DEBUG", "GameService.onUnbind() called.");
		return false;
	}
}