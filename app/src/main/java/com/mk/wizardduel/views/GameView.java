package com.mk.wizardduel.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

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

	private Drawable mCastingAreaDrawable1, mCastingAreaDrawable2;

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

		final float NOT_SET = GameAttributes.NOT_SET;
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

			mGameAttributes.fireballRelativeHeight = attributes.getFloat(R.styleable.GameView_fireballRelativeHeight, NOT_SET);
			mGameAttributes.fireballSpeedPx = (int)TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					mGameAttributes.fireballSpeedDp,
					getResources().getDisplayMetrics());

			mGameAttributes.castingAreaRelativeWidth = attributes.getFloat(R.styleable.GameView_castingAreaSize, mGameAttributes.castingAreaRelativeWidth);
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
	public void init(@NonNull GameService gameService)
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
		prepareCastingAreas();
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);

		if (!initialised)
			return;

		drawCastingAreas(canvas);

		mGameService.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent)
	{
		boolean result = mGestureDetector.onTouchEvent(motionEvent);
		result = mScaleGestureDetector.onTouchEvent(motionEvent) || result;
		result = mGameService.getGameInputHandler().onTouch(motionEvent) || result;

		return result;
	}

	private void prepareCastingAreas()
	{
		int castingAreaDrawableId = R.drawable.casting_border;
		mCastingAreaDrawable1 = AppCompatResources.getDrawable(getContext(), castingAreaDrawableId);
		mCastingAreaDrawable2 = AppCompatResources.getDrawable(getContext(), castingAreaDrawableId);

		if (mCastingAreaDrawable1 == null || mCastingAreaDrawable2 == null)
		{
			Log.e("GameView", "Couldn't fetch casting area drawable.");
			return;
		}

		float drawableHeight = mCastingAreaDrawable1.getIntrinsicHeight();
		float drawableWidth = mCastingAreaDrawable1.getIntrinsicWidth();

		int bottomBound = mGameAttributes.viewBounds.height();
		int topBound = 0;

		int rightBound1 = (int)((float)mGameAttributes.viewBounds.width() * mGameAttributes.castingAreaRelativeWidth);
		float scaleUp = bottomBound / drawableHeight;
		int newWidth = (int)(drawableWidth * scaleUp);
		int leftBound1 = rightBound1 - newWidth;
		mCastingAreaDrawable1.setBounds(leftBound1, topBound, rightBound1, bottomBound);

		int leftBound2 = mGameAttributes.viewBounds.width() - rightBound1;
		int rightBound2 = leftBound2 + newWidth;
		mCastingAreaDrawable2.setBounds(leftBound2, topBound, rightBound2, bottomBound);

		mCastingAreaDrawable1.setTint(mGameAttributes.player1Colour);
		mCastingAreaDrawable2.setTint(mGameAttributes.player2Colour);

		mGameAttributes.castingAreaWidth = rightBound1;

		int alpha = 200;
		mCastingAreaDrawable1.setAlpha(alpha);
		mCastingAreaDrawable2.setAlpha(alpha);
	}

	private void drawCastingAreas(Canvas canvas)
	{
		mCastingAreaDrawable1.draw(canvas);
		Rect castingArea2Bounds = mCastingAreaDrawable2.getBounds();

		canvas.save();
		canvas.rotate(180, castingArea2Bounds.exactCenterX(), castingArea2Bounds.exactCenterY());
		mCastingAreaDrawable2.draw(canvas);
		canvas.restore();
	}

	@Override
	public void invalidateDrawable(@NonNull Drawable drawable)
	{
		// Do nothing! Invalidation is handled by the game loop.
		// Override necessary for AnimationDrawables to work.
	}

	@Override
	protected boolean verifyDrawable(@NonNull Drawable who)
	{
		// Also necessary for AnimationDrawables to work.
		super.verifyDrawable(who);
		return true;
	}
}
