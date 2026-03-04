package com.example.hubspot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 400 - Bad Request: Validációs hibák kezelése (pl. rossz email formátum).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * HubSpot specifikus hibák kezelése (401, 403, 429).
     * Azért kezeljük egyben, hogy a HttpClientErrorException ne essen bele az Exception.class ágba.
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpClientError(HttpClientErrorException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        String message;

        if (status == HttpStatus.UNAUTHORIZED) {
            message = "Érvénytelen HubSpot API kulcs!";
        } else if (status == HttpStatus.FORBIDDEN) {
            message = "Nincs jogosultsága a kért művelethez a HubSpotban.";
        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            message = "HubSpot Rate Limit elérve. Kérjük, várjon!";
        } else {
            message = "Külső API hiba: " + ex.getStatusText();
        }

        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    /**
     * 500 - Internal Server Error: Minden egyéb váratlan hiba elkapása.
     * Ez az ág csak akkor fut le, ha a fentiek közül egyik sem illeszkedik.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllOtherErrors(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Váratlan hiba történt: " + ex.getMessage()));
    }
}