package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class XEvent {
	
	public enum Type { MOTION_ABSOLUTE, BUTTON_ABSOLUTE, RESOLUTION, MOTION_RELATIVE, BUTTON_RELATIVE, DISCONNECT };
	public enum Button { NO_BUTTON, BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4, BUTTON_5 };
	
	private Type type;
	private int x, y, pressure;
	private Button button;
	private boolean down;

	public XEvent(Type type, int x, int y, int pressure, Button button, boolean down) {
		this.type = type;
		this.x = Math.max(x, 0);
		this.y = Math.max(y, 0);
		this.pressure = pressure;
		this.button = button;
		this.down = down;
	}
		
	public byte[] toByteArray() {
		if (type == Type.DISCONNECT)
			return null;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.write(type.ordinal());
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(pressure);
			dos.write(button.ordinal());
			dos.write(down ? 1 : 0);
		} catch (IOException e) {
			return null;
		}
		
		return baos.toByteArray();
	}
}
