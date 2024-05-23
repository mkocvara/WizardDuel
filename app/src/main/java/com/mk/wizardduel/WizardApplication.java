package com.mk.wizardduel;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

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
		return ResourcesCompat.getDrawable(mInstance.getResources(), rId, null);
	}

	public static Resources getAppResources()
	{
		return mInstance.getResources();
	}

	public static float dipToPx(float dip)
	{
		return TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				dip,
				WizardApplication.getAppResources().getDisplayMetrics());
	}
}
