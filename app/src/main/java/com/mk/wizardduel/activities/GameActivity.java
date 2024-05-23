package com.mk.wizardduel.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;

import com.mk.wizardduel.GameAttributes;
import com.mk.wizardduel.Player;
import com.mk.wizardduel.WizardApplication;
import com.mk.wizardduel.gameobjects.Wizard;
import com.mk.wizardduel.services.GameService;
import com.mk.wizardduel.R;
import com.mk.wizardduel.utils.AnimHandler;
import com.mk.wizardduel.views.GameView;

public class GameActivity extends ImmersiveActivity implements Wizard.WizardStatusListener
{
	/** Service running the game's logic. Runs the game loop on a worker thread. */
	private GameService mGameService = null;
	private GameView mGameView;
	private boolean isGameServiceBound() { return mGameService != null; }
	private AnimHandler mAnimHandler;

	private LinearLayout mHPLayoutP1, mHPLayoutP2;

	/** Defines callbacks for service binding, passed to bindService() when binding GameService
	 * and to unbindService() when unbinding it. */
	final private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			GameService.GameBinder binder = (GameService.GameBinder) service;
			mGameService = binder.getService();
			Log.i("DEBUG", "GameActivity: GameService has been bound.");
			mGameService.bind(GameActivity.this);
			mGameService.setGameTickCallback(GameActivity.this::gameTick);
			mGameView.init(mGameService);
			setUpUI();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
			mGameService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		mGameView = findViewById(R.id.game_view);
		mHPLayoutP1 = findViewById(R.id.game_layout_hp_p1);
		mHPLayoutP2 = findViewById(R.id.game_layout_hp_p2);

		mAnimHandler = new AnimHandler(getLifecycle(), true);
		getLifecycle().addObserver(mAnimHandler);
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
		mGameView.postInvalidateOnAnimation();
	}

	private void setUpUI()
	{
		GameAttributes gameAttributes = mGameService.getGameAttributes();
		int maxHP = gameAttributes.getMaxHitPoints();
		@ColorInt int p1Colour = gameAttributes.player1Colour;
		@ColorInt int p2Colour = gameAttributes.player2Colour;

		for (int i = 0; i < Player.COUNT; i++)
		{
			for (int j = 0; j < maxHP; j++)
			{
				LinearLayout layoutToAddTo = (Player.getPlayer(i) == Player.PLAYER_ONE) ? mHPLayoutP1 : mHPLayoutP2;
				@ColorInt int tint = (Player.getPlayer(i) == Player.PLAYER_ONE) ? p1Colour : p2Colour;

				ImageView hpImgView = new ImageView(this);

				hpImgView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.hit_hat_anim));
				hpImgView.getDrawable().setTint(tint);

				/*// Actually... the UI animating like that too make the screen too busy, it's distracting.
				  // Keeping it here for at least one commit in case I change my mind.
				AnimationDrawable anim = (AnimationDrawable) hpImgView.getDrawable();
				if (anim != null)
					mAnimHandler.addAnim(anim);
				else
					Log.w("GameActivity", "Hit Hat drawable is not an AnimationDrawable and will not animate.");
				*/

				int height = (int) WizardApplication.dipToPx(gameAttributes.hitHatHeightDip);
				int width = (int) WizardApplication.dipToPx(gameAttributes.hitHatWidthDip);
				hpImgView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

				layoutToAddTo.addView(hpImgView);
			}
		}
	}

	@Override
	public void onHitPointLost(Player player, int newHitPoints)
	{
		runOnUiThread(() -> {
			LinearLayout layoutToUpdate = (player == Player.PLAYER_ONE) ? mHPLayoutP1 : mHPLayoutP2;
			View imageView = layoutToUpdate.getChildAt(newHitPoints);
			imageView.setEnabled(false);
			imageView.setVisibility(View.GONE);
			layoutToUpdate.postInvalidate();

			if (newHitPoints == 0 && mGameService.getGameAttributes().getMaxHitPoints() != 0)
				gameOver();
		});
	}

	@Override
	public void onFireballsAvailableChanged(Player player, int newFireballsAvailable)
	{
		// TODO

	}

	@Override
	public void onFireballRecharge(Player player, float progress)
	{
		// TODO

	}

	@Override
	public void onShieldEnergyLevelChanged(Player player, int newEnergyLevel)
	{
		// TODO

	}

	private void gameOver()
	{
		Log.i("DEBUG", "Game Over!");
	}
}