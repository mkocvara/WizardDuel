package com.mk.wizardduel;

import com.mk.wizardduel.gameobjects.Shield;
import com.mk.wizardduel.gameobjects.Wizard;

/** Convenience class for housing important data that needs to persist
 *  across the game activity's lifecycle. */
public class GameData
{
	private Wizard mWizard1 = null, mWizard2 = null;
	private Shield mShield1 = null, mShield2 = null;

	public void registerWizards(Wizard w1, Wizard w2)
	{
		mWizard1 = w1;
		mWizard2 = w2;
	}

	public void registerShields(Shield s1, Shield s2)
	{
		mShield1 = s1;
		mShield2 = s2;
	}

	public Wizard getWizard1() { return mWizard1; }
	public Wizard getWizard2() { return mWizard2; }

	public Shield getShield1() { return mShield1; }
	public Shield getShield2() { return mShield2; }
}
