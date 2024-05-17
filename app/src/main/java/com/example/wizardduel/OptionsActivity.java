package com.example.wizardduel;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Method;

public class OptionsActivity extends AppCompatActivity implements View.OnClickListener
{
	private Button mBtnBack;
	private ToggleButtonDual mTglSound, mTglAnim;
	private AnimHandler mAnimHandler;

	private boolean animOption = true, soundOption = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);

		mBtnBack = findViewById(R.id.options_btn_back);
		mTglSound = findViewById(R.id.options_tglbtn_sound);
		mTglAnim = findViewById(R.id.options_tglbtn_anim);

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
}