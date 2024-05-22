package com.mk.wizardduel.gameobjects;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.DrawableRes;

import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;

import kotlin.random.Random;

public class Wizard extends GameObject
{
	private static final @DrawableRes int[] drawablePool = {
			R.drawable.wizard1_anim,
			R.drawable.wizard2_anim,
			R.drawable.wizard3_anim,
			R.drawable.wizard4_anim,
			R.drawable.wizard5_anim
	};

	public Wizard()
	{
		int randDrawableIndex = Random.Default.nextInt(drawablePool.length);
		int drawableRes = drawablePool[randDrawableIndex];
		Drawable wizardDrawable = WizardApplication.getDrawableFromResourceId(drawableRes);
		if (wizardDrawable == null)
		{
			Log.e("Resource", "Wizard: Drawable resource not found, wizard can't be rendered.");
			destroy();
			return;
		}

		init(wizardDrawable);
	}

	public Wizard(BitmapDrawable drawable)
	{
		init(drawable);
	}

	public Wizard(AnimationDrawable drawable)
	{
		init(drawable);
	}

	private void init(Drawable drawable)
	{
		setDrawable(drawable);

		int h = drawable.getIntrinsicHeight();
		int w = drawable.getIntrinsicWidth();

		setHeight(h);
		setWidth(w);

		setCollideable(true);
		setActive(true);
	}

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);
		// Do nothing, Wizards simply stay in their spots and focus on casting spells.

	}

	@Override
	public void handleCollision(GameObject other)
	{
		if (other instanceof Fireball)
		{
			Fireball asFireball = (Fireball) other;
			if (asFireball.getCaster() != this)
				registerHit();
		}
	}

	private void registerHit()
	{
		Log.i("DEBUG:Collisions", "A Wizard got hit by the opponent's fireball. Ouch.");
	}
}
