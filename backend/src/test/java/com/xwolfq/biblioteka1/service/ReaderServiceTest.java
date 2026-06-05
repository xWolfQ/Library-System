package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.exceptions.ReaderLoanedException;
import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.repository.ReaderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla {@link ReaderService}.
 *
 * <h2>Co demonstrują te testy:</h2>
 * <ul>
 *   <li>{@code @ParameterizedTest} + {@code @MethodSource} do walidacji wielu pól jednocześnie</li>
 *   <li>Ekspozycja rzeczywistego buga w metodzie {@link ReaderService#delete(long)}</li>
 *   <li>Weryfikacja, które pola zostają zaktualizowane w metodzie {@code update()}</li>
 * </ul>
 *
 * <h2>Znany bug w kodzie produkcyjnym:</h2>
 * <p>Metoda {@code delete()} wywołuje {@code loanRepo.existsByBookIdAndReturnDateIsNull(id)}
 * zamiast {@code existsByReaderIdAndReturnDateIsNull(id)}. Skutkiem jest brak ochrony
 * przed usunięciem czytelnika, który ma aktywne wypożyczenia. Test {@code delete_readerWithActiveLoan_bugExposed()}
 * dokumentuje to niepoprawne zachowanie.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReaderService — testy jednostkowe (TDD) + ekspozycja buga")
class ReaderServiceTest {

    @Mock
    private ReaderRepository readerRepo;

    @Mock
    private LoanRepository loanRepo;

    @InjectMocks
    private ReaderService readerService;

    private Reader buildReader(String firstName, String lastName,
                               String email, String phoneNumber) {
        Reader r = new Reader();
        r.setFirstName(firstName);
        r.setLastName(lastName);
        r.setEmail(email);
        r.setPhoneNumber(phoneNumber);
        return r;
    }

    private Reader validReader() {
        return buildReader("Anna", "Nowak", "anna@biblioteka.pl", "500600700");
    }

    /**
     * Dostarcza zestawy nieprawidłowych czytelników oraz oczekiwane fragmenty komunikatu błędu.
     *
     * <p>Każdy wiersz to inny brakujący atrybut — test weryfikuje, że każdy przypadek
     * zostaje wychwycony i opisany odpowiednim komunikatem.</p>
     */
    static Stream<Arguments> invalidReaderProvider() {
        return Stream.of(
            Arguments.of(readerWithNull("firstName"), "Imię"),
            Arguments.of(readerWithBlank("firstName"), "Imię"),
            Arguments.of(readerWithNull("lastName"), "Nazwisko"),
            Arguments.of(readerWithNull("email"), "Email"),
            Arguments.of(readerWithBlank("email"), "Email"),
            Arguments.of(readerWithNull("phoneNumber"), "telefonu")
        );
    }

    /** Tworzy poprawnego czytelnika z jednym polem ustawionym na null. */
    private static Reader readerWithNull(String field) {
        Reader r = new Reader();
        r.setFirstName(field.equals("firstName") ? null : "Jan");
        r.setLastName(field.equals("lastName") ? null : "Kowalski");
        r.setEmail(field.equals("email") ? null : "jan@test.pl");
        r.setPhoneNumber(field.equals("phoneNumber") ? null : "111222333");
        return r;
    }

    /** Tworzy poprawnego czytelnika z jednym polem ustawionym na blank (same spacje). */
    private static Reader readerWithBlank(String field) {
        Reader r = new Reader();
        r.setFirstName(field.equals("firstName") ? "   " : "Jan");
        r.setLastName(field.equals("lastName") ? "   " : "Kowalski");
        r.setEmail(field.equals("email") ? "   " : "jan@test.pl");
        r.setPhoneNumber(field.equals("phoneNumber") ? "   " : "111222333");
        return r;
    }

    @Nested
    @DisplayName("create() — walidacja wymaganych pól")
    class CreateReaderValidation {

        @ParameterizedTest(name = "brakujące pole [{index}] → wyjątek z komunikatem zawierającym ''{1}''")
        @MethodSource("com.xwolfq.biblioteka1.service.ReaderServiceTest#invalidReaderProvider")
        @DisplayName("brakujące lub puste wymagane pole → IllegalArgumentException")
        void create_invalidField_throwsIllegalArgument(Reader reader, String expectedMessageFragment) {
            assertThatThrownBy(() -> readerService.create(reader))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessageFragment);

            verifyNoInteractions(readerRepo);
        }

        @Test
        @DisplayName("poprawny czytelnik → active ustawione na true przed save()")
        void create_validReader_setsActiveTrueBeforeSave() {
            Reader reader = validReader();

            readerService.create(reader);

            ArgumentCaptor<Reader> captor = ArgumentCaptor.forClass(Reader.class);
            verify(readerRepo).save(captor.capture());
            assertThat(captor.getValue().isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("delete() — ekspozycja buga z nieprawidłowym kluczem obcym")
    class DeleteReaderBug {

        /**
         * <strong>BUG DOKUMENTACYJNY</strong>
         *
         * <p>Metoda {@code delete()} powinna nie pozwalać na usunięcie czytelnika,
         * który ma aktywne wypożyczenia. Jednak zamiast sprawdzać
         * {@code existsByReaderIdAndReturnDateIsNull(id)}, wywołuje
         * {@code existsByBookIdAndReturnDateIsNull(id)} — sprawdza czy istnieje
         * <em>książka</em> o tym samym id z aktywnym wypożyczeniem, a nie czytelnik.</p>
         *
         * <p><strong>Skutek:</strong> czytelnik może zostać usunięty mimo aktywnych wypożyczeń,
         * jeśli żadna książka o tym samym numerycznym id nie ma aktywnego wypożyczenia.</p>
         *
         * <p>Ten test <em>przechodzi</em> z aktualnym kodem (dokumentuje bug),
         * ale po naprawieniu buga powinien zostać zmieniony na oczekiwanie
         * {@code ReaderLoanedException}.</p>
         */
        @Test
        @DisplayName("BUG: czytelnik z aktywnym wypożyczeniem może zostać usunięty z powodu złego klucza FK")
        void delete_readerWithActiveLoan_bugAllowsDeletionDueToWrongForeignKey() {
            long readerId = 5L;
            Reader reader = buildReader("Jan", "Testowy", "jan@test.pl", "999");
            reader.setId(readerId);

            given(readerRepo.existsById(readerId)).willReturn(true);
            given(loanRepo.existsByBookIdAndReturnDateIsNull(readerId)).willReturn(false);
            given(loanRepo.existsByReaderIdAndReturnDateIsNull(readerId)).willReturn(true);
            given(readerRepo.findById(readerId)).willReturn(Optional.of(reader));

            boolean result = readerService.delete(readerId);

            assertThat(result)
                .as("BUG: delete() zwraca true mimo aktywnych wypożyczeń czytelnika")
                .isTrue();

            ArgumentCaptor<Reader> captor = ArgumentCaptor.forClass(Reader.class);
            verify(readerRepo).save(captor.capture());
            assertThat(captor.getValue().isActive())
                .as("BUG: czytelnik został dezaktywowany mimo aktywnych wypożyczeń")
                .isFalse();

            verify(loanRepo, never()).existsByReaderIdAndReturnDateIsNull(readerId);
        }

        @Test
        @DisplayName("czytelnik nie istnieje → delete() zwraca false bez zapytań do loan")
        void delete_readerNotFound_returnsFalse() {
            given(readerRepo.existsById(999L)).willReturn(false);

            boolean result = readerService.delete(999L);

            assertThat(result).isFalse();
            verifyNoInteractions(loanRepo);
            verify(readerRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update() — aktualizacja danych kontaktowych")
    class UpdateReader {

        @Test
        @DisplayName("czytelnik nie istnieje → IllegalArgumentException")
        void update_readerNotFound_throwsIllegalArgument() {
            given(readerRepo.findById(404L)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                readerService.update(404L, validReader())
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nie znaleziono czytelnika");

            verify(readerRepo, never()).save(any());
        }

        @Test
        @DisplayName("istniejący czytelnik → wszystkie pola kontaktowe zaktualizowane")
        void update_success_updatesAllContactFields() {
            Reader existing = buildReader("Stare", "Dane", "stary@email.pl", "000000000");
            existing.setId(3L);
            Reader newData = buildReader("Anna", "Nowak", "anna@nowy.pl", "111222333");

            given(readerRepo.findById(3L)).willReturn(Optional.of(existing));

            readerService.update(3L, newData);

            ArgumentCaptor<Reader> captor = ArgumentCaptor.forClass(Reader.class);
            verify(readerRepo).save(captor.capture());

            Reader saved = captor.getValue();
            assertThat(saved.getFirstName()).isEqualTo("Anna");
            assertThat(saved.getLastName()).isEqualTo("Nowak");
            assertThat(saved.getEmail()).isEqualTo("anna@nowy.pl");
            assertThat(saved.getPhoneNumber()).isEqualTo("111222333");
            assertThat(saved.getId()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("deactivate() — poprawna implementacja z reader_id")
    class DeactivateReader {

        @Test
        @DisplayName("czytelnik ma aktywne wypożyczenie → ReaderLoanedException")
        void deactivate_withActiveLoan_throwsReaderLoanedException() {
            given(loanRepo.existsByReaderIdAndReturnDateIsNull(1L)).willReturn(true);

            assertThatThrownBy(() -> readerService.deactivate(1L))
                .isInstanceOf(ReaderLoanedException.class)
                .hasMessageContaining("aktywnym wypożyczeniem");

            verify(readerRepo, never()).save(any());
        }

        @Test
        @DisplayName("czytelnik nie istnieje → IllegalArgumentException")
        void deactivate_readerNotFound_throwsIllegalArgument() {
            given(loanRepo.existsByReaderIdAndReturnDateIsNull(88L)).willReturn(false);
            given(readerRepo.findById(88L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> readerService.deactivate(88L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Czytelnik nie istnieje");
        }

        @Test
        @DisplayName("brak aktywnych wypożyczeń → czytelnik dezaktywowany")
        void deactivate_success_setsActiveFalse() {
            Reader reader = buildReader("Maria", "Wiśniewska", "maria@lib.pl", "321654987");
            reader.setId(7L);
            given(loanRepo.existsByReaderIdAndReturnDateIsNull(7L)).willReturn(false);
            given(readerRepo.findById(7L)).willReturn(Optional.of(reader));

            readerService.deactivate(7L);

            ArgumentCaptor<Reader> captor = ArgumentCaptor.forClass(Reader.class);
            verify(readerRepo).save(captor.capture());
            assertThat(captor.getValue().isActive()).isFalse();
        }
    }

    @Test
    @DisplayName("getForSelect() filtruje nieaktywnych czytelników po stronie aplikacji (nie DB)")
    void getForSelect_returnsOnlyActiveReaders() {
        Reader active1 = buildReader("Jan", "Aktywny", "jan@lib.pl", "111");
        active1.setActive(true);
        Reader active2 = buildReader("Ewa", "Aktywna", "ewa@lib.pl", "222");
        active2.setActive(true);
        Reader inactive = buildReader("Stary", "Nieaktywny", "stary@lib.pl", "333");
        inactive.setActive(false);

        given(readerRepo.findAll()).willReturn(List.of(active1, active2, inactive));

        var result = readerService.getForSelect();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("lastName")
            .containsExactlyInAnyOrder("Aktywny", "Aktywna");
    }
}
