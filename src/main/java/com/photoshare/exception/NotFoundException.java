package com.photoshare.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}