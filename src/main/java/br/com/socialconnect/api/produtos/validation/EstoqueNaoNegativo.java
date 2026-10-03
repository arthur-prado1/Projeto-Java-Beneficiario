package br.com.socialconnect.api.produtos.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EstoqueNaoNegativoValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface EstoqueNaoNegativo {

    String message() default "{produto.estoque.negativo}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
