package com.mk.wizardduel;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

import com.mk.wizardduel.views.GameView;

public class GameAttributes
{
	public static final int NOT_SET = -1;

	public GameView gameView = null;
	public Rect viewBounds = new Rect();
	public float castingAreaRelativeWidth = 0.3f;
	public float castingAreaWidth = NOT_SET; // must be set before accessing

	public @ColorInt int player1Colour = Color.BLUE;
	public @ColorInt int player2Colour = Color.RED;

	public RectF wizard1RelativeBounds = new RectF();
	public RectF wizard2RelativeBounds = new RectF();

	public float fireballRelativeHeight = NOT_SET;
	public int fireballSpeedDp = 6; // in dp, must be converted to pixels
	public int fireballSpeedPx = NOT_SET; // in dp, must be converted to pixels
}
