package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.model.Book;
import com.xwolfq.biblioteka1.repository.BookRepository;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla {@link BookService}.
 *
 * <p>Demonstruje technikę <strong>@ParameterizedTest</strong> do weryfikacji
 * granicznych wartości parametru {@code limit} w metodzie {@code getTopBorrowed()},
 * a także weryfikację domyślnych wartości ustawianych przez metodę {@code create()}.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService — testy jednostkowe (TDD)")
class BookServiceTest {

    @Mock
    private BookRepository bookRepo;

    @Mock
    private LoanRepository loanRepo;

    @InjectMocks
    private BookService bookService;

    private Book buildBook(long id, boolean active, Integer borrowedCopies) {
        Book b = new Book();
        b.setId(id);
        b.setTitle("Książka " + id);
        b.setAuthor("Autor " + id);
        b.setAvailableCopies(5);
        b.setBorrowedCopies(borrowedCopies);
        b.setActive(active);
        return b;
    }

    @Nested
    @DisplayName("create() — domyślne wartości i flagi")
    class CreateBook {

        @Test
        @DisplayName("borrowedCopies == null → ustawiane na 0 przed save()")
        void create_nullBorrowedCopies_defaultsToZero() {
            Book input = buildBook(0L, true, null);
            given(bookRepo.save(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

            bookService.create(input);

            ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
            verify(bookRepo).save(captor.capture());
            assertThat(captor.getValue().getBorrowedCopies()).isZero();
        }

        @Test
        @DisplayName("create() zawsze ustawia active = true, niezależnie od wejścia")
        void create_alwaysSetsActiveTrue() {
            Book input = buildBook(0L, false, 0);
            given(bookRepo.save(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

            bookService.create(input);

            ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
            verify(bookRepo).save(captor.capture());
            assertThat(captor.getValue().isActive()).isTrue();
        }

        @Test
        @DisplayName("create() zwraca encję zapisaną przez repozytorium")
        void create_returnsPersistedBook() {
            Book input = buildBook(0L, true, 3);
            Book persisted = buildBook(42L, true, 3);
            given(bookRepo.save(any(Book.class))).willReturn(persisted);

            Book result = bookService.create(input);

            assertThat(result.getId()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("deactivate() — soft delete z walidacją wypożyczeń")
    class DeactivateBook {

        @Test
        @DisplayName("książka ma aktywne wypożyczenie → IllegalStateException")
        void deactivate_withActiveLoans_throwsIllegalState() {
            given(loanRepo.existsByBookIdAndReturnDateIsNull(1L)).willReturn(true);

            assertThatThrownBy(() -> bookService.deactivate(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aktywnym wypożyczeniem");

            verify(bookRepo, never()).save(any());
        }

        @Test
        @DisplayName("książka nie istnieje w repozytorium → zwraca false")
        void deactivate_bookNotFound_returnsFalse() {
            given(loanRepo.existsByBookIdAndReturnDateIsNull(99L)).willReturn(false);
            given(bookRepo.findById(99L)).willReturn(Optional.empty());

            boolean result = bookService.deactivate(99L);

            assertThat(result).isFalse();
            verify(bookRepo, never()).save(any());
        }

        @Test
        @DisplayName("brak aktywnych wypożyczeń → active = false, zwraca true")
        void deactivate_success_setsActiveFalseAndReturnsTrue() {
            Book book = buildBook(5L, true, 2);
            given(loanRepo.existsByBookIdAndReturnDateIsNull(5L)).willReturn(false);
            given(bookRepo.findById(5L)).willReturn(Optional.of(book));

            boolean result = bookService.deactivate(5L);

            assertThat(result).isTrue();

            ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
            verify(bookRepo).save(captor.capture());
            assertThat(captor.getValue().isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("getTopBorrowed() — graniczne wartości parametru limit")
    class GetTopBorrowed {

        /**
         * Test parametryzowany: dla każdej niedodatniej wartości limit
         * metoda powinna zwrócić pustą listę BEZ odpytywania repozytorium.
         *
         * <p>To kluczowy test wydajnościowy — chroni przed zbędnym zapytaniem SQL.</p>
         */
        @ParameterizedTest(name = "limit = {0} → pusta lista, repo nie wywołane")
        @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
        @DisplayName("niedodatni limit → natychmiastowy zwrot pustej listy")
        void getTopBorrowed_nonPositiveLimit_returnsEmptyListWithoutDbCall(int limit) {
            List<Book> result = bookService.getTopBorrowed(limit);

            assertThat(result).isEmpty();
            verifyNoInteractions(bookRepo);
        }

        @Test
        @DisplayName("limit = 3 → repozytorium wywołane z PageRequest(0, 3)")
        void getTopBorrowed_positiveLimit_callsRepoWithPageRequest() {
            List<Book> expected = List.of(
                buildBook(1L, true, 20),
                buildBook(2L, true, 15),
                buildBook(3L, true, 10)
            );
            given(bookRepo.findByActiveTrueOrderByBorrowedCopiesDesc(PageRequest.of(0, 3)))
                .willReturn(expected);

            List<Book> result = bookService.getTopBorrowed(3);

            assertThat(result).hasSize(3);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("limit = 1 → zwraca jedną najpopularniejszą książkę")
        void getTopBorrowed_limitOne_returnsSingleBook() {
            Book topBook = buildBook(7L, true, 100);
            given(bookRepo.findByActiveTrueOrderByBorrowedCopiesDesc(PageRequest.of(0, 1)))
                .willReturn(List.of(topBook));

            List<Book> result = bookService.getTopBorrowed(1);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getBorrowedCopies()).isEqualTo(100);
        }
    }
}
