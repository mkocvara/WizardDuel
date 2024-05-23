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

	public GameView gameView = null;
	public Rect viewBounds = new Rect();
	public float castingAreaWidth = NOT_SET; // must be set before accessing

	public @ColorInt int player1Colour = Color.BLUE;
	public @ColorInt int player2Colour = Color.RED;

	public RectF wizard1RelativeBounds = new RectF();
	public RectF wizard2RelativeBounds = new RectF();

	public float fireballRelativeHeight = NOT_SET;
	public int fireballSpeedDp = 375; // in dp, must be converted to pixels
	public int fireballSpeedPx = NOT_SET; // in dp, must be converted to pixels
	public int hitHatHeightDip = 55;
	public int hitHatWidthDip = 67;

	private float mCastingAreaRelativeWidth = 0.3334f;
	public float getCastingAreaRelativeWidth() { return mCastingAreaRelativeWidth; }
	public void setCastingAreaRelativeWidth(float w){ mCastingAreaRelativeWidth = Utils.clamp(w, 0.1f, 0.5f); }

	private int mMaxHitPoints = 3;
	public int getMaxHitPoints() { return mMaxHitPoints; }
	public void setMaxHitPoints(int w){ mMaxHitPoints = Utils.clamp(w, 0, 5); }
		// Value of 0 means sandbox (no hits, no death, no victory)
}
