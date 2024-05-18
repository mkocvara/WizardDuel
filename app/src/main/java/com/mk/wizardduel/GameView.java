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
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class GameView extends View
{
	private Bitmap mW1Bitmap, mW2Bitmap;
	private Paint mGameObjectPaint;
	private int mPlayer1Colour, mPlayer2Colour;
	private ColorFilter mPlayer1ColourFilter, mPlayer2ColourFilter;
	private RectF mViewBounds = new RectF(),
					mWizard1Bounds = new RectF(),
					mWizard2Bounds = new RectF(),
					mWizard1RelativeBounds,
					mWizard2RelativeBounds;

	public GameView(Context context)
	{
		super(context);
		init();
	}

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

		final TypedArray attributes = context.obtainStyledAttributes(
				attrs, R.styleable.GameView, defStyleAttr, defStyleRes);

		final float NOT_SET = -1.f;
		float wBothTop, wBothBottom, wBothEdge, w1Top, w1Bottom, w1Left, w2Top, w2Bottom, w2Right;
		wBothTop = wBothBottom = wBothEdge = w1Top = w1Bottom = w1Left = w2Top = w2Bottom = w2Right = NOT_SET;

		try
		{
			wBothTop = attributes.getFloat(R.styleable.GameView_wizardsRelativeTop, NOT_SET);
			wBothBottom = attributes.getFloat(R.styleable.GameView_wizardsRelativeBottom, NOT_SET);
			wBothEdge = attributes.getFloat(R.styleable.GameView_wizardsRelativeDistFromEdge, NOT_SET);

			w1Top = (wBothTop == -1.f) ? attributes.getFloat(R.styleable.GameView_wizard1RelativeTop, NOT_SET) : wBothTop;
			w2Top = (wBothTop == -1.f) ? attributes.getFloat(R.styleable.GameView_wizard2RelativeTop, NOT_SET) : wBothTop;
			w1Bottom = (wBothBottom == -1.f) ? attributes.getFloat(R.styleable.GameView_wizard1RelativeBottom, NOT_SET) : wBothBottom;
			w2Bottom = (wBothBottom == -1.f) ? attributes.getFloat(R.styleable.GameView_wizard2RelativeBottom, NOT_SET) : wBothBottom;
			w1Left =  (wBothEdge == -1.f) ? attributes.getFloat(R.styleable.GameView_wizard1RelativeLeft, NOT_SET)  : wBothEdge;
			w2Right = (wBothEdge == -1.f) ? attributes.getFloat(R.styleable.GameView_wizard2RelativeRight, NOT_SET) : 1-wBothEdge;
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

		mW1Bitmap = wizardDrawable.getBitmap();
		mW2Bitmap = Utils.makeFlippedBitmap(mW1Bitmap, true, false);

		setupPaints();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// Account for padding (even though there should be none).
		float xpad = (float)(getPaddingLeft() + getPaddingRight());
		float ypad = (float)(getPaddingTop() + getPaddingBottom());

		float ww = (float)w - xpad;
		float hh = (float)h - ypad;

		// Calculate the needed bounds
		mViewBounds.set(0.f, 0.f, ww, hh);

		float w1Width = getWizardWidth(1);
		float w2Width = getWizardWidth(2);

		mWizard1Bounds.set(
				ww * mWizard1RelativeBounds.left,
				hh * mWizard1RelativeBounds.top,
				ww * mWizard1RelativeBounds.left + w1Width,
				hh * mWizard1RelativeBounds.bottom);

		mWizard2Bounds.set(
				ww * mWizard2RelativeBounds.right - w2Width,
				hh * mWizard2RelativeBounds.top,
				ww * mWizard2RelativeBounds.right,
				hh * mWizard2RelativeBounds.bottom);
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);

		mGameObjectPaint.setColorFilter(mPlayer1ColourFilter);
		canvas.drawBitmap(mW1Bitmap, null, mWizard1Bounds, mGameObjectPaint);

		mGameObjectPaint.setColorFilter(mPlayer2ColourFilter);
		canvas.drawBitmap(mW2Bitmap, null, mWizard2Bounds, mGameObjectPaint);
	}

	private void setupPaints()
	{
		mPlayer1ColourFilter = new LightingColorFilter(Color.BLUE, 1);
		mPlayer2ColourFilter = new LightingColorFilter(Color.RED, 1);

		mGameObjectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	private float getWizardWidth(int i)
	{
		RectF relativeBounds = (i == 1) ? mWizard1RelativeBounds : mWizard2RelativeBounds;

		int wizardDrawableHeight = mW1Bitmap.getHeight();
		int wizardDrawableWidth = mW1Bitmap.getWidth();
			// Note: dimensions of both W1 and W2 bitmaps are equal

		float scaledHeight = mViewBounds.height() * (relativeBounds.bottom - relativeBounds.top);
		float scale = scaledHeight / wizardDrawableHeight;
		return wizardDrawableWidth * scale;
	}
}
