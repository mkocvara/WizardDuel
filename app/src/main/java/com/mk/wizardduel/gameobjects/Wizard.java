package com.mk.wizardduel.gameobjects;

import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.mk.wizardduel.GameAttributes;
import com.mk.wizardduel.Player;
import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;
import com.mk.wizardduel.utils.Vector2D;

import kotlin.random.Random;

public class Wizard extends GameObject
{
	public interface WizardStatusListener
	{
		void onHitPointLost(Player player, int newHitPoints);
		void onFireballsAvailableChanged(Player player, int newFireballsAvailable);
		void onFireballRecharge(Player player, float progress);
		void onShieldEnergyLevelChanged(Player player, int newEnergyLevel);
	}

	private static final @DrawableRes int[] drawablePool = {
			R.drawable.wizard1_anim,
			R.drawable.wizard2_anim,
			R.drawable.wizard3_anim,
			R.drawable.wizard4_anim,
			R.drawable.wizard5_anim
	};

	private final Player mPlayer;
	private int mCurrentHitPoints;

	/** Callback which will be passed the new number of hit points. */
	private WizardStatusListener mStatusListener;

	public Wizard(@NonNull GameAttributes gameAttributes, Player player)
	{
		mCurrentHitPoints = gameAttributes.getMaxHitPoints();
		mPlayer = player;

		int randDrawableIndex = Random.Default.nextInt(drawablePool.length);
		int drawableRes = drawablePool[randDrawableIndex];
		Drawable wizardDrawable = WizardApplication.getDrawableFromResourceId(drawableRes);
		if (wizardDrawable == null)
		{
			Log.e("Resource", "Wizard: Drawable resource not found, wizard can't be rendered.");
			destroy();
			return;
		}

		setDrawable(wizardDrawable);

		setCollisionInset(new Vector2D(15f, 15f));
		setCollideable(true);

		setActive(true);
	}

	public void setStatusListener(WizardStatusListener listener) { mStatusListener = listener; }

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);
		// Do nothing, Wizards simply stay in their spots and focus on casting spells.
	}

	@Override
	public void handleCollision(GameObject other, Region overlapRegion)
	{
		if (other instanceof Fireball)
		{
			Fireball asFireball = (Fireball) other;
			if (asFireball.getCaster() != this)
				registerHit();
		}
	}

	private void registerHit()
	{
		// Log.i("DEBUG:Collisions", "A Wizard got hit by the opponent's fireball. Ouch.");
		mCurrentHitPoints = Math.max(mCurrentHitPoints-1, 0);
		if (mStatusListener != null)
			mStatusListener.onHitPointLost(mPlayer, mCurrentHitPoints);
	}
}
