package com.mk.wizardduel;

import android.graphics.Rect;
import android.graphics.RectF;

import com.mk.wizardduel.views.GameView;

public class GameAttributes
{
	public GameView gameView = null;
	public Rect viewBounds = new Rect();
	public RectF wizard1RelativeBounds = new RectF();
	public RectF wizard2RelativeBounds = new RectF();
}
