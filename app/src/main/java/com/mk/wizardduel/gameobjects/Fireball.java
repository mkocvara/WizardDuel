package com.mk.wizardduel.gameobjects;

import android.graphics.drawable.Drawable;

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
		fireball = fireball != null ? fireball : new Fireball();
		fireball.mInPool = false;
		return fireball;
	}

	private Wizard mCasterWizard;
	private Vector2D mDirection;
	private int mBaseSpeed;

	private boolean mInPool = false;
	private boolean mSentFlying = false;
	private final Drawable mCastingDrawable, mFlyingDrawable;

	private final double BOUNCE_TIMEOUT = 0.5f;
	private double mBounceTimeout = 0.f;


	public Fireball()
	{
		mCastingDrawable = WizardApplication.getDrawableFromResourceId(R.drawable.fireball_anim);
		mFlyingDrawable = WizardApplication.getDrawableFromResourceId(R.drawable.fireball_flying_anim);

		setCollideable(true);
		setCollisionInset(new Vector2D(25f, 25f));
		setActive(false);
	}

	public Wizard getCaster() { return mCasterWizard; }

	protected void recycle()
	{
		setActive(false);
		mSentFlying = false;
		mInPool = true;
		pool.release(this);
	}

	@Override public void destroy()
	{
		if (!mInPool)
			recycle();
	}

	public void init(@NonNull Wizard caster, @NonNull Vector2D position)
	{
		mCasterWizard = caster;
		setPos(position);

		setTint(mCasterWizard.getTint());
		setDrawable(mCastingDrawable);
		resetDimensions();

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

		updateRotFromDir();

		mSentFlying = true;
	}

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);

		if (mBounceTimeout > 0)
			mBounceTimeout -= deltaTime;

		if (mSentFlying)
		{
			// Add direction vector timed by speed to the position to move
			setPos(getPos().getAdded(mDirection.getMultiplied(mBaseSpeed)));
		}
	}

	@Override
	public void handleCollision(@NonNull GameObject other)
	{
		//Log.i("DEBUG:Collisions", "Collision detected between a fireball and " + other);

		// Only apply collision logic if the fireball has been released.
		if (!mSentFlying)
			return;

		if (other instanceof Fireball)
		{
			this.destroy();
		}

		else if (other instanceof Wizard)
		{
			Wizard asWizard = (Wizard) other;
			if (asWizard != getCaster())
				this.destroy();
		}

		else if (other instanceof Boundary)
		{
			Boundary asBoundary = (Boundary) other;
			if (asBoundary.isReflective())
				bounce(asBoundary.getSurfaceNormal());
			else
				this.destroy();
		}
	}

	// Expose setActive() as public:
	@Override public void setActive(boolean active) { super.setActive(active); }

	private void updateRotFromDir()
	{
		// Set rotation: 0 rotation is the direction [-1,0] (tail on the right)
		Vector2D noRotDir = new Vector2D(-1f, 0f);
		double angle = mDirection.getAngle() + noRotDir.getAngle();
		rotation = (int) Math.toDegrees(angle);
	}

	private void bounce(Vector2D normal)
	{
		if (mBounceTimeout > 0)
			return;

		mDirection.subtract(normal.getMultiplied(2 * mDirection.dot(normal))); // Mirror reflection
		updateRotFromDir();
		mBounceTimeout = BOUNCE_TIMEOUT;
	}
}
