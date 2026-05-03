package com.xwolfq.biblioteka1.controller;

import com.xwolfq.biblioteka1.model.Book;
import com.xwolfq.biblioteka1.dto.BookSelectDTO;
import com.xwolfq.biblioteka1.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Kontroler REST obsługujący żądania HTTP dotyczące książek.
 *
 * <p>Mapowany na ścieżkę bazową {@code /api/books}. Udostępnia endpointy
 * do pobierania katalogu, tworzenia, aktualizacji i dezaktywacji książek
 * oraz do pobierania rankingu i listy wyboru.</p>
 *
 * <p>Adnotacja {@code @CrossOrigin} zezwala na żądania z dowolnego źródła
 * (CORS wildcard), co umożliwia komunikację z frontendem działającym
 * na innym porcie (np. {@code localhost:5173}).</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see BookService
 * @see com.xwolfq.biblioteka1.model.Book
 */
@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {

    /** Serwis biznesowy delegujący operacje na książkach. */
    private final BookService service;

    /** Logger używany do rejestrowania błędów i ostrzeżeń na poziomie kontrolera. */
    private final Logger log = LoggerFactory.getLogger(BookController.class);

    /**
     * Tworzy kontroler z wstrzykniętą zależnością serwisu (wstrzykiwanie przez konstruktor).
     *
     * @param service serwis zarządzający logiką biznesową książek
     */
    public BookController(BookService service) {
        this.service = service;
    }

    /**
     * Zwraca listę wszystkich aktywnych książek w katalogu biblioteki.
     *
     * <p>Endpoint: {@code GET /api/books}</p>
     *
     * @return lista aktywnych encji {@link Book} serializowana do JSON;
     *         pusta lista jeśli katalog jest pusty
     */
    @GetMapping
    public List<Book> getAll() {
        return service.getAll();
    }

    /**
     * Tworzy nową książkę w katalogu biblioteki.
     *
     * <p>Endpoint: {@code POST /api/books}</p>
     * <p>Ciało żądania: obiekt JSON z danymi książki (tytuł, autor, ISBN, itp.).</p>
     *
     * @param book dane nowej książki deserializowane z ciała żądania
     * @return {@code 200 OK} po poprawnym zapisie;
     *         {@code 400 Bad Request} z komunikatem błędu przy nieprawidłowych danych
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Book book) {
        try {
            service.create(book);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Aktualizuje dane istniejącej książki.
     *
     * <p>Endpoint: {@code PUT /api/books/{id}}</p>
     * <p>Ciało żądania: pełny obiekt JSON z nowymi danymi książki.</p>
     *
     * <p><strong>Uwaga implementacyjna:</strong> aktualizacja realizowana jest
     * przez wywołanie {@link BookService#create(Book)} z ustawionym {@code id}
     * (podejście upsert). Metoda ta zawsze ustawia {@code active = true}
     * i {@code borrowedCopies = 0} jeśli null — co jest efektem ubocznym
     * tej nieortodoksyjnej implementacji.</p>
     *
     * @param id   identyfikator książki do zaktualizowania (z URL)
     * @param book nowe dane książki deserializowane z ciała żądania
     * @return {@code 200 OK} po poprawnej aktualizacji;
     *         {@code 400 Bad Request} gdy tytuł jest pusty lub dane są błędne;
     *         {@code 500 Internal Server Error} przy nieoczekiwanym błędzie serwera
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable long id,
            @RequestBody Book book
    ) {
        try {
            // prosta walidacja
            if (book.getTitle() == null || book.getTitle().isBlank()) {
                return ResponseEntity.badRequest().body("Tytuł książki jest wymagany");
            }
            // nie mamy metody update w serwisie - zapisujemy jako save nadpisując id
            book.setId(id);
            service.create(book); // service.create ustawia borrowedCopies i active; używamy jako upsert
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Błąd przy aktualizacji książki id={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd serwera");
        }
    }

    /**
     * Dezaktywuje książkę (soft delete) — usuwa ją z widocznego katalogu.
     *
     * <p>Endpoint: {@code DELETE /api/books/{id}}</p>
     * <p>Dezaktywacja jest niemożliwa, gdy książka ma aktywne wypożyczenia.</p>
     *
     * @param id identyfikator książki do dezaktywacji (z URL)
     * @return {@code 204 No Content} po poprawnej dezaktywacji;
     *         {@code 404 Not Found} gdy książka o podanym {@code id} nie istnieje;
     *         {@code 409 Conflict} gdy książka ma aktywne wypożyczenia
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            boolean deactivated = service.deactivate(id);
            return deactivated
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            log.warn("Próba usunięcia/dezaktywacji książki id={} przerwana: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    /**
     * Zwraca uproszczoną listę aktywnych książek do użycia w listach wyboru (select).
     *
     * <p>Endpoint: {@code GET /api/books/select}</p>
     * <p>Każda pozycja zawiera wyłącznie {@code id} i {@code title} książki.</p>
     *
     * @return lista {@link BookSelectDTO} dla wszystkich aktywnych książek
     */
    @GetMapping("/select")
    public List<BookSelectDTO> getForSelect() {
        return service.getForSelect();
    }

    /**
     * Zwraca ranking aktywnych książek posortowany malejąco według liczby wypożyczeń.
     *
     * <p>Endpoint: {@code GET /api/books/top?limit=N}</p>
     * <p>Parametr {@code limit} jest opcjonalny — domyślna wartość to {@code 5}.</p>
     *
     * @param limit maksymalna liczba pozycji w rankingu (domyślnie {@code 5})
     * @return lista co najwyżej {@code limit} aktywnych książek, od najczęściej
     *         do najrzadziej wypożyczanej
     */
    @GetMapping("/top")
    public List<Book> topBorrowed(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.getTopBorrowed(limit);
    }

}
