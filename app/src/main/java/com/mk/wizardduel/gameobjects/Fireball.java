package com.mk.wizardduel.gameobjects;

import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.util.Pools;

import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;
import com.mk.wizardduel.utils.Vector2D;

import java.util.logging.Handler;

public class Fireball extends GameObject
{
	private enum FireballState
	{
		RECYCLED,
		CASTING,
		FLYING,
		IMPACTING
	}

	private static final Pools.SynchronizedPool<Fireball> pool = new Pools.SynchronizedPool<>(50);

	private static final float IMPACT_DRAWABLE_UPSCALE = 2.5f;

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
	private FireballState mFireballState = FireballState.RECYCLED;
	private final AnimationDrawable mCastingDrawable, mFlyingDrawable, mImpactDrawable;

	private final double BOUNCE_TIMEOUT = 0.5f;
	private double mBounceTimeout = 0.f;
	private final double mImpactAnimLen;
	private double mImpactTimer = 0;

	public Fireball()
	{
		mCastingDrawable = (AnimationDrawable) WizardApplication.getDrawableFromResourceId(R.drawable.fireball_anim);
		mFlyingDrawable = (AnimationDrawable) WizardApplication.getDrawableFromResourceId(R.drawable.fireball_flying_anim);
		mImpactDrawable = (AnimationDrawable) WizardApplication.getDrawableFromResourceId(R.drawable.fireball_impact_anim);

		// Determine the length of the impact drawable
		mImpactDrawable.setOneShot(true);
		float numFrames = mImpactDrawable.getNumberOfFrames();
		float totalDurationMs = 0.f;
		for (int i = 0; i < numFrames; i++)
			totalDurationMs += mImpactDrawable.getDuration(i);
		mImpactAnimLen = totalDurationMs / 1000.f;

		setCollisionInset(new Vector2D(25f, 25f));
		setActive(false);
	}

	@Override
	public boolean isCollideable()
	{
		// Bypasses the GameObject mCollidable property.
		// Safer and easier than changing that property on every state change.
		return mFireballState == FireballState.FLYING;
	}

	public Wizard getCaster() { return mCasterWizard; }

	protected void recycle()
	{
		setActive(false);
		mFireballState = FireballState.RECYCLED;
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

		mFireballState = FireballState.FLYING;
	}

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);

		if (mBounceTimeout > 0)
			mBounceTimeout -= deltaTime;

		if (mFireballState == FireballState.FLYING)
		{
			// Add direction vector timed by speed to the position to move
			setPos(getPos().getAdded(mDirection.getMultiplied(mBaseSpeed)));
		}

		else if (mFireballState == FireballState.IMPACTING)
		{
			if (mImpactTimer <= 0)
				destroy();
			else
				mImpactTimer -= deltaTime;
		}
	}

	@Override
	public void handleCollision(@NonNull GameObject other, Region overlapRegion)
	{
		//Log.i("DEBUG:Collisions", "Collision detected between a fireball and " + other);

		if (mFireballState != FireballState.FLYING)
			return;

		if (other instanceof Fireball)
		{
			this.impact(getImpactNormal(other), overlapRegion);
		}

		else if (other instanceof Wizard)
		{
			Wizard asWizard = (Wizard) other;
			if (asWizard != getCaster())
				this.impact(getImpactNormal(other), overlapRegion);
		}

		else if (other instanceof Boundary)
		{
			Boundary asBoundary = (Boundary) other;
			if (asBoundary.isReflective())
				bounce(asBoundary.getSurfaceNormal());
			else
				this.impact(asBoundary.getSurfaceNormal(), overlapRegion);
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

	private void impact(Vector2D impactNormal, Region overlapRegion)
	{
		setDrawable(mImpactDrawable);

		int newHeight = (int) (getHeight() * IMPACT_DRAWABLE_UPSCALE);
		resetDimensions();
		setHeight(newHeight, true);

		mDirection.set(impactNormal);
		mDirection.negate();
		updateRotFromDir();

		// Set new position to be half the object's width (0.5 anchor) away from the centre point of impact
		// in the direction of the impact normal, in order to visually touch the impact point.
		Rect overlapBounds = overlapRegion.getBounds();
		Vector2D centreOfImpact = new Vector2D(overlapBounds.exactCenterX(), overlapBounds.exactCenterY());
		Vector2D newPos = centreOfImpact.getAdded(impactNormal.getMultiplied(getWidth()/2f));
		setPos(newPos);

		mImpactTimer = mImpactAnimLen;

		mFireballState = FireballState.IMPACTING;
	}

	private Vector2D getImpactNormal(GameObject impactObject)
	{
		Vector2D otherPos = impactObject.getCentrePoint();
		Vector2D impactNormal = otherPos.getSubtracted(this.getPos());
		impactNormal.negate();
		impactNormal.normalize();
		return impactNormal;
	}
}
