package br.com.socialconnect.api.exception;

public class EstoqueNegativoException extends RuntimeException {

    public EstoqueNegativoException(String message) {
        super(message);
    }
}
