package com.remla6.app.exception;

public class InferenceFailedException extends RuntimeException {
    public InferenceFailedException(String errorMessage) {
        super(errorMessage);
    }
}
