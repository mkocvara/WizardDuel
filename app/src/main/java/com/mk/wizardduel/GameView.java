package com.mk.wizardduel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import kotlin.NotImplementedError;

public class GameView extends View
{
	private Paint mGameObjectPaint;
	private ColorFilter mPlayer1ColourFilter, mPlayer2ColourFilter;
	private RectF mViewBounds = new RectF(),
					mWizard1RelativeBounds,
					mWizard2RelativeBounds;

	private Wizard mW1, mW2;


	public GameView(Context context, @Nullable AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
	{
		this(context, attrs, defStyleAttr, 0);
	}

	public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);

		// Extract attributes
		final TypedArray attributes = context.obtainStyledAttributes(
				attrs, R.styleable.GameView, defStyleAttr, defStyleRes);

		final float NOT_SET = -1.f;
		float wBothTop, wBothBottom, wBothEdge, w1Top, w1Bottom, w1Left, w2Top, w2Bottom, w2Right;
		w1Top = w1Bottom = w1Left = w2Top = w2Bottom = w2Right = NOT_SET;

		try
		{
			wBothTop = attributes.getFloat(R.styleable.GameView_wizardsRelativeTop, NOT_SET);
			wBothBottom = attributes.getFloat(R.styleable.GameView_wizardsRelativeBottom, NOT_SET);
			wBothEdge = attributes.getFloat(R.styleable.GameView_wizardsRelativeDistFromEdge, NOT_SET);

			w1Top = (wBothTop == NOT_SET) ? attributes.getFloat(R.styleable.GameView_wizard1RelativeTop, NOT_SET) : wBothTop;
			w2Top = (wBothTop == NOT_SET) ? attributes.getFloat(R.styleable.GameView_wizard2RelativeTop, NOT_SET) : wBothTop;
			w1Bottom = (wBothBottom == NOT_SET) ? attributes.getFloat(R.styleable.GameView_wizard1RelativeBottom, NOT_SET) : wBothBottom;
			w2Bottom = (wBothBottom == NOT_SET) ? attributes.getFloat(R.styleable.GameView_wizard2RelativeBottom, NOT_SET) : wBothBottom;
			w1Left =  (wBothEdge == NOT_SET) ? attributes.getFloat(R.styleable.GameView_wizard1RelativeLeft, NOT_SET)  : wBothEdge;
			w2Right = (wBothEdge == NOT_SET) ? attributes.getFloat(R.styleable.GameView_wizard2RelativeRight, NOT_SET) : 1-wBothEdge;
		}
		finally
		{
			attributes.recycle();

			String assertErrorMessage = "GameView: View is missing one or more Wizard bounds attributes.";
			assert(w1Top != NOT_SET) : assertErrorMessage;
			assert(w2Top != NOT_SET) : assertErrorMessage;
			assert(w1Bottom != NOT_SET) : assertErrorMessage;
			assert(w2Bottom != NOT_SET) : assertErrorMessage;
			assert(w1Left != NOT_SET) : assertErrorMessage;
			assert(w2Right != NOT_SET) : assertErrorMessage;
		}

		mWizard1RelativeBounds = new RectF(w1Left, w1Top, 0f, w1Bottom);
		mWizard2RelativeBounds = new RectF(0f, w2Top, w2Right, w2Bottom);

		init();
	}

	private void init()
	{
		BitmapDrawable wizardDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.wizard, null);
		assert wizardDrawable != null : "GameView: Wizard drawable could not be loaded.";

		mW1 = new Wizard(wizardDrawable);
		mW2 = new Wizard(wizardDrawable);

		setupPaints();
	}

	public ArrayList<AnimationDrawable> getAllAnims()
	{
		throw new NotImplementedError();

		// TODO: return mGame.getAllAnims();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// Account for padding (even though there should be none).
		float xpad = (float)(getPaddingLeft() + getPaddingRight());
		float ypad = (float)(getPaddingTop() + getPaddingBottom());

		float ww = (float)w - xpad;
		float hh = (float)h - ypad;

		// Calculate the view's bounds
		mViewBounds.set(0.f, 0.f, ww, hh);

		// Set Wizard sizes and positions
		int scaledHeight1 = (int)(mViewBounds.height() * (mWizard1RelativeBounds.bottom - mWizard1RelativeBounds.top));
		mW1.setHeight(scaledHeight1, true);

		int scaledHeight2 = (int)(mViewBounds.height() * (mWizard2RelativeBounds.bottom - mWizard2RelativeBounds.top));
		mW2.setHeight(scaledHeight2, true);

		mW1.pos.set((int)(ww * mWizard1RelativeBounds.left),  (int)(hh * mWizard1RelativeBounds.top));
		mW2.pos.set((int)(ww * mWizard2RelativeBounds.right), (int)(hh * mWizard2RelativeBounds.top));

		mW2.anchor.set(0.f, 1.f);
		mW2.rotation = 180.f;
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);

		mGameObjectPaint.setColorFilter(mPlayer1ColourFilter);
		mW1.update();
		mW1.draw(canvas, mGameObjectPaint);

		mGameObjectPaint.setColorFilter(mPlayer2ColourFilter);
		mW2.update();
		mW2.draw(canvas, mGameObjectPaint);
	}

	private void setupPaints()
	{
		mPlayer1ColourFilter = new LightingColorFilter(Color.BLUE, 1);
		mPlayer2ColourFilter = new LightingColorFilter(Color.RED, 1);

		mGameObjectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}
}
