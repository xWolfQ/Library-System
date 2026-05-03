package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.model.Book;
import com.xwolfq.biblioteka1.dto.BookSelectDTO;
import com.xwolfq.biblioteka1.repository.BookRepository;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Serwis zawierający logikę biznesową związaną z zarządzaniem książkami.
 *
 * <p>Pośredniczy między kontrolerem {@link com.xwolfq.biblioteka1.controller.BookController}
 * a repozytoriami {@link BookRepository} i {@link LoanRepository}.
 * Odpowiada za tworzenie, dezaktywację (soft delete) i pobieranie książek,
 * z uwzględnieniem reguł biznesowych takich jak blokada dezaktywacji
 * przy aktywnych wypożyczeniach.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.model.Book
 * @see BookRepository
 * @see LoanRepository
 */
@Service
public class BookService {

    /** Repozytorium umożliwiające operacje CRUD na encji {@link com.xwolfq.biblioteka1.model.Book}. */
    private final BookRepository bookRepo;

    /** Repozytorium używane do sprawdzania istnienia aktywnych wypożyczeń danej książki. */
    private final LoanRepository loanRepo;

    /**
     * Tworzy serwis z wstrzykniętymi zależnościami (wstrzykiwanie przez konstruktor).
     *
     * @param bookRepo repozytorium książek
     * @param loanRepo repozytorium wypożyczeń
     */
    public BookService(BookRepository bookRepo, LoanRepository loanRepo) {
        this.bookRepo = bookRepo;
        this.loanRepo = loanRepo;
    }

    /**
     * Zwraca listę wszystkich aktywnych książek w katalogu biblioteki.
     *
     * <p>Filtruje wyniki po polu {@code active = true}, ukrywając książki
     * dezaktywowane (soft-deleted).</p>
     *
     * @return lista aktywnych książek; pusta lista, jeśli katalog jest pusty
     */
    // ===== GET ALL (TYLKO AKTYWNE) =====
    public List<Book> getAll() {
        return bookRepo.findByActiveTrue();
    }

    /**
     * Tworzy nową książkę i zapisuje ją w bazie danych.
     *
     * <p>Jeśli pole {@code borrowedCopies} nie zostało podane, ustawiane jest
     * domyślnie na {@code 0}. Flaga {@code active} zawsze ustawiana jest na
     * {@code true}, niezależnie od wartości przekazanej przez klienta.</p>
     *
     * <p><strong>Uwaga:</strong> metoda ta jest również wywoływana przez
     * {@link com.xwolfq.biblioteka1.controller.BookController#update} jako upsert
     * (aktualizacja istniejącej książki przez nadpisanie {@code id}).</p>
     *
     * @param book obiekt książki do zapisania (bez {@code id} — nowy rekord;
     *             z {@code id} — aktualizacja istniejącego)
     * @return zapisana encja {@link com.xwolfq.biblioteka1.model.Book} z nadanym
     *         przez bazę identyfikatorem
     */
    // ===== CREATE =====
    public Book create(Book book) {
        if (book.getBorrowedCopies() == null) book.setBorrowedCopies(0);
        book.setActive(true);
        return bookRepo.save(book);
    }

    /**
     * Dezaktywuje książkę (soft delete) — ustawia {@code active = false}.
     *
     * <p>Operacja jest transakcyjna. Przed dezaktywacją sprawdzane jest,
     * czy książka nie ma aktywnych wypożyczeń ({@code returnDate IS NULL}).
     * Jeśli ma — rzucany jest {@link IllegalStateException}.</p>
     *
     * @param id identyfikator książki do dezaktywacji
     * @return {@code true} jeśli dezaktywacja się powiodła;
     *         {@code false} jeśli książka o podanym {@code id} nie istnieje
     * @throws IllegalStateException gdy książka ma aktywne wypożyczenia
     *         — dezaktywacja jest wtedy niedozwolona
     */
    // ===== SOFT DELETE =====
    @Transactional
    public boolean deactivate(long id) {

        // blokada: aktywne wypożyczenia (returnDate == null)
        if (loanRepo.existsByBookIdAndReturnDateIsNull(id)) {
            throw new IllegalStateException(
                    "Nie można dezaktywować książki z aktywnym wypożyczeniem"
            );
        }

        Book book = bookRepo.findById(id).orElse(null);
        if (book == null) return false;

        book.setActive(false);
        bookRepo.save(book);
        return true;
    }

    /**
     * Zwraca listę najpopularniejszych książek posortowaną malejąco
     * według łącznej liczby wypożyczeń ({@code borrowedCopies}).
     *
     * <p>Uwzględnia wyłącznie aktywne książki. Jeśli {@code limit} wynosi
     * {@code 0} lub jest ujemny, zwracana jest pusta lista.
     * Domyślna wartość {@code limit} w endpoincie API wynosi {@code 5}.</p>
     *
     * @param limit maksymalna liczba zwracanych pozycji rankingowych
     * @return lista co najwyżej {@code limit} aktywnych książek, posortowana
     *         od najczęściej do najrzadziej wypożyczanej
     */
    // ===== TOP BORROWED (TYLKO AKTYWNE) =====
    public List<Book> getTopBorrowed(int limit) {
        if (limit <= 0) return Collections.emptyList();
        // PageRequest użyty tylko dla ograniczenia liczby wyników
        return bookRepo.findByActiveTrueOrderByBorrowedCopiesDesc(PageRequest.of(0, limit));
    }

    /**
     * Zwraca uproszczoną listę aktywnych książek do użycia w listach wyboru (select).
     *
     * <p>Mapuje każdą aktywną encję {@link com.xwolfq.biblioteka1.model.Book}
     * na {@link BookSelectDTO} zawierający wyłącznie {@code id} i {@code title}.</p>
     *
     * @return lista DTO z identyfikatorem i tytułem każdej aktywnej książki
     */
    public List<BookSelectDTO> getForSelect() {
        return bookRepo.findByActiveTrue()
                .stream()
                .map(b -> new BookSelectDTO(b.getId(), b.getTitle()))
                .collect(Collectors.toList());
    }
}
