package com.gimpusers.xorgtablet;

public class XMotionEvent extends XEvent {
	public XMotionEvent(int x, int y, int pressure, boolean absolute) {
		super(absolute ? Type.MOTION_ABSOLUTE : Type.MOTION_RELATIVE, x, y, pressure, Button.NO_BUTTON, false);
	}
}
