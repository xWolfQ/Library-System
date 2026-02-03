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

@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {

    private final BookService service;
    private final Logger log = LoggerFactory.getLogger(BookController.class);

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Book book) {
        try {
            service.create(book);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


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

    @GetMapping("/select")
    public List<BookSelectDTO> getForSelect() {
        return service.getForSelect();
    }

    @GetMapping("/top")
    public List<Book> topBorrowed(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.getTopBorrowed(limit);
    }

}
