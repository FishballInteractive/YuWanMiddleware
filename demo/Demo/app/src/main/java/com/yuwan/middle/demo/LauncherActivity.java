package com.yuwan.middle.demo;

import android.content.Intent;

import com.yuwan8.middleware.FlashActivity;

public class LauncherActivity extends FlashActivity {

	@Override
	public void onsplashStop() {
		Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
		startActivity(intent);

	}

}
