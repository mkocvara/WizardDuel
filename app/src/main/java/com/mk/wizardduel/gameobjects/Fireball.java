package com.mk.wizardduel.gameobjects;

import android.graphics.drawable.Drawable;
import android.os.Message;

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

	public void recycle()
	{
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

	@Override
	public void update(double deltaTime)
	{
		super.update(deltaTime);

		//pos.add(mDirection.getMultiplied(mBaseSpeed));
	}

	@Override
	public void handleCollision(GameObject other)
	{
		// TODO
	}
}
