package com.mk.wizardduel.utils;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleOwner;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This lifecycle-aware class can be used by activities to handle its AnimatedDrawables and
 * simplify their collective management. It automatically stops and resumes animations at relevant
 * points of an activity's lifecycle.
 */
public class AnimHandler implements DefaultLifecycleObserver
{
	final private ArrayList<AnimationDrawable> mAnims = new ArrayList<>();
	final private Lifecycle mLifecycle;
	final private SimplePreferences mPreferences;

	/**
	 * True if animation been started using start().
	 */
	private boolean mPlaying = false;

	/**
	 * True if the handled AnimatedDrawables are currently animating.
	 */
	private boolean mAnimating = false;

	public AnimHandler(Lifecycle lifecycle)
	{
		mLifecycle = lifecycle;
		mPreferences = SimplePreferences.get();
	}

	public AnimHandler(Lifecycle lifecycle, boolean playImmediately)
	{
		this(lifecycle);
		if (playImmediately) start();
	}

	/**
	 * Add an AnimationDrawable to the list of handled animations.
	 * @param anim The AnimationDrawable to add.
	 */
	public void addAnim(@NonNull AnimationDrawable anim)
	{
			mAnims.add(anim);
			if (mAnimating) anim.start();
	}

	/**
	 * Add multiple AnimationDrawables to the list of handled animations.
	 * @param anims The AnimationDrawables to add.
	 */
	public void addAnim(@NonNull ArrayList<AnimationDrawable> anims)
	{
		for (AnimationDrawable a : anims)
			addAnim(a);
	}

	/**
	 * Add multiple AnimationDrawables to the list of handled animations.
	 * @param anims The AnimationDrawables to add.
	 */
	public void addAnim(@NonNull AnimationDrawable[] anims)
	{
		for (AnimationDrawable a : anims)
			addAnim(a);
	}

	public void addStateListDrawableAnims(@NonNull StateListDrawable stateListDrawable)
	{
		AnimationDrawable anim;
		if (Build.VERSION.SDK_INT >= 29)
		{
			for (int i = 0; i < stateListDrawable.getStateCount(); i++)
			{
				Drawable drawable = stateListDrawable.getStateDrawable(i);
				if (drawable instanceof AnimationDrawable)
				{
					anim = (AnimationDrawable) drawable;
					addAnim(anim);
				}
			}
		}
		else
		{
			try
			{
				// A backup reflection method to use hidden methods to get individual drawables from a StateListDrawable
				// Source: https://stackoverflow.com/questions/26713618/get-specific-drawable-from-state-list-drawable
				// Number of states is hard-coded as there seems to be no way to get it pre-API v29.
				// If it doesn't work, the toggle buttons simply won't animate on very old devices.
				final int numStates = 2;
				for (int i = 0; i < numStates; i++)
				{
					Method getStateDrawable = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);

					Drawable drawable = (Drawable) getStateDrawable.invoke(stateListDrawable, i);
					if (drawable instanceof AnimationDrawable)
					{
						anim = (AnimationDrawable) drawable;
						addAnim(anim);
					}
				}
			}
			catch (Exception e)
			{
				Log.w("API", "Old API reflection method to get individual drawables from a StateListDrawable failed. They won't animate.");
			}
		}
	}

	/**
	 * Remove an AnimationDrawable from the list of handled animations.
	 * @param anim The AnimationDrawable to remove.
	 */
	public void removeAnim(@NonNull AnimationDrawable anim)
	{
			mAnims.remove(anim);
			anim.stop();
	}

	/**
	 * Remove multiple AnimationDrawables to the list of handled animations.
	 * @param anims The AnimationDrawables to remove.
	 */
	public void removeAnim(@NonNull ArrayList<AnimationDrawable> anims)
	{
		for (AnimationDrawable a : anims)
			removeAnim(a);
	}

	/**
	 * Remove multiple AnimationDrawables to the list of handled animations.
	 * @param anims The AnimationDrawables to remove.
	 */
	public void removeAnim(@NonNull AnimationDrawable[] anims)
	{
		for (AnimationDrawable a : anims)
			removeAnim(a);
	}

	/**
	 * Indicates that the animations should be playing.
	 */
	public void start()
	{
		mPlaying = true;

		if (mLifecycle.getCurrentState().isAtLeast(State.STARTED))
			animate();
	}

	/**
	 * Indicates that the animations should not be playing.
	 */
	public void stop()
	{
		mPlaying = false;

		stopAnimating();
	}


	/**
	 * Starts all handled animations.
	 */
	protected void animate()
	{
		boolean animationsOn = mPreferences.getPrefAnim();

		if (!animationsOn || mAnimating)
			return;

		for (AnimationDrawable anim : mAnims)
			anim.start();

		mAnimating = true;
	}


	/**
	 * Stops all handled animations.
	 */
	protected void stopAnimating()
	{
		if (!mAnimating)
			return;

		for (AnimationDrawable anim : mAnims)
			anim.stop();

		mAnimating = false;
	}

	@Override
	public void onStart(@NonNull LifecycleOwner owner)
	{
		if (mPlaying) animate();
	}

	@Override
	public void onStop(@NonNull LifecycleOwner owner)
	{
		if (mPlaying) stopAnimating();
	}

	public boolean areAnimsPlaying()
	{
		return mPlaying;
	}
}
