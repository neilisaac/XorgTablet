package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class XEvent {
	
	public enum Type { MOTION_ABSOLUTE, BUTTON_ABSOLUTE, RESOLUTION, MOTION_RELATIVE, BUTTON_RELATIVE, DISCONNECT };
	public enum Button { NO_BUTTON, BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4, BUTTON_5, BUTTON_6, BUTTON_7, BUTTON_8 };

	private final static int PRESSURE_RESOLUTION = 10000;

	private Type type;
	private int x, y, pressure;
	private Button button;
	private boolean down;

	public XEvent(Type type, int x, int y, int pressure, Button button, boolean down) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.pressure = pressure;
		this.button = button;
		this.down = down;
	}
	
	public static XEvent button(int x, int y, float pressure, Button button, boolean down, boolean absolute) {
		return new XEvent(absolute ? Type.BUTTON_ABSOLUTE : Type.BUTTON_RELATIVE, x, y, (int) (pressure * PRESSURE_RESOLUTION), button, down);
	}
	
	public static XEvent configuration(int width, int height) {
		return new XEvent(Type.RESOLUTION, width, height, PRESSURE_RESOLUTION, Button.NO_BUTTON, false);
	}
	
	public static XEvent disconnect() {
		return new XEvent(Type.DISCONNECT, 0, 0, 0, Button.NO_BUTTON, false);
	}
	
	public static XEvent motion(int x, int y, float pressure, boolean absolute) {
		return new XEvent(absolute ? Type.MOTION_ABSOLUTE : Type.MOTION_RELATIVE, x, y, (int) (pressure * PRESSURE_RESOLUTION), Button.NO_BUTTON, false);
	}
	
	public Type getType() {
		return type;
	}
	
	public byte[] toByteArray() {
		if (type == Type.DISCONNECT)
			return null;
		
		//Log.d("toByteArray", "type=" + type.ordinal() +  " x=" + x + " y=" + y +
		//		" pressure=" + pressure + " button=" + button.ordinal() + " down=" + down);
		
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
