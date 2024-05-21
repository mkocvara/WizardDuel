package com.mk.wizardduel.gameobjects;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pools;

import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;
import com.mk.wizardduel.utils.Vector2D;

public class Fireball extends GameObject
{
	private static final Pools.SynchronizedPool<Fireball> pool = new Pools.SynchronizedPool<>(50);
	@NonNull
	public static Fireball obtain()
	{
		Fireball fireball = pool.acquire();
		return fireball != null ? fireball : new Fireball();
	}

	private Wizard mCasterWizard;
	private Vector2D mDirection;
	private int mBaseSpeed;

	private boolean mSentFlying = false;
	private Drawable mCastingDrawable, mFlyingDrawable;

	public Fireball()
	{
		mCastingDrawable = WizardApplication.getDrawableFromResourceId(R.drawable.fireball_anim);
		mFlyingDrawable = WizardApplication.getDrawableFromResourceId(R.drawable.fireball_flying_anim);

		setActive(false);
	}

	public Wizard getCaster() { return mCasterWizard; }

	public void recycle()
	{
		resetDimensions();
		setActive(false);
		pool.release(this);
	}

	@Override public void destroy() { recycle(); }

	public void init(@NonNull Wizard caster, @NonNull Vector2D position)
	{
		mCasterWizard = caster;
		setPos(position);

		setTint(mCasterWizard.getTint());
		setDrawable(mCastingDrawable);

		setActive(true);
	}

	public void release(Vector2D direction, int speed)
	{
		mDirection = direction;
		mBaseSpeed = speed;

		int preH = getHeight();
		int preW = getWidth();


		setDrawable(mFlyingDrawable);

		// Maintain height but correct for different aspect ratio.
		// This works, because the flying drawable is elongated in the dimension.
		float widthScale = (float) getWidth() / (float) mCastingDrawable.getIntrinsicHeight();
		int newWidth = (int) ((float)mFlyingDrawable.getIntrinsicWidth() * widthScale);
		setWidth(newWidth, false);

		// Set rotation: 0 rotation is the direction [-1,0] (tail on the right)
		Vector2D noRotDir = new Vector2D(-1f, 0f);
		double angle = direction.getAngle() + noRotDir.getAngle();
		rotation = (int)Math.toDegrees(angle);

		mSentFlying = true;
	}

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);

		if (mSentFlying)
		{
			// Add direction vector timed by speed to the position to move
			setPos(getPos().getAdded(mDirection.getMultiplied(mBaseSpeed)));
		}
	}

	@Override
	public void handleCollision(GameObject other)
	{
		// TODO
	}
}
