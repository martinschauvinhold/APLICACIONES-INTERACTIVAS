package com.uade.tpo.demo.exceptions;

public class NotFoundException extends EcommerceException {
    public NotFoundException(String recurso, Object id) {
        super(recurso + " con id " + id + " no encontrado");
    }
}
