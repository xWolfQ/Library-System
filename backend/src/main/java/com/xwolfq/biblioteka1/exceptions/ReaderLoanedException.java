package com.xwolfq.biblioteka1.exceptions;

/**
 * Wyjątek rzucany przy próbie wykonania operacji niedozwolonej na czytelniku,
 * który posiada aktywne wypożyczenia.
 *
 * <p>Rzucany przez warstwę serwisu w sytuacjach, gdy operacja (np. usunięcie
 * lub dezaktywacja czytelnika) jest niemożliwa ze względu na nierozliczone
 * wypożyczenia ({@code returnDate IS NULL}).</p>
 *
 * <p>Przechwytywany globalnie przez {@link RestExceptionHandler#handleReaderLoaned},
 * który przekształca go w odpowiedź HTTP {@code 409 Conflict}.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see RestExceptionHandler
 * @see com.xwolfq.biblioteka1.service.ReaderService#delete(long)
 * @see com.xwolfq.biblioteka1.service.ReaderService#deactivate(Long)
 */
public class ReaderLoanedException extends RuntimeException {

    /**
     * Tworzy nowy wyjątek z podanym komunikatem błędu.
     *
     * @param message opis przyczyny wyjątku przekazywany do klienta API
     */
    public ReaderLoanedException(String message) {
        super(message);
    }
}
