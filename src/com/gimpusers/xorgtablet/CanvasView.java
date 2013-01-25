package com.gimpusers.xorgtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;

@SuppressLint("ViewConstructor")
public class CanvasView extends View implements OnSharedPreferenceChangeListener, OnGestureListener {
	final static int PRESSURE_RESOLUTION = 10000;
	
	private XorgClient xorgClient;
	private SharedPreferences settings;
	private boolean touchEnabled;
	private boolean touchAbsolute;
	private GestureDetector gesture;
	
	public CanvasView(Context context, XorgClient xorgClient) {
		super(context);
		
		// disable until networking has been configured
		setEnabled(false);
		setBackgroundColor(0xFFD0D0D0);

		settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings.registerOnSharedPreferenceChangeListener(this);
		reconfigureAcceptedInputDevices();
		reconfigureMotionSettings();

		gesture = new GestureDetector(context, this);
		
		this.xorgClient = xorgClient;
		
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
	}
	
	void reconfigureMotionSettings() {
		touchAbsolute = settings.getBoolean(SettingsActivity.KEY_PREF_TOUCH_ABSOLUTE, false);
	}
		
	void reconfigureAcceptedInputDevices() {
		touchEnabled = settings.getBoolean(SettingsActivity.KEY_PREF_TOUCH, true);
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		Toast.makeText(getContext(), String.format("%dx%d", w, h), Toast.LENGTH_SHORT).show();
		xorgClient.queue(XEvent.configuration(w, h, PRESSURE_RESOLUTION));
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
					int p = (int) (event.getPressure(ptr) * PRESSURE_RESOLUTION);
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
		
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		for (int ptr = 0; ptr < event.getPointerCount(); ptr++) {
			int x = (int) event.getX(ptr);
			int y = (int) event.getY(ptr);
			int p = (int) (event.getPressure(ptr) * PRESSURE_RESOLUTION);
			
			// Log.i("XorgTablet", String.format("Touch event logged: %f|%f, pressure %f", x, y, p));
			
			if (event.getToolType(ptr) == MotionEvent.TOOL_TYPE_STYLUS || (touchAbsolute && touchEnabled)) {
				
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
					break;
				}
			}
			
			else if (touchEnabled && !touchAbsolute) {
				return gesture.onTouchEvent(event);
			}
		}
		
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
		xorgClient.queue(XEvent.motion((int) -dx, (int) -dy, 0, false));
		return true;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		int p = (int) (event.getPressure() * PRESSURE_RESOLUTION);
		xorgClient.queue(XEvent.button(0, 0, p, XEvent.Button.BUTTON_1, true, false));
		xorgClient.queue(XEvent.button(0, 0, p, XEvent.Button.BUTTON_1, false, false));
		return true;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		int p = (int) (event.getPressure() * PRESSURE_RESOLUTION);
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
