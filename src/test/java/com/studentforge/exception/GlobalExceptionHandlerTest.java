package com.studentforge.exception;

import com.studentforge.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MapBindingResult;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_ShouldReturn404() {
        var ex = new ResourceNotFoundException("User not found");
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("User not found");
    }

    @Test
    void handleBadRequest_ShouldReturn400() {
        var ex = new BadRequestException("Bad request");
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void handleIllegalArgument_ShouldReturn400() {
        var ex = new IllegalArgumentException("Illegal argument");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void handleGeneral_ShouldReturn500() {
        var ex = new RuntimeException("Server error");
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().status()).isEqualTo(500);
    }

    @Test
    void handleBadCredentials_ShouldReturn401() {
        var ex = new org.springframework.security.authentication.BadCredentialsException("Bad credentials");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().status()).isEqualTo(401);
    }

    @Test
    void handleValidation_ShouldReturn400() {
        var bindingResult = new org.springframework.validation.MapBindingResult(
                new java.util.HashMap<>(), "object");
        bindingResult.rejectValue("email", "error", "Некорректный email");
        var ex = new org.springframework.web.bind.MethodArgumentNotValidException(
                null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("email");
    }
}