package com.gimpusers.xorgtablet;

public class XDisconnectEvent extends XEvent {
	public XDisconnectEvent() {
		super(Type.DISCONNECT, 0, 0, 0, Button.NO_BUTTON, false);
	}
}
