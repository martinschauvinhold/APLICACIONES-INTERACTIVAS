package com.uade.tpo.demo.exceptions;

public class DuplicateException extends EcommerceException {
    public DuplicateException(String recurso, String campo, Object valor) {
        super(recurso + " con " + campo + " '" + valor + "' ya existe");
    }
}
