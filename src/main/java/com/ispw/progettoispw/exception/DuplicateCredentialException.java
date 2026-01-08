package com.ispw.progettoispw.exception;

public class DuplicateCredentialException extends BusinessRuleException {
    public enum Field { EMAIL, PHONE }
    private final Field field;

    public DuplicateCredentialException(Field field, String message) {
        super(message);
        this.field = field;
    }

    public Field getField() { return field; }
}