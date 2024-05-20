package com.mk.wizardduel.services;

import static java.lang.Thread.sleep;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.mk.wizardduel.Game;
import com.mk.wizardduel.gameobjects.GameObject;

public class GameService extends Service
{

	public class GameBinder extends Binder
	{
		public GameService getService() { return GameService.this; }
	}

	private final IBinder mBinder = new GameBinder();
	private Runnable mGameTickCallback = null;
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
		startGameThread(); // TODO temp
	}

	private void startGameThread() // TODO temp
	{
		Runnable updateLoop = new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						sleep(100);
						mGame.update();
						if (mGameTickCallback != null)
							mGameTickCallback.run();
					} catch (InterruptedException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		};

		Thread updateThread = new Thread(updateLoop);
		updateThread.start();
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

		// TODO clean up game thread?
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