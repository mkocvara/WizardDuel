package com.mk.wizardduel.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.mk.wizardduel.services.GameService;
import com.mk.wizardduel.R;
import com.mk.wizardduel.views.GameView;

import java.util.function.Consumer;

public class GameActivity extends AppCompatActivity
{
	/** Service running the game's logic. Runs on a worker thread. */
	private GameView mGameView;
	private GameService mGame = null;
	private boolean isGameServiceBound() { return mGame != null; }

	/** Defines callbacks for service binding, passed to bindService() when binding GameService
	 * and to unbindService() when unbinding it. */
	final private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			GameService.GameBinder binder = (GameService.GameBinder) service;
			mGame = binder.getService();
			Log.i("DEBUG", "GameActivity: GameService has been bound.");
			mGame.init(GameActivity.this);
			mGame.setGameTickCallback(GameActivity.this::gameTick);
			mGameView.init(mGame);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
			mGame = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		mGameView = findViewById(R.id.game_view);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		bindGameService();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		unbindService(connection);
	}

	private void bindGameService()
	{
		Intent intent = new Intent(this, GameService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	private void gameTick()
	{
		mGameView.postInvalidate();
	}
}