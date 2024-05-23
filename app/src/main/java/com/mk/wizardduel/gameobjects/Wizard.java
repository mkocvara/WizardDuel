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
		void onFireballsChargedChanged(Player player, int newFireballsAvailable);
		void onFireballRecharge(Player player, int percentProgress);
		void onShieldTimeChargedChanged(Player player, int newEnergyLevel);
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

	private final int mMaxChargedFireballs;
	private int mChargedFireballs;
	private final double mTimeToRechargeFireball;
	private double mFireballRechargeTimer = 0.0;
	private int mFireballRechargePercent = 0;

	private final double mMaxShieldTime;
	private final boolean mUseLengthBasedShieldDepletion;
	private final int mShieldDepletionMedianSpan;
	private final double mShieldRechargeRate;
	private final double mShieldDepletionBuffer;
	private double mCurrentShieldTimeLeft;
	private int mShieldChargePercent = 100;
	private boolean mShieldActive = false;
	private int mCurrentShieldSpan = 0;

	/** Callback which will be passed the new number of hit points. */
	private WizardStatusListener mStatusListener;

	public Wizard(@NonNull GameAttributes gameAttributes, Player player)
	{
		mCurrentHitPoints = gameAttributes.getMaxHitPoints();
		mMaxChargedFireballs = gameAttributes.getMaxChargedFireballs();
		mChargedFireballs = gameAttributes.getStartingChargedFireballs();
		mTimeToRechargeFireball = gameAttributes.getTimeToRechargeFireball();

		mMaxShieldTime = gameAttributes.getMaxShieldTime();
		mShieldRechargeRate = gameAttributes.getShieldRechargeRate();
		mShieldDepletionBuffer = gameAttributes.getShieldDepletionBuffer();
		mUseLengthBasedShieldDepletion = gameAttributes.lengthBasedShieldDepletion;
		mShieldDepletionMedianSpan = gameAttributes.getShieldDepletionMedianSpan();
		mCurrentShieldTimeLeft = mMaxShieldTime;

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

	public Player getPlayer() { return mPlayer; }

	public int getNumChargedFireballs() { return mChargedFireballs; }

	public void setStatusListener(WizardStatusListener listener) { mStatusListener = listener; }

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);

		// FIREBALLS
		if (mChargedFireballs != mMaxChargedFireballs)
		{
			mFireballRechargeTimer += deltaTime;
			int newPercent = (int) ((mFireballRechargeTimer / mTimeToRechargeFireball) * 100);
			if (newPercent != mFireballRechargePercent)
			{
				mFireballRechargePercent = newPercent;
				mStatusListener.onFireballRecharge(mPlayer, mFireballRechargePercent);

				if (mFireballRechargePercent >= 100)
				{
					mChargedFireballs += 1;
					mStatusListener.onFireballsChargedChanged(mPlayer, mChargedFireballs);
					mFireballRechargeTimer = 0.0;
				}
			}
		}

		// SHIELD
		if (mShieldActive)
		{
			double depletion = deltaTime;

			if (mUseLengthBasedShieldDepletion)
				depletion *= (double)mCurrentShieldSpan / (double)mShieldDepletionMedianSpan;

			mCurrentShieldTimeLeft = Math.max(0, mCurrentShieldTimeLeft - depletion);
		}
		else if (mCurrentShieldTimeLeft < mMaxShieldTime)
		{
			mCurrentShieldTimeLeft += deltaTime * mShieldRechargeRate;
		}

		int newPercent = (int) ((mCurrentShieldTimeLeft / mMaxShieldTime) * 100);
		if (newPercent != mShieldChargePercent)
		{
			mShieldChargePercent = newPercent;
			mStatusListener.onShieldTimeChargedChanged(mPlayer, mShieldChargePercent);
		}
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

	public boolean tryReleaseFireball()
	{
		if (mChargedFireballs == 0)
			return false;

		mChargedFireballs--;
		mStatusListener.onFireballsChargedChanged(mPlayer, mChargedFireballs);

		return true;
	}

	public boolean tryActivateShield(int span)
	{
		if (mCurrentShieldTimeLeft <= mShieldDepletionBuffer)
			return false;

		mShieldActive = true;
		mCurrentShieldSpan = span;
		return true;
	}

	public boolean tryContinueShield(int span)
	{
		if (mCurrentShieldTimeLeft <= 0)
			return false;

		mCurrentShieldSpan = span;
		return true;
	}

	public void deactivateShield() { mShieldActive = false; }

	private void registerHit()
	{
		// Log.i("DEBUG:Collisions", "A Wizard got hit by the opponent's fireball. Ouch.");
		mCurrentHitPoints = Math.max(mCurrentHitPoints-1, 0);
		if (mStatusListener != null)
			mStatusListener.onHitPointLost(mPlayer, mCurrentHitPoints);
	}
}
