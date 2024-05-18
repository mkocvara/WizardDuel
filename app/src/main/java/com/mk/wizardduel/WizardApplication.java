package com.mk.wizardduel;

import android.app.Application;

/**
 * Light extension to Application which holds a static instance of itself for singleton retrieval.
 */
public class WizardApplication extends Application
{
	private static WizardApplication mInstance;
	static WizardApplication getInstance() { return mInstance; }

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}
}
