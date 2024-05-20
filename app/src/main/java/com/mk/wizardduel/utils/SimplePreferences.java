package com.mk.wizardduel.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.mk.wizardduel.R;
import com.mk.wizardduel.WizardApplication;

/**
 * Simple API to handle this application's few settings.
 */
public class SimplePreferences
{
	final private SharedPreferences mAppSharedPreferences;
	final private String mPrefSoundKey, mPrefAnimKey;

	private static SimplePreferences mInstance;
	public static SimplePreferences get()
	{
		if (mInstance == null)
		{
			mInstance = new SimplePreferences();
		}

		return mInstance;
	}

	private SimplePreferences()
	{
		Context appContext = WizardApplication.getInstance();
		mAppSharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
		mPrefSoundKey = appContext.getString(R.string.saved_key_sound);
		mPrefAnimKey = appContext.getString(R.string.saved_key_anim);
	}

	public boolean getPrefSound()
	{
		return mAppSharedPreferences.getBoolean(mPrefSoundKey, true);
	}

	public boolean getPrefAnim()
	{
		return mAppSharedPreferences.getBoolean(mPrefAnimKey, true);
	}

	public void savePrefSound(boolean on)
	{
		SharedPreferences.Editor edit = mAppSharedPreferences.edit();
		edit.putBoolean(mPrefSoundKey, on);
		edit.apply();
	}

	public void savePrefAnim(boolean on)
	{
		SharedPreferences.Editor edit = mAppSharedPreferences.edit();
		edit.putBoolean(mPrefAnimKey, on);
		edit.apply();
	}
}
