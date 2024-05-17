package com.example.wizardduel;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A ToggleButton extension which assumes halved button design with states. When checked state
 * changes, padding is added to push the text to one or the other half of the button.
 */
public class ToggleButtonDual extends androidx.appcompat.widget.AppCompatToggleButton
{
	private int mCachedPaddingLeft, mCachedPaddingRight;

	public ToggleButtonDual(Context context)
	{
		super(context);
	}

	public ToggleButtonDual(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ToggleButtonDual(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public void setChecked(boolean checked)
	{
		super.setChecked(checked);
		updatePadding();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);

		if (changed)
			updatePadding();
	}

	private void updatePadding()
	{
		int paddingAmount = getWidth() / 2;

		if (isChecked())
		{
			mCachedPaddingLeft = getPaddingLeft();
			mCachedPaddingLeft = getPaddingStart();
			setPadding(paddingAmount, getPaddingTop(), mCachedPaddingRight, getPaddingBottom());
		}
		else
		{
			mCachedPaddingRight = getPaddingRight();
			mCachedPaddingRight = getPaddingEnd();
			setPadding(mCachedPaddingLeft, getPaddingTop(), paddingAmount, getPaddingBottom());
		}
	}
}