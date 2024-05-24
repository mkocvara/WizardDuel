package com.mk.wizardduel.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mk.wizardduel.R;

public class HUD extends ConstraintLayout
{
	private static final float SHIELD_BAR_BAR_FRACTION = 0.7f;

	private ImageView mShieldBar, mFireballBar, mFireball,
						mShieldBarFill, mFireballBarFill, mFireballBarDisabled;
	private TextView mNumFireballs;

	private boolean mMirrored = false;

	public HUD(@NonNull Context context)
	{
		super(context);
		init();
	}

	public HUD(@NonNull Context context, @Nullable AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public HUD(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	public HUD(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init()
	{
		inflate(getContext(), R.layout.hud, this);
		mShieldBar = findViewById(R.id.hud_img_shield_bar);
		mFireballBar = findViewById(R.id.hud_img_fireball_bar);
		mFireball = findViewById(R.id.hud_img_fireball);

		mShieldBarFill = findViewById(R.id.hud_img_shield_bar_fill);
		mFireballBarFill = findViewById(R.id.hud_img_fireball_bar_fill);
		mFireballBarDisabled = findViewById(R.id.hud_img_fireball_bar_disabled);

		mNumFireballs = findViewById(R.id.hud_txt_numfireballs);

		mFireballBarDisabled.setVisibility(INVISIBLE);

		mMirrored = getScaleX() < 0;
		if (mMirrored && mNumFireballs.getScaleX() > 0)
			mNumFireballs.setScaleX(mNumFireballs.getScaleX() * -1);
	}

	public void setTint(@ColorInt int colour)
	{
		mShieldBar.getDrawable().setTint(colour);
		mFireballBar.getDrawable().setTint(colour);
		mFireball.getDrawable().setTint(colour);
		mShieldBarFill.getDrawable().setTint(colour);
		mFireballBarFill.getDrawable().setTint(colour);
		mFireballBarDisabled.getDrawable().setTint(colour);

		mNumFireballs.setTextColor(colour);
	}

	public void setMirrored(boolean mirrored)
	{
		if (mirrored != mMirrored)
		{
			setScaleX(getScaleX() * -1);
			mNumFireballs.setScaleX(mNumFireballs.getScaleX() * -1);
		}
	}

	public void setNumFireballs(int numFireballs)
	{
		mNumFireballs.setText(Integer.toString(numFireballs));
	}

	public void setFireballRechargeBarDisabled(boolean b)
	{
		mFireballBarFill.setVisibility(b ? INVISIBLE : VISIBLE);
		mFireballBarDisabled.setVisibility(b ? VISIBLE : INVISIBLE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		float scale = (float) h / mShieldBar.getDrawable().getIntrinsicHeight();
		mShieldBar.setMaxWidth((int)((float)mShieldBar.getWidth() * scale));
	}

	public void setFireballBarFill(int percentProgress)
	{
		if (mFireballBarFill.getVisibility() != VISIBLE)
			return;

		float fractionProgress = (float) percentProgress / 100;
		Rect clip = mFireballBarFill.getClipBounds();

		float rightClip = mFireballBarFill.getWidth() * fractionProgress;

		if (clip != null)
			clip.right = (int)rightClip;
		else
			clip = new Rect(0,0, (int)rightClip, mFireballBarFill.getHeight());

		mFireballBarFill.setClipBounds(clip);
	}

	public void setShieldBarFill(int percentProgress)
	{
		float fractionProgress = (float) percentProgress / 100;
		Rect clip = mShieldBarFill.getClipBounds();

		float bottomBarBound = (float)mShieldBarFill.getHeight() * SHIELD_BAR_BAR_FRACTION;
		float topClip = bottomBarBound * (1.f - fractionProgress);

		if (clip != null)
			clip.top = (int)topClip;
		else
			clip = new Rect(0,(int)topClip, mShieldBarFill.getWidth(),  mShieldBarFill.getHeight());

		mShieldBarFill.setClipBounds(clip);
	}
}
