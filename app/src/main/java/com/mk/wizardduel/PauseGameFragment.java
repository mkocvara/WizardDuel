package com.mk.wizardduel;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.mk.wizardduel.utils.AnimHandler;
import com.mk.wizardduel.utils.SimplePreferences;
import com.mk.wizardduel.views.ToggleButtonDual;

public class PauseGameFragment extends Fragment
{
	private Button mMainMenuBtn, mResumeBtn;
	private ToggleButtonDual mTglSound, mTglAnim;
	private View mRootView;
	private SimplePreferences mPreferences;

	private AnimHandler mAnimHandler;

	private Runnable mOnMainMenuButtonPressed, mOnResumeButtonPressed;

	public PauseGameFragment()
	{
		super(R.layout.fragment_pause_game);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
									 Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_pause_game, container, false);

		init();

		return mRootView;
	}

	private void init()
	{
		mPreferences = SimplePreferences.get();

		mMainMenuBtn = mRootView.findViewById(R.id.pause_btn_mainmenu);
		mMainMenuBtn.setOnClickListener(v -> { if (mOnMainMenuButtonPressed != null) mOnMainMenuButtonPressed.run(); });

		mResumeBtn = mRootView.findViewById(R.id.pause_btn_resume);
		mResumeBtn.setOnClickListener(v -> { if (mOnResumeButtonPressed != null) mOnResumeButtonPressed.run(); });

		mTglSound = mRootView.findViewById(R.id.pause_tglbtn_sound);
		mTglAnim = mRootView.findViewById(R.id.pause_tglbtn_anim);

		mTglSound.setChecked(mPreferences.getPrefSound());
		mTglAnim.setChecked(mPreferences.getPrefAnim());

		mTglSound.setOnCheckedChangeListener((btn, checked) -> {
			mPreferences.savePrefSound(checked);
			if (mAnimHandler.areAnimsPlaying())
			{
				mAnimHandler.stop();
				mAnimHandler.start();
			}
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

	public void setMainMenuButtonCallback(Runnable onMainMenuButtonClicked)
	{
		mOnMainMenuButtonPressed = onMainMenuButtonClicked;
	}

	public void setResumeButtonClicked(Runnable onResumeButtonClicked)
	{
		mOnResumeButtonPressed = onResumeButtonClicked;
	}

	private void addAnimsToHandler()
	{
		ImageView heading = mRootView.findViewById(R.id.pause_img_header);
		AnimationDrawable anim = (AnimationDrawable)heading.getDrawable();
		if (anim != null) mAnimHandler.addAnim(anim);

		anim = (AnimationDrawable) mResumeBtn.getBackground();
		if (anim != null) mAnimHandler.addAnim(anim);

		anim = (AnimationDrawable) mMainMenuBtn.getBackground();
		if (anim != null) mAnimHandler.addAnim(anim);

		anim = (AnimationDrawable) mMainMenuBtn.getBackground();
		if (anim != null) mAnimHandler.addAnim(anim);

		StateListDrawable sld = (StateListDrawable)mTglSound.getBackground();
		mAnimHandler.addStateListDrawableAnims(sld);

		sld = (StateListDrawable)mTglAnim.getBackground();
		mAnimHandler.addStateListDrawableAnims(sld);
	}
}