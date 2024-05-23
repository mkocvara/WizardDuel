package com.mk.wizardduel;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.mk.wizardduel.utils.Utils;
import com.mk.wizardduel.views.GameView;

public class GameAttributes
{
	public static final int NOT_SET = -1;

	public @ColorInt int player1Colour = Color.BLUE;
	public @ColorInt int player2Colour = Color.RED;

	public GameView gameView = null;
	public Rect viewBounds = new Rect();

	public float castingAreaWidth = NOT_SET; // must be set before accessing

	private float mCastingAreaRelativeWidth = 0.3334f;
	public float getCastingAreaRelativeWidth() { return mCastingAreaRelativeWidth; }
	public void setCastingAreaRelativeWidth(float w){ mCastingAreaRelativeWidth = Utils.clamp(w, 0.1f, 0.5f); }

	public RectF wizard1RelativeBounds = new RectF(0.01f, 0.38f, 0.f, 0.62f);
	public RectF wizard2RelativeBounds = new RectF(0.00f, 0.38f, 0.99f, 0.62f);

	/** Dips by default. */
	public int hitHatHeight = 55;
	/** Dips by default. */
	public int hitHatWidth = 67;

	private int mMaxHitPoints = 3;
	public int getMaxHitPoints() { return mMaxHitPoints; }
	/** Value of 0 means sandbox (no hits, no death, no victory) */
	public void setMaxHitPoints(int maxHP) { mMaxHitPoints = Utils.clamp(maxHP, 0, 5); }

	public float fireballRelativeHeight = 0.072f;
	/** Default in dips. Should be converted. */
	public int fireballSpeed = 375;

	private int mMaxChargedFireballs = 5;
	public int getMaxChargedFireballs() { return mMaxChargedFireballs; }
	public void setMaxChargedFireballs(int maxChargedFireballs) { mMaxChargedFireballs = Utils.clamp(maxChargedFireballs, 1, 99); }

	private int mStartingChargedFireballs = 3;
	public int getStartingChargedFireballs() { return mStartingChargedFireballs; }
	public void setStartingChargedFireballs(int startCharged) { mStartingChargedFireballs = Utils.clamp(startCharged, 0, 99); }

	private double mTimeToRechargeFireball = 2.f;
	public double getTimeToRechargeFireball() { return mTimeToRechargeFireball; }
	/** Value of 0 means immediate recharge */
	public void setTimeToRechargeFireball(double timeToRecharge) { mTimeToRechargeFireball = Utils.clamp(timeToRecharge, 0.0, 10.0); }

	private double mMaxShieldTime = 3.0; // in seconds
	public double getMaxShieldTime() { return mMaxShieldTime; }
	/** Value of 0 turns shields entirely off */
	public void setMaxShieldTime(double maxShieldTime) { mMaxShieldTime = Utils.clamp(maxShieldTime, 0, 999); }

	private double mShieldRechargeRate = 1.0; // per second
	public double getShieldRechargeRate() { return mShieldRechargeRate; }
	/** Value of 99 makes shield effectively unlimited */
	public void setShieldRechargeRate(double rechargeRate) { mShieldRechargeRate = Utils.clamp(rechargeRate, 0.1, 99); }

	/** Shield will not activate if current time left is under this threshold.
	 * This prevents imperceptibly short shield uptimes and nudges players toward
	 * using the shield slightly more efficiently */
	private double mShieldDepletionBuffer = .5;
	public double getShieldDepletionBuffer() { return mShieldDepletionBuffer; }
	/** Any value bigger than mMaxShieldTime makes shield unusable. */
	public void setShieldDepletionBuffer(double depletionBuffer) { mShieldDepletionBuffer = Utils.clamp(depletionBuffer, 0.01, 999); }

	/** If false, the shield will deplete at the same rate of 1 per 1 second no matter its length.
	 *  If true, the shield's depletion will be multiplied by the ratio of its length over mDepletionMedianSpan. */
	public boolean lengthBasedShieldDepletion = true;

	/** The length of shield at which the shield depletes at a rate of 1 per 1 second.
	 * Only applies if lengthBasedShieldDepletion is true.
	 * Backup value, convert from dips. */
	private int mShieldDepletionMedianSpan = 100;
	public int getShieldDepletionMedianSpan() { return mShieldDepletionMedianSpan; }
	public void setShieldDepletionMedianSpan(int span) { mShieldDepletionMedianSpan = Utils.clamp(span, 1, 999); }
}