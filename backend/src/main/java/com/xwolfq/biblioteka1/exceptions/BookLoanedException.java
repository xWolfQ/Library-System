package com.xwolfq.biblioteka1.exceptions;

/**
 * Wyjątek rzucany przy próbie wykonania operacji niedozwolonej na książce,
 * która posiada aktywne wypożyczenia.
 *
 * <p>Rzucany przez warstwę serwisu w sytuacjach, gdy operacja (np. dezaktywacja)
 * wymagałaby usunięcia lub zablokowania książki mającej aktualnie nierozliczone
 * wypożyczenia ({@code returnDate IS NULL}).</p>
 *
 * <p>Przechwytywany globalnie przez {@link RestExceptionHandler#handleBookLoaned},
 * który przekształca go w odpowiedź HTTP {@code 409 Conflict}.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see RestExceptionHandler
 * @see com.xwolfq.biblioteka1.service.BookService#deactivate(long)
 */
public class BookLoanedException extends RuntimeException {

    /**
     * Tworzy nowy wyjątek z podanym komunikatem błędu.
     *
     * @param message opis przyczyny wyjątku przekazywany do klienta API
     */
    public BookLoanedException(String message) {
        super(message);
    }
}
