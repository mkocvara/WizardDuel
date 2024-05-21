package com.mk.wizardduel.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mk.wizardduel.GameAttributes;
import com.mk.wizardduel.R;
import com.mk.wizardduel.services.GameService;

public class GameView extends View
{
	private GameService mGameService;
	final private GameAttributes mGameAttributes = new GameAttributes();

	private boolean initialised = false;

	private GestureDetector mGestureDetector;
	private ScaleGestureDetector mScaleGestureDetector;

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

			mGameAttributes.fireballRelativeHeight = attributes.getFloat(R.styleable.GameView_fireballRelativeHeight, -1);
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

		mGameAttributes.wizard1RelativeBounds = new RectF(w1Left, w1Top, 0.f, w1Bottom);
		mGameAttributes.wizard2RelativeBounds = new RectF(0.f, w2Top, w2Right, w2Bottom);
	}

	/** Must be called by parent activity, passing a reference to a running GameService */
	public void init(GameService gameService)
	{
		mGameAttributes.gameView = this;
		mGameService = gameService;
		mGameService.init(mGameAttributes);

		mGestureDetector = new GestureDetector(getContext(), gameService.getGameInputHandler());
		mGestureDetector.setOnDoubleTapListener(gameService.getGameInputHandler());
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), gameService.getGameInputHandler());
		//mScaleGestureDetector.setQuickScaleEnabled(true); // TODO check when implementing shield

		initialised = true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// Account for padding (even though there should be none).
		int xpad = getPaddingLeft() + getPaddingRight();
		int ypad = getPaddingTop() + getPaddingBottom();

		int ww = w - xpad;
		int hh = h - ypad;

		// Set the view's bounds
		mGameAttributes.viewBounds.set(0, 0, ww, hh);
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);

		if (!initialised)
			return;

		mGameService.draw(canvas);
	}

	@Override
	public void invalidateDrawable(@NonNull Drawable drawable)
	{
		// Do nothing! Invalidation is handled by the game loop.
	}

	@Override
	protected boolean verifyDrawable(@NonNull Drawable who)
	{
		super.verifyDrawable(who);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent)
	{
		boolean result = mGestureDetector.onTouchEvent(motionEvent);
		result = mScaleGestureDetector.onTouchEvent(motionEvent) || result;
		result = mGameService.getGameInputHandler().onTouch(motionEvent) || result;

		return result;
	}
}
