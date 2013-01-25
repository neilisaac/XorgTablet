package com.gimpusers.xorgtablet;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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
	public void onBackPressed() {
		if (getActionBar().isShowing())
			super.onBackPressed();
		else
			getActionBar().show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.show_settings:
			startActivity(new Intent(CanvasActivity.this, SettingsActivity.class));
			return true;
			
		case R.id.show_gestures:
			showGesturesDialog();
			return true;
			
		case R.id.show_about:
			showAboutDialog();
			return true;
		}
		
		return false;
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
	
	private void showGesturesDialog() {
		final Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setTitle(R.string.menu_gestures);
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setMessage(getString(R.string.gestures));
		dialog.show();
	}
	
	private void showAboutDialog() {
		final Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.app_name);
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setMessage(getString(R.string.about));
		dialog.setNeutralButton(R.string.button_website, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((getString(R.string.website)))));
			}
		});
		dialog.show();
	}
}
