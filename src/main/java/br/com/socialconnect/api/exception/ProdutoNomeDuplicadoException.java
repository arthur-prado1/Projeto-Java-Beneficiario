package br.com.socialconnect.api.exception;

public class ProdutoNomeDuplicadoException extends RuntimeException {

    public ProdutoNomeDuplicadoException(String nome) {
        super("Já existe um produto cadastrado com o nome: " + nome);
    }
}
