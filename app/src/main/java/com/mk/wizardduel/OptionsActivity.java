package com.mk.wizardduel;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener
{
	private Button mBtnBack;
	private ToggleButtonDual mTglSound, mTglAnim;
	private AnimHandler mAnimHandler;
	private SimplePreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);

		mPreferences = SimplePreferences.get();

		mBtnBack = findViewById(R.id.options_btn_back);
		mTglSound = findViewById(R.id.options_tglbtn_sound);
		mTglAnim = findViewById(R.id.options_tglbtn_anim);

		mTglSound.setChecked(mPreferences.getPrefSound());
		mTglAnim.setChecked(mPreferences.getPrefAnim());

		mTglSound.setOnCheckedChangeListener((btn, checked) -> {
			mPreferences.savePrefSound(checked);
			syncAnims();
		});
		mTglAnim.setOnCheckedChangeListener((btn, checked) -> {
			mPreferences.savePrefAnim(checked);

			if (checked)
				mAnimHandler.start();
			else
				mAnimHandler.stop();
		});

		mAnimHandler = new AnimHandler(getLifecycle(), true);
		getLifecycle().addObserver(mAnimHandler);
		addAnimsToHandler();

	}

	@Override
	public void onClick(View view)
	{
		if (view == mBtnBack)
			finish();
	}

	private void addAnimsToHandler()
	{
		AnimationDrawable anim = (AnimationDrawable)mBtnBack.getBackground();
		mAnimHandler.addAnim(anim);

		StateListDrawable sld = (StateListDrawable)mTglSound.getBackground();
		mAnimHandler.addStateListDrawableAnims(sld);

		sld = (StateListDrawable)mTglAnim.getBackground();
		mAnimHandler.addStateListDrawableAnims(sld);
	}

	private void syncAnims()
	{
		// A slightly hacky solution to syncing animations when a toggle button is toggled
		// (thus resetting its anim delay) on the options screen.

		if (!mAnimHandler.areAnimsPlaying())
			return;

		mAnimHandler.stop();
		mAnimHandler.start();
	}
}