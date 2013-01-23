package com.gimpusers.xorgtablet;

public class XConfigurationEvent extends XEvent {
	public XConfigurationEvent(int width, int height, int pressure) {
		super(Type.RESOLUTION, width, height, pressure, Button.NO_BUTTON, false);
	}
}
