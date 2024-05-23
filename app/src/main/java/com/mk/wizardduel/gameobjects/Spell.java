package com.mk.wizardduel.gameobjects;

import androidx.annotation.NonNull;

public abstract class Spell extends GameObject
{
	private Wizard mCaster;

	public Wizard getCaster() { return mCaster; }
	protected void setCaster(@NonNull Wizard caster)
	{
		mCaster = caster;
		setTint(caster.getTint());
	}
}
