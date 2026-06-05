package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.model.Book;
import com.xwolfq.biblioteka1.model.Loan;
import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.repository.BookRepository;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.repository.ReaderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla {@link LoanService}.
 *
 * <p>Wszystkie zależności są mockowane — testy nie wymagają bazy danych.
 * Struktura: Given / When / Then (podejście BDD).</p>
 *
 * <p><strong>Cykl TDD:</strong></p>
 * <ol>
 *   <li>RED  — napisanie testu przed implementacją (test failuje)</li>
 *   <li>GREEN — implementacja minimalna, aby test przeszedł</li>
 *   <li>REFACTOR — porządkowanie kodu bez zmiany zachowania</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService — testy jednostkowe (TDD)")
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepo;

    @Mock
    private BookRepository bookRepo;

    @Mock
    private ReaderRepository readerRepo;

    @InjectMocks
    private LoanService loanService;

    /** Tworzy książkę z podaną liczbą dostępnych i wypożyczonych egzemplarzy. */
    private Book buildBook(long id, int available, int borrowed) {
        Book b = new Book();
        b.setId(id);
        b.setTitle("Testowa książka #" + id);
        b.setAvailableCopies(available);
        b.setBorrowedCopies(borrowed);
        b.setActive(true);
        return b;
    }

    /** Tworzy czytelnika z podanym id. */
    private Reader buildReader(long id) {
        Reader r = new Reader();
        r.setId(id);
        r.setFirstName("Jan");
        r.setLastName("Kowalski");
        r.setEmail("jan@test.pl");
        r.setPhoneNumber("123456789");
        r.setActive(true);
        return r;
    }

    /** Tworzy aktywne wypożyczenie (bez returnDate). */
    private Loan buildActiveLoan(long id, Book book, LocalDate dueDate) {
        Loan loan = new Loan();
        loan.setId(id);
        loan.setBook(book);
        loan.setReader(buildReader(10L));
        loan.setLoanDate(LocalDate.now().minusDays(7));
        loan.setDueDate(dueDate);
        return loan;
    }

    @Nested
    @DisplayName("createLoan() — walidacja null-owych parametrów")
    class CreateLoanNullValidation {

        @Test
        @DisplayName("bookId == null → IllegalArgumentException")
        void createLoan_nullBookId_throwsIllegalArgument() {
            assertThatThrownBy(() ->
                loanService.createLoan(null, 2L, LocalDate.now().plusDays(14))
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("wymagane");

            verifyNoInteractions(bookRepo, readerRepo, loanRepo);
        }

        @Test
        @DisplayName("readerId == null → IllegalArgumentException")
        void createLoan_nullReaderId_throwsIllegalArgument() {
            assertThatThrownBy(() ->
                loanService.createLoan(1L, null, LocalDate.now().plusDays(14))
            )
            .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(bookRepo, readerRepo, loanRepo);
        }

        @Test
        @DisplayName("dueDate == null → IllegalArgumentException")
        void createLoan_nullDueDate_throwsIllegalArgument() {
            assertThatThrownBy(() ->
                loanService.createLoan(1L, 2L, null)
            )
            .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(bookRepo, readerRepo, loanRepo);
        }
    }

    @Nested
    @DisplayName("createLoan() — walidacja encji")
    class CreateLoanEntityValidation {

        @Test
        @DisplayName("książka nie istnieje → IllegalArgumentException z komunikatem")
        void createLoan_bookNotFound_throwsIllegalArgument() {
            given(bookRepo.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                loanService.createLoan(99L, 2L, LocalDate.now().plusDays(14))
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Książka nie istnieje");

            verifyNoInteractions(readerRepo, loanRepo);
        }

        @Test
        @DisplayName("brak dostępnych egzemplarzy → IllegalStateException")
        void createLoan_noCopiesAvailable_throwsIllegalState() {
            Book book = buildBook(1L, 0, 10);
            given(bookRepo.findById(1L)).willReturn(Optional.of(book));

            assertThatThrownBy(() ->
                loanService.createLoan(1L, 2L, LocalDate.now().plusDays(14))
            )
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Brak dostępnych egzemplarzy");

            verifyNoInteractions(readerRepo, loanRepo);
        }

        @Test
        @DisplayName("czytelnik nie istnieje → IllegalArgumentException")
        void createLoan_readerNotFound_throwsIllegalArgument() {
            Book book = buildBook(1L, 2, 3);
            given(bookRepo.findById(1L)).willReturn(Optional.of(book));
            given(readerRepo.findById(77L)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                loanService.createLoan(1L, 77L, LocalDate.now().plusDays(14))
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Czytelnik nie istnieje");

            verifyNoInteractions(loanRepo);
        }
    }

    @Nested
    @DisplayName("createLoan() — ścieżka sukcesu")
    class CreateLoanSuccess {

        @Test
        @DisplayName("poprawne dane → loan zapisany, liczniki zaktualizowane")
        void createLoan_success_savesLoanAndUpdatesCounters() {
            Book book = buildBook(1L, 3, 5);
            Reader reader = buildReader(2L);
            LocalDate dueDate = LocalDate.now().plusDays(14);

            given(bookRepo.findById(1L)).willReturn(Optional.of(book));
            given(readerRepo.findById(2L)).willReturn(Optional.of(reader));

            loanService.createLoan(1L, 2L, dueDate);

            ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanRepo).save(loanCaptor.capture());

            Loan savedLoan = loanCaptor.getValue();
            assertThat(savedLoan.getBook()).isEqualTo(book);
            assertThat(savedLoan.getReader()).isEqualTo(reader);
            assertThat(savedLoan.getLoanDate()).isEqualTo(LocalDate.now());
            assertThat(savedLoan.getDueDate()).isEqualTo(dueDate);
            assertThat(savedLoan.getReturnDate()).isNull();

            assertThat(book.getAvailableCopies()).isEqualTo(2);
            assertThat(book.getBorrowedCopies()).isEqualTo(6);

            verify(bookRepo).save(book);
        }
    }

    @Nested
    @DisplayName("returnLoan() — scenariusze błędów")
    class ReturnLoanErrors {

        @Test
        @DisplayName("wypożyczenie nie istnieje → IllegalStateException")
        void returnLoan_loanNotFound_throwsIllegalState() {
            given(loanRepo.findById(404L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.returnLoan(404L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Wypożyczenie nie istnieje");

            verifyNoInteractions(bookRepo);
        }

        @Test
        @DisplayName("wypożyczenie już zwrócone → IllegalStateException")
        void returnLoan_alreadyReturned_throwsIllegalState() {
            Book book = buildBook(1L, 2, 8);
            Loan loan = buildActiveLoan(1L, book, LocalDate.now().plusDays(7));
            loan.setReturnDate(LocalDate.now().minusDays(1));

            given(loanRepo.findById(1L)).willReturn(Optional.of(loan));

            assertThatThrownBy(() -> loanService.returnLoan(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Wypożyczenie zostało już zakończone");

            verify(loanRepo, never()).save(any());
            verify(bookRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("returnLoan() — ścieżka sukcesu")
    class ReturnLoanSuccess {

        @Test
        @DisplayName("aktywne wypożyczenie → returnDate ustawione, availableCopies +1")
        void returnLoan_success_setsReturnDateAndIncrementsAvailable() {
            Book book = buildBook(1L, 1, 4);
            Loan loan = buildActiveLoan(10L, book, LocalDate.now().plusDays(3));

            given(loanRepo.findById(10L)).willReturn(Optional.of(loan));

            loanService.returnLoan(10L);

            ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanRepo).save(loanCaptor.capture());
            assertThat(loanCaptor.getValue().getReturnDate()).isEqualTo(LocalDate.now());

            assertThat(book.getAvailableCopies()).isEqualTo(2);
            assertThat(book.getBorrowedCopies()).isEqualTo(4);

            verify(bookRepo).save(book);
        }
    }
}
