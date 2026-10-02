package br.com.socialconnect.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<ProblemDetail.FieldError> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(e -> new ProblemDetail.FieldError(
                        e.getField(),
                        e.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        boolean hasEstoqueNegativo = ex.getBindingResult().getFieldErrors().stream()
                .anyMatch(fe -> ("estoqueAtual".equals(fe.getField()) || "estoqueMinimo".equals(fe.getField()))
                        && ("EstoqueNaoNegativo".equals(fe.getCode())
                        || "Min".equals(fe.getCode())
                        || (fe.getRejectedValue() instanceof Number n && n.intValue() < 0)));

        if (hasEstoqueNegativo) {
            ProblemDetail problem = new ProblemDetail(
                    "https://socialconnect.api/errors/estoque-negativo",
                    "Estoque inválido",
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    "Operação rejeitada: o estoque não pode ser negativo.",
                    request.getDescription(false),
                    LocalDateTime.now(),
                    errors
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
        }

        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/validacao",
                "Erro de validação",
                HttpStatus.BAD_REQUEST.value(),
                "Um ou mais campos são inválidos.",
                request.getDescription(false),
                LocalDateTime.now(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String mensagem = String.format("O parâmetro '%s' com valor '%s' é inválido.", ex.getName(), ex.getValue());
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            mensagem = String.format("O parâmetro '%s' recebeu o valor inválido '%s'. Valores aceitos: %s",
                    ex.getName(), ex.getValue(), Arrays.toString(ex.getRequiredType().getEnumConstants()));
        }

        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/parametro-invalido",
                "Parâmetro inválido",
                HttpStatus.BAD_REQUEST.value(),
                mensagem,
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(EstoqueNegativoException.class)
    public ResponseEntity<ProblemDetail> handleEstoqueNegativo(
            EstoqueNegativoException ex, WebRequest request) {
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/estoque-negativo",
                "Estoque inválido",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(ProdutoNomeDuplicadoException.class)
    public ResponseEntity<ProblemDetail> handleProdutoNomeDuplicado(
            ProdutoNomeDuplicadoException ex, WebRequest request) {
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/produto-duplicado",
                "Nome de produto já cadastrado",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<ProblemDetail> handleCpfDuplicado(
            CpfDuplicadoException ex, WebRequest request) {
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/cpf-duplicado",
                "CPF já cadastrado",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handleNaoEncontrado(
            RecursoNaoEncontradoException ex, WebRequest request) {
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/nao-encontrado",
                "Recurso não encontrado",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/requisicao-invalida",
                "Requisição inválida",
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição ausente ou malformatado. Verifique os tipos de dados e campos obrigatórios.",
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/requisicao-invalida",
                "Requisição inválida",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenerico(
            Exception ex, WebRequest request) {
        log.error("Erro interno não tratado", ex);
        ProblemDetail problem = new ProblemDetail(
                "https://socialconnect.api/errors/erro-interno",
                "Erro interno do servidor",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado. Tente novamente.",
                request.getDescription(false),
                LocalDateTime.now(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
