package com.cwsni.world;

import java.io.IOException;

public class CwException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CwException(String msg) {
		super(msg);
	}

	public CwException(String msg, IOException e) {
		super(msg, e);
	}

}
