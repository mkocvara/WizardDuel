package com.mk.wizardduel.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mk.wizardduel.R;
import com.mk.wizardduel.gameobjects.Wizard;
import com.mk.wizardduel.services.GameService;

import java.util.ArrayList;

import kotlin.NotImplementedError;

public class GameView extends View
{
	private GameService mGame;
	final private RectF mViewBounds = new RectF(),
					mWizard1RelativeBounds,
					mWizard2RelativeBounds;

	private boolean initialised = false;

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

		//init();
	}

	/** Must be called by parent activity, passing a reference to a running GameService */
	public void init(GameService gameService)
	{
		mGame = gameService;

		createWizards();

		initialised = true;
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
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);

		if (!initialised)
			return;

		mGame.draw(canvas);
	}

	private void createWizards()
	{
		Wizard wizard1 = new Wizard();
		Wizard wizard2 = new Wizard();

		// Set Wizard sizes and positions
		int scaledHeight1 = (int)(mViewBounds.height() * (mWizard1RelativeBounds.bottom - mWizard1RelativeBounds.top));
		wizard1.setHeight(scaledHeight1, true);

		int scaledHeight2 = (int)(mViewBounds.height() * (mWizard2RelativeBounds.bottom - mWizard2RelativeBounds.top));
		wizard2.setHeight(scaledHeight2, true);

		wizard1.pos.set((int)(mViewBounds.width() * mWizard1RelativeBounds.left),  (int)(mViewBounds.height() * mWizard1RelativeBounds.top));
		wizard2.pos.set((int)(mViewBounds.width() * mWizard2RelativeBounds.right), (int)(mViewBounds.height() * mWizard2RelativeBounds.top));

		wizard2.anchor.set(0.f, 1.f);
		wizard2.rotation = 180.f;

		Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint1.setColorFilter(new LightingColorFilter(Color.BLUE, 1));
		wizard1.paint = paint1;

		Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint2.setColorFilter(new LightingColorFilter(Color.RED, 1));
		wizard2.paint = paint2;

		mGame.addObject(wizard1);
		mGame.addObject(wizard2);
	}
}
