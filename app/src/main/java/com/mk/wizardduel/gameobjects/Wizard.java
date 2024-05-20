package com.mk.wizardduel.gameobjects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;

public class Wizard extends GameObject
{
	enum DrawType {
		Animation,
		Bitmap
	}

	private DrawType mDrawType; // TODO reevaluate the necessity (looking superseded with getAnim())

	public Wizard()
	{
		Drawable wizardDrawable = WizardApplication.getDrawableFromResourceId(R.drawable.wizard);
		if (wizardDrawable == null)
		{
			destroy();
			return;
		}

		if (wizardDrawable instanceof BitmapDrawable)
			mDrawType = DrawType.Bitmap;
		else if (wizardDrawable instanceof AnimationDrawable)
			mDrawType = DrawType.Animation;

		init(wizardDrawable);
	}

	public Wizard(BitmapDrawable drawable)
	{
		mDrawType = DrawType.Bitmap;
		init(drawable);
	}

	public Wizard(AnimationDrawable drawable)
	{
		mDrawType = DrawType.Animation;
		init(drawable);
	}

	private void init(Drawable drawable)
	{
		mDrawable = drawable;
		setHeight(drawable.getIntrinsicHeight());
		setWidth(drawable.getIntrinsicWidth());

		collidable = true;
		setActive(true);
	}

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);
		// Do nothing, Wizards simply stay in their spots and focus on casting spells.
	}

	@Override
	public void draw(Canvas canvas, Paint paint)
	{
		Drawable current = mDrawable.getCurrent();
		Bitmap bitmap = ((BitmapDrawable) current).getBitmap();
		transformAndDrawBitmap(canvas, paint, bitmap);
	}

	@Override
	public void handleCollision(GameObject other)
	{
		/*/ TODO implement Eg.:
		if (other instanceof Fireball)
		{
			loseHealth();
		}
		*/
	}
}
