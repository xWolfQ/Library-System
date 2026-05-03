package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.model.Book;
import com.xwolfq.biblioteka1.model.Loan;
import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.repository.BookRepository;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Serwis zawierający logikę biznesową związaną z zarządzaniem wypożyczeniami.
 *
 * <p>Pośredniczy między kontrolerem {@link com.xwolfq.biblioteka1.controller.LoanController}
 * a repozytoriami {@link LoanRepository}, {@link BookRepository} i {@link ReaderRepository}.
 * Odpowiada za tworzenie wypożyczeń i obsługę zwrotów, modyfikując przy okazji
 * liczniki egzemplarzy w encji {@link Book}.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.model.Loan
 * @see LoanRepository
 */
@Service
public class LoanService {

    /** Repozytorium umożliwiające operacje CRUD na encji {@link Loan}. */
    private final LoanRepository loanRepo;

    /** Repozytorium używane do pobierania i aktualizacji liczników egzemplarzy {@link Book}. */
    private final BookRepository bookRepo;

    /** Repozytorium używane do weryfikacji istnienia czytelnika przed wypożyczeniem. */
    private final ReaderRepository readerRepo;

    /**
     * Tworzy serwis z wstrzykniętymi zależnościami (wstrzykiwanie przez konstruktor).
     *
     * @param loanRepo   repozytorium wypożyczeń
     * @param bookRepo   repozytorium książek
     * @param readerRepo repozytorium czytelników
     */
    public LoanService(
            LoanRepository loanRepo,
            BookRepository bookRepo,
            ReaderRepository readerRepo
    ) {
        this.loanRepo = loanRepo;
        this.bookRepo = bookRepo;
        this.readerRepo = readerRepo;
    }

    /**
     * Tworzy nowe wypożyczenie książki przez czytelnika.
     *
     * <p>Operacja jest transakcyjna i obejmuje:</p>
     * <ol>
     *   <li>Walidację kompletności danych wejściowych.</li>
     *   <li>Weryfikację istnienia książki i czytelnika w bazie.</li>
     *   <li>Sprawdzenie dostępności egzemplarzy ({@code availableCopies > 0}).</li>
     *   <li>Utworzenie rekordu {@link Loan} z datą dzisiejszą jako {@code loanDate}.</li>
     *   <li>Dekrementację {@code availableCopies} i inkrementację {@code borrowedCopies}
     *       w encji {@link Book}.</li>
     * </ol>
     *
     * @param bookId   identyfikator wypożyczanej książki
     * @param readerId identyfikator czytelnika wypożyczającego
     * @param dueDate  planowana data zwrotu książki
     * @throws IllegalArgumentException gdy którykolwiek z parametrów jest {@code null}
     *         lub gdy książka bądź czytelnik o podanym identyfikatorze nie istnieje
     * @throws IllegalStateException    gdy brak dostępnych egzemplarzy książki
     *         ({@code availableCopies <= 0})
     */
    // ===== CREATE LOAN =====
    @Transactional
    public void createLoan(Long bookId, Long readerId, LocalDate dueDate) {

        if (bookId == null || readerId == null || dueDate == null) {
            throw new IllegalArgumentException(
                    "Książka, czytelnik i termin zwrotu są wymagane"
            );
        }

        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Książka nie istnieje"));

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Brak dostępnych egzemplarzy książki");
        }

        Reader reader = readerRepo.findById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("Czytelnik nie istnieje"));

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setReader(reader);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(dueDate);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setBorrowedCopies(book.getBorrowedCopies() + 1);

        loanRepo.save(loan);
        bookRepo.save(book);
    }

    /**
     * Obsługuje zwrot wypożyczonej książki.
     *
     * <p>Operacja jest transakcyjna i obejmuje:</p>
     * <ol>
     *   <li>Weryfikację istnienia wypożyczenia o podanym {@code loanId}.</li>
     *   <li>Sprawdzenie, czy wypożyczenie nie zostało już zakończone
     *       ({@code returnDate != null}).</li>
     *   <li>Ustawienie {@code returnDate} na bieżącą datę.</li>
     *   <li>Inkrementację {@code availableCopies} w powiązanej encji {@link Book}.</li>
     * </ol>
     *
     * <p><strong>Uwaga:</strong> przy zwrocie nie jest aktualizowane pole
     * {@code borrowedCopies} — licznik wypożyczeń ogólnych pozostaje bez zmian,
     * co pozwala na historyczne rankingi popularności.</p>
     *
     * @param loanId identyfikator wypożyczenia do zakończenia
     * @throws IllegalStateException gdy wypożyczenie o podanym {@code loanId} nie istnieje
     *         lub zostało już wcześniej zakończone ({@code returnDate != null})
     */
    @Transactional
    public void returnLoan(Long loanId) {

        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new IllegalStateException("Wypożyczenie nie istnieje"));

        if (loan.getDueDate() != null && loan.getReturnDate() != null) {
            throw new IllegalStateException("Wypożyczenie zostało już zakończone");
        }

        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        loanRepo.save(loan);
        bookRepo.save(book);
    }
}
