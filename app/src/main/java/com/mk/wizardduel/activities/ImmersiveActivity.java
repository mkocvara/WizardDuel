package com.mk.wizardduel.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class ImmersiveActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
		// Configure the behavior of the hidden system bars.
		windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
		windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

		getWindow().setFlags(1024,1024);
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//mWindowInsetsController.show(WindowInsetsCompat.Type.systemBars());
	}
}
