package com.mk.wizardduel;

import android.graphics.Rect;
import android.graphics.RectF;

import com.mk.wizardduel.views.GameView;

public class GameAttributes
{
	public static final int NOT_SET = -1;

	public GameView gameView = null;
	public Rect viewBounds = new Rect();

	public RectF wizard1RelativeBounds = new RectF();
	public RectF wizard2RelativeBounds = new RectF();

	public float fireballRelativeHeight = NOT_SET;
	public int fireballSpeedDp = 6; // in dp, must be converted to pixels
	public int fireballSpeedPx = NOT_SET; // in dp, must be converted to pixels
}
