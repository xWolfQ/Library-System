package com.xwolfq.biblioteka1.repository;

import com.xwolfq.biblioteka1.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repozytorium Spring Data JPA dla encji {@link Loan}.
 *
 * <p>Rozszerza {@link JpaRepository}, dostarczając standardowe operacje CRUD
 * oraz niestandardowe zapytania sprawdzające istnienie aktywnych wypożyczeń.
 * Wypożyczenie uznaje się za aktywne, gdy pole {@code returnDate} ma wartość
 * {@code null}.</p>
 *
 * <p>Metody {@code existsBy...} są używane jako <em>guard clauses</em>
 * przed operacjami soft delete — zapobiegają dezaktywacji książki lub
 * czytelnika, jeśli istnieją powiązane nierozliczone wypożyczenia.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see Loan
 * @see com.xwolfq.biblioteka1.service.LoanService
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Sprawdza, czy istnieje aktywne wypożyczenie dla danej książki.
     *
     * <p>Wypożyczenie jest aktywne, gdy {@code returnDate IS NULL}.
     * Używana w {@link com.xwolfq.biblioteka1.service.BookService#deactivate(long)}
     * jako blokada przed dezaktywacją książki z nierozliczonymi wypożyczeniami.</p>
     *
     * @param bookId identyfikator książki do sprawdzenia
     * @return {@code true} jeśli istnieje co najmniej jedno aktywne wypożyczenie
     *         dla tej książki; {@code false} w przeciwnym razie
     */
    boolean existsByBookIdAndReturnDateIsNull(Long bookId);

    /**
     * Sprawdza, czy istnieje aktywne wypożyczenie dla danego czytelnika.
     *
     * <p>Wypożyczenie jest aktywne, gdy {@code returnDate IS NULL}.
     * Używana w {@link com.xwolfq.biblioteka1.service.ReaderService#deactivate(Long)}
     * jako blokada przed dezaktywacją czytelnika z nierozliczonymi wypożyczeniami.</p>
     *
     * @param readerId identyfikator czytelnika do sprawdzenia
     * @return {@code true} jeśli istnieje co najmniej jedno aktywne wypożyczenie
     *         dla tego czytelnika; {@code false} w przeciwnym razie
     */
    boolean existsByReaderIdAndReturnDateIsNull(Long readerId);

}
