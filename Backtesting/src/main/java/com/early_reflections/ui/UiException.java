package com.early_reflections.ui;

public class UiException extends RuntimeException {

    public UiException(String msg) {
        super(msg);
    }

    public UiException(String msg, Exception e) {
        super(msg, e);
    }
}
