package org.demo;

public class ReceivedException extends Exception {
	public ReceivedException(String message){
		super(message);
	}
	public ReceivedException(Throwable throwable){
		super(throwable);
	}
	public ReceivedException(String message ,Throwable throwable){
		super(message,throwable);
	}
}
