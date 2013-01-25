package com.gimpusers.xorgtablet;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

public class CanvasActivity extends Activity {
	CanvasView canvas;
	SharedPreferences prefs;
	XorgClient xorgClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.tablet_preferences, false);
		
		new Thread(xorgClient = new XorgClient(PreferenceManager.getDefaultSharedPreferences(this))).start();

		setContentView(new CanvasView(this, xorgClient));
	}

	@Override
	protected void onDestroy() {
		xorgClient.queue(XEvent.disconnect());
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_canvas, menu);
		return true;
	}

	public void showAbout(MenuItem item) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(("https://github.com/rfc2822/XorgTablet"))));
	}
	
	public void showSettings(MenuItem item) {
		startActivity(new Intent(CanvasActivity.this, SettingsActivity.class));
	}
}
