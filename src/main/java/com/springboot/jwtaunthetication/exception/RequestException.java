package com.springboot.jwtaunthetication.exception;

public class RequestException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1130372206847249109L;

	public RequestException(String message) {
        super(message);
    }
}
