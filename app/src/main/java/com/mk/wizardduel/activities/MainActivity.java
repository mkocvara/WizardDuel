package com.mk.wizardduel.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mk.wizardduel.R;
import com.mk.wizardduel.utils.AnimHandler;

public class MainActivity extends ImmersiveActivity implements View.OnClickListener
{
	private Button mPlayBtn, mOptionsBtn;
	private AnimHandler mAnimHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPlayBtn = findViewById(R.id.main_btn_play);
		mOptionsBtn = findViewById(R.id.main_btn_options);

		mAnimHandler = new AnimHandler(getLifecycle(), true);
		getLifecycle().addObserver(mAnimHandler);
		addAnimsToHandler();
	}

	@Override
	public void onClick(View view)
	{
		Intent intent = null;
		if (view == mPlayBtn)
		{
			//intent = new Intent(this, PlayerSetupActivity.class);
			intent = new Intent(this, GameActivity.class);
		}
		else if (view == mOptionsBtn)
		{
			intent = new Intent(this, OptionsActivity.class);
		}
		else
		{
			Log.w("DEBUG_LOG", "Unhandled button was clicked in MainActivity.");
			return;
		}

		if (intent != null) startActivity(intent);
	}

	protected void addAnimsToHandler()
	{
		ImageView title = findViewById(R.id.main_img_title);
		AnimationDrawable anim = (AnimationDrawable)title.getDrawable();
		if (anim != null) mAnimHandler.addAnim(anim);

		LinearLayout btnLayout = findViewById(R.id.main_layout_buttons);
		int numBtns = btnLayout.getChildCount();
		for (int i = 0; i < numBtns; i++)
		{
			anim = (AnimationDrawable) btnLayout.getChildAt(i).getBackground();
			if (anim != null) mAnimHandler.addAnim(anim);
		}
	}
}