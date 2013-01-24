package com.gimpusers.xorgtablet;

public class XButtonEvent extends XEvent {
	public XButtonEvent(int x, int y, int pressure, Button button, boolean down, boolean absolute) {
		super(absolute ? Type.BUTTON_ABSOLUTE : Type.BUTTON_RELATIVE, x, y, pressure, button, down);
	}
}
