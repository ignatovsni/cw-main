package com.cwsni.world;

public class CwException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CwException(String msg) {
		super(msg);
	}

	public CwException(String msg, Exception e) {
		super(msg, e);
	}

}
