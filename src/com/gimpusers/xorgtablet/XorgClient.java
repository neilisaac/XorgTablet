package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import com.gimpusers.xorgtablet.XEvent.Type;

import android.content.SharedPreferences;
import android.util.Log;

// see xf86-networktablet on Github for details about the protocol


public class XorgClient implements Runnable {
	
	private LinkedBlockingQueue<XEvent> motionQueue;
	
	private InetAddress destAddress;
	private SharedPreferences preferences;
	private XEvent lastConfiguration = null;

	public XorgClient(SharedPreferences preferences) {
		this.preferences = preferences;
		motionQueue = new LinkedBlockingQueue<XEvent>();
	}
	
	public void queue(XEvent event) {
		motionQueue.add(event);
	}
	
	public boolean configureNetworking() {
		try {
			String hostName = preferences.getString(SettingsActivity.KEY_PREF_HOST, "unknown.invalid");
			destAddress = InetAddress.getByName(hostName);
			
			if (lastConfiguration != null)
				motionQueue.add(lastConfiguration);
	
		} catch (UnknownHostException e) {
			destAddress = null;
			return false;
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			while (true) {
				XEvent event = motionQueue.take();
				
				// save resolution, even if not sending it
				if (event.getType() == XEvent.Type.RESOLUTION)
					lastConfiguration = event;
				// graceful shutdown
				else if (event.getType() == Type.DISCONNECT)
					break;
				
				if (destAddress == null)		// no valid destination host
					continue;
			
				byte[] data = event.toByteArray();
				DatagramPacket pkt = new DatagramPacket(data, data.length, destAddress, 40117);
				socket.send(pkt);

				baos.reset();			
			}
		} catch (Exception e) {
			Log.e("XorgTablet", "motionQueue failed: " + e.getMessage());
		}
	}
}
