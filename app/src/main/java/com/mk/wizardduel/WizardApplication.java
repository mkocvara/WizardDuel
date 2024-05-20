package com.mk.wizardduel;

import android.app.Application;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

/**
 * Light extension to Application which holds a static instance of itself for singleton retrieval.
 */
public class WizardApplication extends Application
{
	private static WizardApplication mInstance;
	public static WizardApplication getInstance() { return mInstance; }

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static Drawable getDrawableFromResourceId(int rId)
	{
		Drawable d = ResourcesCompat.getDrawable(mInstance.getResources(), rId, null);
		return d;
	}
}
