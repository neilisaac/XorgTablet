package com.gimpusers.xorgtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;

@SuppressLint("ViewConstructor")
public class CanvasView extends View implements OnSharedPreferenceChangeListener, OnGestureListener {
	
	private XorgClient xorgClient;
	private SharedPreferences settings;
	private boolean touchEnabled;
	private boolean touchAbsolute;
	private GestureDetector gesture;
	private Activity activity;
	private boolean down;
	private float pressureThreshold;
	
	public CanvasView(Context context, XorgClient xorgClient) {
		super(context);
		
		activity = (Activity) context;
		this.xorgClient = xorgClient;
		
		down = false;
		
		// disable until networking has been configured
		setEnabled(false);
		
		setBackgroundColor(0xFFD0D0D0);

		settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings.registerOnSharedPreferenceChangeListener(this);
		reconfigureAcceptedInputDevices();
		reconfigureMotionSettings();
		reconfigurePressureThreshold();

		gesture = new GestureDetector(context, this);
		
		new ConfigureNetworkingTask().execute();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equals(SettingsActivity.KEY_PREF_HOST))
			new ConfigureNetworkingTask().execute();
		else if (key.equals(SettingsActivity.KEY_PREF_TOUCH))
			reconfigureAcceptedInputDevices();
		else if (key.equals(SettingsActivity.KEY_PREF_TOUCH_ABSOLUTE))
			reconfigureMotionSettings();
		else if (key.equals(SettingsActivity.KEY_PREF_PRESSURE_THRESHOLD))
			reconfigurePressureThreshold();
	}
	
	void reconfigureMotionSettings() {
		touchAbsolute = settings.getBoolean(SettingsActivity.KEY_PREF_TOUCH_ABSOLUTE, false);
	}
		
	void reconfigureAcceptedInputDevices() {
		touchEnabled = settings.getBoolean(SettingsActivity.KEY_PREF_TOUCH, true);
	}
	
	void reconfigurePressureThreshold() {
		pressureThreshold = Float.parseFloat(settings.getString(SettingsActivity.KEY_PREF_PRESSURE_THRESHOLD, "0.2"));
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		Log.i(getClass().getName(), "Setting resolution to " + w + "x" + h);
		xorgClient.queue(XEvent.configuration(w, h));
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (!isEnabled())
			return false;
		
		boolean consumedEvent = false;
		
		for (int ptr = 0; ptr < event.getPointerCount(); ptr++) {
			if (event.getToolType(ptr) == MotionEvent.TOOL_TYPE_STYLUS) {
				//Log.i("XorgTablet", String.format("Generic motion event logged: %f|%f, pressure %f", event.getX(ptr), event.getY(ptr), event.getPressure(ptr)));
				
				if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE) {
					int x = (int) event.getX(ptr);
					int y = (int) event.getY(ptr);
					float p = event.getPressure(ptr);
					xorgClient.queue(XEvent.motion(x, y, p, true));
					consumedEvent = true;
				}
			}
		}
		
		return consumedEvent;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled())
			return false;
		
		activity.getActionBar().hide();
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		// first consider a stylus and ignore other pointers if one exists
		for (int ptr = 0; ptr < event.getPointerCount(); ptr++) {
			int x = (int) event.getX(ptr);
			int y = (int) event.getY(ptr);
			float p = event.getPressure(ptr);
			
//			Log.i("XorgTablet", String.format("Touch event logged: %d|%d, pressure %d", x, y, p));
			
			if (event.getToolType(ptr) == MotionEvent.TOOL_TYPE_STYLUS) {
				
				switch (event.getActionMasked()) {
				
				case MotionEvent.ACTION_MOVE:
					xorgClient.queue(XEvent.motion(x, y, p, true));
					return true;
					
				case MotionEvent.ACTION_DOWN:
					xorgClient.queue(XEvent.button(x, y, p, XEvent.Button.BUTTON_1, true, true));
					return true;
					
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					xorgClient.queue(XEvent.button(x, y, p, XEvent.Button.BUTTON_1, false, true));
					return true;
					
				default:
					return false;
				}
			}
		}
			
		if (touchEnabled) {
			if (touchAbsolute) {
				int x = (int) event.getX();
				int y = (int) event.getY();
				float p = event.getPressure();
				
				//Log.i("XorgTablet", String.format("Touch event logged: %d|%d, pressure %f", x, y, p));
				
				if (!down && p >= pressureThreshold) {
					xorgClient.queue(XEvent.button(x, y, p, XEvent.Button.BUTTON_1, true, true));
					down = true;
				} else if (down && p < pressureThreshold) {
					xorgClient.queue(XEvent.button(x, y, p, XEvent.Button.BUTTON_1, false, true));
					down = false;
				}
			}
			
			return gesture.onTouchEvent(event);
		}
		
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
		XEvent.Button button;
		float p = e2.getPressure();
		
		switch (e2.getPointerCount()) {
		default:
		case 1:
			if (touchAbsolute) {
				int x = (int) e2.getX();
				int y = (int) e2.getY();
				xorgClient.queue(XEvent.motion(x, y, p, true));
			} else {
				xorgClient.queue(XEvent.motion((int) -dx, (int) -dy, 0, false));
			}
			break;
			
		case 2:
			if (Math.abs(dy) > Math.abs(dx)) {
				if (dy > 0)
					button = XEvent.Button.BUTTON_4;
				else
					button = XEvent.Button.BUTTON_5;
			} else {
				if (dx > 0)
					button = XEvent.Button.BUTTON_6;
				else
					button = XEvent.Button.BUTTON_7;
			}
			
			xorgClient.queue(XEvent.button(0, 0, p, button, true, false));
			xorgClient.queue(XEvent.button(0, 0, p, button, false, false));
			break;
		}
		
		return true;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		XEvent.Button button = XEvent.Button.BUTTON_1;
		float p = event.getPressure();
		xorgClient.queue(XEvent.button(0, 0, p, button, true, false));
		xorgClient.queue(XEvent.button(0, 0, p, button, false, false));
		return true;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		if (touchAbsolute)
			return;
		
		float p = event.getPressure();
		xorgClient.queue(XEvent.button(0, 0, p, XEvent.Button.BUTTON_1, true, false));
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		return false;
	}
	
	private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return xorgClient.configureNetworking();
		}
		
		protected void onPostExecute(Boolean success) {
			if (success)
				setEnabled(true);
			else {
				setEnabled(false);
				Toast.makeText(getContext(), "Unknown host name, network tablet disabled!", Toast.LENGTH_LONG).show();
			}
		}
	}
}
