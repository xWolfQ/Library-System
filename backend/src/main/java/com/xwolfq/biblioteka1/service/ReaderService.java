// java
package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.dto.ReaderSelectDTO;
import com.xwolfq.biblioteka1.exceptions.ReaderLoanedException;
import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.repository.ReaderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis zawierający logikę biznesową związaną z zarządzaniem czytelnikami.
 *
 * <p>Pośredniczy między kontrolerem {@link com.xwolfq.biblioteka1.controller.ReaderController}
 * a repozytoriami {@link ReaderRepository} i {@link LoanRepository}.
 * Odpowiada za rejestrację, aktualizację, usuwanie i dezaktywację czytelników,
 * z uwzględnieniem reguł biznesowych takich jak walidacja danych wejściowych
 * i blokada operacji przy aktywnych wypożyczeniach.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.model.Reader
 * @see ReaderRepository
 * @see LoanRepository
 */
@Service
public class ReaderService {

    /** Repozytorium umożliwiające operacje CRUD na encji {@link Reader}. */
    private final ReaderRepository readerRepo;

    /** Repozytorium używane do sprawdzania istnienia aktywnych wypożyczeń danego czytelnika. */
    private final LoanRepository loanRepo;

    /**
     * Tworzy serwis z wstrzykniętymi zależnościami (wstrzykiwanie przez konstruktor).
     *
     * @param readerRepo repozytorium czytelników
     * @param loanRepo   repozytorium wypożyczeń
     */
    public ReaderService(ReaderRepository readerRepo, LoanRepository loanRepo) {
        this.readerRepo = readerRepo;
        this.loanRepo = loanRepo;
    }

    /**
     * Zwraca listę wszystkich aktywnych czytelników zarejestrowanych w bibliotece.
     *
     * <p>Filtruje wyniki po polu {@code active = true}, ukrywając czytelników
     * dezaktywowanych (soft-deleted).</p>
     *
     * @return lista aktywnych czytelników; pusta lista, jeśli brak zarejestrowanych
     */
    public List<Reader> getAll() {
        return readerRepo.findByActiveTrue();
    }

    /**
     * Rejestruje nowego czytelnika w systemie bibliotecznym.
     *
     * <p>Przed zapisem waliduje wymagane pola: imię, nazwisko, e-mail i numer telefonu.
     * Flaga {@code active} ustawiana jest zawsze na {@code true}.</p>
     *
     * @param reader dane nowego czytelnika do zapisania
     * @throws IllegalArgumentException gdy imię, nazwisko, e-mail lub numer telefonu
     *         jest pusty lub {@code null}
     */
    public void create(Reader reader) {
        if (reader.getFirstName() == null || reader.getFirstName().isBlank())
            throw new IllegalArgumentException("Musisz wpisać Imię czytelnika");

        if (reader.getLastName() == null || reader.getLastName().isBlank())
            throw new IllegalArgumentException("Musisz wpisać Nazwisko czytelnika");

        if (reader.getEmail() == null || reader.getEmail().isBlank())
            throw new IllegalArgumentException("Email czytelnika jest wymagany");

        if (reader.getPhoneNumber() == null || reader.getPhoneNumber().isBlank())
            throw new IllegalArgumentException("Numer telefonu czytelnika jest wymagany");

        reader.setActive(true);
        readerRepo.save(reader);
    }

    /**
     * Usuwa czytelnika z systemu poprzez soft delete ({@code active = false}).
     *
     * <p>Przed usunięciem sprawdza, czy czytelnik istnieje. Następnie weryfikuje,
     * czy nie posiada aktywnych wypożyczeń — jeśli tak, rzuca
     * {@link ReaderLoanedException}, która zostanie obsłużona przez
     * {@link com.xwolfq.biblioteka1.exceptions.RestExceptionHandler}.</p>
     *
     * <p><strong>Uwaga techniczna:</strong> w oryginalnym kodzie sprawdzenie
     * aktywnych wypożyczeń używa {@code existsByBookIdAndReturnDateIsNull(id)}
     * zamiast {@code existsByReaderIdAndReturnDateIsNull(id)} — co wygląda na
     * potencjalny błąd implementacji (sprawdzana jest zła kolumna klucza obcego).</p>
     *
     * @param id identyfikator czytelnika do usunięcia
     * @return {@code true} jeśli soft delete się powiódł;
     *         {@code false} jeśli czytelnik o podanym {@code id} nie istnieje
     * @throws ReaderLoanedException gdy czytelnik posiada aktywne wypożyczenia
     */
    public boolean delete(long id) {
        if (!readerRepo.existsById(id)) {
            return false;
        }

        // jeśli są aktywne wypożyczenia -> wyjątek (obsłuży ControllerAdvice)
        if (loanRepo.existsByBookIdAndReturnDateIsNull(id)) {
            throw new ReaderLoanedException("Nie można usunąć czytelnika: istnieją aktywne wypożyczenia");
        }

        // soft delete
        Reader reader = readerRepo.findById(id).orElse(null);
        if (reader == null) return false;
        reader.setActive(false);
        readerRepo.save(reader);
        return true;
    }

    /**
     * Zwraca uproszczoną listę aktywnych czytelników do użycia w listach wyboru (select).
     *
     * <p>Mapuje każdą aktywną encję {@link Reader} na {@link ReaderSelectDTO}
     * zawierający {@code id}, imię ({@code firstName}) i nazwisko ({@code lastName}).
     * Filtrowanie odbywa się po stronie aplikacji (nie bazy danych) — pobierani są
     * wszyscy czytelnicy, a następnie filtrowana jest aktywność.</p>
     *
     * @return lista DTO z identyfikatorem, imieniem i nazwiskiem każdego aktywnego czytelnika
     */
    public List<ReaderSelectDTO> getForSelect() {
        return readerRepo.findAll()
                .stream()
                .filter(Reader::isActive)
                .map(r -> new ReaderSelectDTO(r.getId(), r.getFirstName(), r.getLastName()))
                .collect(Collectors.toList());
    }

    /**
     * Aktualizuje dane kontaktowe istniejącego czytelnika.
     *
     * <p>Nadpisuje imię, nazwisko, e-mail i numer telefonu. Pozostałe pola
     * (np. {@code registrationDate}, {@code active}) nie są modyfikowane.</p>
     *
     * @param id      identyfikator czytelnika do zaktualizowania
     * @param newData obiekt zawierający nowe dane kontaktowe
     * @throws IllegalArgumentException gdy czytelnik o podanym {@code id} nie istnieje
     */
    public void update(long id, Reader newData) {
        Reader reader = readerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono czytelnika"));

        reader.setFirstName(newData.getFirstName());
        reader.setLastName(newData.getLastName());
        reader.setEmail(newData.getEmail());
        reader.setPhoneNumber(newData.getPhoneNumber());

        readerRepo.save(reader);
    }

    /**
     * Dezaktywuje czytelnika (soft delete) — ustawia {@code active = false}.
     *
     * <p>Operacja jest transakcyjna. Przed dezaktywacją sprawdzane jest,
     * czy czytelnik nie ma aktywnych wypożyczeń ({@code returnDate IS NULL}).
     * Jeśli ma — rzucany jest {@link ReaderLoanedException}.</p>
     *
     * @param readerId identyfikator czytelnika do dezaktywacji
     * @throws ReaderLoanedException    gdy czytelnik posiada aktywne wypożyczenia
     * @throws IllegalArgumentException gdy czytelnik o podanym {@code readerId} nie istnieje
     */
    @Transactional
    public void deactivate(Long readerId) {
        if (loanRepo.existsByReaderIdAndReturnDateIsNull(readerId)) {
            throw new ReaderLoanedException(
                    "Nie można dezaktywować czytelnika z aktywnym wypożyczeniem"
            );
        }

        Reader reader = readerRepo.findById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("Czytelnik nie istnieje"));

        reader.setActive(false);
        readerRepo.save(reader);
    }
}
