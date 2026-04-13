package com.uade.tpo.demo.exceptions;

public abstract class EcommerceException extends RuntimeException {
    protected EcommerceException(String message) {
        super(message);
    }
}
