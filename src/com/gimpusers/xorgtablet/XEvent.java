package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class XEvent {
	
	public enum Type { MOTION_ABSOLUTE, BUTTON_ABSOLUTE, RESOLUTION, MOTION_RELATIVE, BUTTON_RELATIVE, DISCONNECT };
	public enum Button { NO_BUTTON, BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4, BUTTON_5 };
	
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
	
	public static XEvent button(int x, int y, int pressure, Button button, boolean down, boolean absolute) {
		return new XEvent(absolute ? Type.BUTTON_ABSOLUTE : Type.BUTTON_RELATIVE, x, y, pressure, button, down);
	}
	
	public static XEvent configuration(int width, int height, int pressure) {
		return new XEvent(Type.RESOLUTION, width, height, pressure, Button.NO_BUTTON, false);
	}
	
	public static XEvent disconnect() {
		return new XEvent(Type.DISCONNECT, 0, 0, 0, Button.NO_BUTTON, false);
	}
	
	public static XEvent motion(int x, int y, int pressure, boolean absolute) {
		return new XEvent(absolute ? Type.MOTION_ABSOLUTE : Type.MOTION_RELATIVE, x, y, pressure, Button.NO_BUTTON, false);
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
