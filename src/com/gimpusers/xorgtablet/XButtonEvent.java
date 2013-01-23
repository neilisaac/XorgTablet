package com.gimpusers.xorgtablet;

public class XButtonEvent extends XEvent {
	public XButtonEvent(int x, int y, int pressure, Button button, boolean down, boolean absolute) {
		super(absolute ? Type.MOTION_ABSOLUTE : Type.MOTION_RELATIVE, x, y, pressure, button, down);
	}
}
