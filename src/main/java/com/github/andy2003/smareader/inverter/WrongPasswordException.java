package com.github.andy2003.smareader.inverter;

public class WrongPasswordException extends LoginException {
	WrongPasswordException(String message) {
		super(message);
	}
}
