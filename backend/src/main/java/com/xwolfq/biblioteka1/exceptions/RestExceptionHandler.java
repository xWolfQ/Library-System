package com.xwolfq.biblioteka1.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Globalny handler wyjątków dla całego API REST.
 *
 * <p>Klasa oznaczona {@code @ControllerAdvice} przechwytuje wyjątki rzucane
 * przez dowolny kontroler i przekształca je w ujednolicone odpowiedzi HTTP,
 * zanim trafią do klienta. Dzięki temu kontrolery nie muszą samodzielnie
 * obsługiwać wyjątków domenowych.</p>
 *
 * <p>Obsługiwane wyjątki i odpowiadające im kody HTTP:</p>
 * <ul>
 *   <li>{@link BookLoanedException} → {@code 409 Conflict}</li>
 *   <li>{@link ReaderLoanedException} → {@code 409 Conflict}</li>
 * </ul>
 *
 * <p>Każda odpowiedź zawiera komunikat błędu wzbogacony o znacznik czasowy
 * w formacie: {@code <komunikat> | Czas: <timestamp>}.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see BookLoanedException
 * @see ReaderLoanedException
 */
@ControllerAdvice
public class RestExceptionHandler {

    /** Logger używany do rejestrowania ostrzeżeń o konfliktach biznesowych. */
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Obsługuje wyjątek {@link BookLoanedException} rzucany przy próbie
     * dezaktywacji książki posiadającej aktywne wypożyczenia.
     *
     * <p>Loguje ostrzeżenie ze ścieżką żądania i zwraca odpowiedź
     * {@code 409 Conflict} z komunikatem błędu i znacznikiem czasowym.</p>
     *
     * @param ex      przechwycony wyjątek z komunikatem przyczyny błędu
     * @param request obiekt żądania HTTP — używany do pobrania ścieżki URL dla logów
     * @return odpowiedź {@code 409 Conflict} z treścią komunikatu błędu
     */
    @ExceptionHandler(BookLoanedException.class)
    public ResponseEntity<String> handleBookLoaned(BookLoanedException ex, HttpServletRequest request) {
        Date now = Date.from(Instant.now());
        String body = ex.getMessage() + " | Czas: " + now.toString();

        log.warn("BookLoanedException - path={}, status={}, message={}", request.getRequestURI(),
                HttpStatus.CONFLICT.value(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Obsługuje wyjątek {@link ReaderLoanedException} rzucany przy próbie
     * usunięcia lub dezaktywacji czytelnika posiadającego aktywne wypożyczenia.
     *
     * <p>Loguje ostrzeżenie ze ścieżką żądania i zwraca odpowiedź
     * {@code 409 Conflict} z komunikatem błędu i znacznikiem czasowym.</p>
     *
     * @param ex      przechwycony wyjątek z komunikatem przyczyny błędu
     * @param request obiekt żądania HTTP — używany do pobrania ścieżki URL dla logów
     * @return odpowiedź {@code 409 Conflict} z treścią komunikatu błędu
     */
    @ExceptionHandler(ReaderLoanedException.class)
    public ResponseEntity<String> handleReaderLoaned(ReaderLoanedException ex, HttpServletRequest request) {
        Date now = Date.from(Instant.now());
        String body = ex.getMessage() + " | Czas: " + now.toString();

        log.warn("ReaderLoanedException - path={}, status={}, message={}", request.getRequestURI(),
                HttpStatus.CONFLICT.value(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

}
