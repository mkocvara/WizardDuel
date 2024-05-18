package com.mk.wizardduel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Wizard extends GameObject
{
	enum DrawType {
		Animation,
		Bitmap
	}

	private DrawType mDrawType;

	public Wizard(BitmapDrawable drawable)
	{
		mDrawType = DrawType.Bitmap;
		init(drawable);
	}

	public Wizard(AnimationDrawable drawable)
	{
		mDrawType = DrawType.Animation;
		init(drawable);
		drawable.start();
	}

	private void init(Drawable drawable)
	{
		mDrawable = drawable;
		setHeight(drawable.getIntrinsicHeight());
		setWidth(drawable.getIntrinsicWidth());
	}

	@Override
	public void update()
	{
		// Do nothing, Wizards simply stay in their spots and focus on casting spells.
	}

	@Override
	public void draw(Canvas canvas, Paint paint)
	{
		Bitmap bitmap;

		if (mDrawType == DrawType.Animation)
		{
			Drawable currentFrame = mDrawable.getCurrent();
			bitmap = ((BitmapDrawable)currentFrame).getBitmap();
		}
		else
		{
			bitmap = ((BitmapDrawable)mDrawable).getBitmap();
		}

		transformAndDrawBitmap(canvas, paint, bitmap);
	}
}
