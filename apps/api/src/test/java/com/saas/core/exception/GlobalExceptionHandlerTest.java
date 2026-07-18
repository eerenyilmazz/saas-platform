package com.saas.core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.saas.testsupport.TestMessages;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(TestMessages.create());

    @Test
    void unexpectedExceptionsBecomeAGenericInternalErrorEnvelope() {
        var response = handler.handleUnexpectedException(new RuntimeException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().error().message()).isEqualTo("Beklenmeyen bir hata oluştu.");
    }

    /**
     * Every validation failure seen so far (missing/blank required fields) has a field-level
     * error, so fieldError has never actually been null in practice - this exercises the fallback
     * branch directly rather than leaving it permanently unreachable in tests.
     */
    @Test
    void validationFailureWithNoFieldLevelErrorFallsBackToTheGenericMessage() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        var methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
        var exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        var response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().error().message()).isEqualTo("Geçersiz istek.");
        assertThat(response.getBody().error().field()).isNull();
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String arg) {
    }
}
