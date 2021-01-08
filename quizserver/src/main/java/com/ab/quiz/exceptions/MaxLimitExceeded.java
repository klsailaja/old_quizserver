package com.ab.quiz.exceptions;

public class MaxLimitExceeded extends Exception {
	private static final long serialVersionUID = 1L;

	public MaxLimitExceeded(String message) {
		super(message);
	}
}
