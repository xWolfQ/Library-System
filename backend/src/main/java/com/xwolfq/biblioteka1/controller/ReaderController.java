package com.xwolfq.biblioteka1.controller;

import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.dto.ReaderSelectDTO;
import com.xwolfq.biblioteka1.service.ReaderService;
import com.xwolfq.biblioteka1.exceptions.ReaderLoanedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Kontroler REST obsługujący żądania HTTP dotyczące czytelników.
 *
 * <p>Mapowany na ścieżkę bazową {@code /api/readers}. Udostępnia endpointy
 * do pobierania listy czytelników, rejestracji, aktualizacji danych kontaktowych,
 * usuwania (soft delete) oraz pobierania listy wyboru.</p>
 *
 * <p>Adnotacja {@code @CrossOrigin} zezwala na żądania z dowolnego źródła
 * (CORS wildcard), umożliwiając komunikację z frontendem.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see ReaderService
 * @see com.xwolfq.biblioteka1.model.Reader
 */
@RestController
@RequestMapping("/api/readers")
@CrossOrigin
public class ReaderController {

    /** Serwis biznesowy delegujący operacje na czytelnikach. */
    private final ReaderService service;

    /** Logger używany do rejestrowania ostrzeżeń przy konfliktach biznesowych. */
    private final Logger log = LoggerFactory.getLogger(ReaderController.class);

    /**
     * Tworzy kontroler z wstrzykniętą zależnością serwisu (wstrzykiwanie przez konstruktor).
     *
     * @param service serwis zarządzający logiką biznesową czytelników
     */
    public ReaderController(ReaderService service) {
        this.service = service;
    }

    /**
     * Zwraca listę wszystkich aktywnych czytelników zarejestrowanych w bibliotece.
     *
     * <p>Endpoint: {@code GET /api/readers}</p>
     *
     * @return lista aktywnych encji {@link Reader} serializowana do JSON;
     *         pusta lista jeśli brak zarejestrowanych czytelników
     */
    @GetMapping
    public List<Reader> getAll() {
        return service.getAll();
    }

    /**
     * Rejestruje nowego czytelnika w systemie bibliotecznym.
     *
     * <p>Endpoint: {@code POST /api/readers}</p>
     * <p>Ciało żądania: obiekt JSON z danymi czytelnika (imię, nazwisko, e-mail, telefon).</p>
     * <p>Walidacja wymaganych pól realizowana jest po stronie serwisu.</p>
     *
     * @param reader dane nowego czytelnika deserializowane z ciała żądania
     * @return {@code 200 OK} po poprawnej rejestracji;
     *         {@code 400 Bad Request} z komunikatem błędu przy brakujących danych
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Reader reader) {
        try {
            service.create(reader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Usuwa czytelnika z widocznej listy poprzez soft delete ({@code active = false}).
     *
     * <p>Endpoint: {@code DELETE /api/readers/{id}}</p>
     * <p>Usunięcie jest niemożliwe, gdy czytelnik ma aktywne wypożyczenia.</p>
     *
     * @param id identyfikator czytelnika do usunięcia (z URL)
     * @return {@code 204 No Content} po poprawnym usunięciu;
     *         {@code 404 Not Found} gdy czytelnik o podanym {@code id} nie istnieje;
     *         {@code 409 Conflict} gdy czytelnik ma aktywne wypożyczenia
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        try {
            boolean deleted = service.delete(id);
            return deleted
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (ReaderLoanedException ex) {
            log.warn("Próba usunięcia czytelnika id={} przerwana: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    /**
     * Zwraca uproszczoną listę aktywnych czytelników do użycia w listach wyboru (select).
     *
     * <p>Endpoint: {@code GET /api/readers/select}</p>
     * <p>Każda pozycja zawiera {@code id}, imię i nazwisko czytelnika.</p>
     *
     * @return lista {@link ReaderSelectDTO} dla wszystkich aktywnych czytelników
     */
    @GetMapping("/select")
    public List<ReaderSelectDTO> getForSelect() {
        return service.getForSelect();
    }

    /**
     * Aktualizuje dane kontaktowe istniejącego czytelnika.
     *
     * <p>Endpoint: {@code PUT /api/readers/{id}}</p>
     * <p>Ciało żądania: obiekt JSON z nowymi danymi kontaktowymi czytelnika.</p>
     * <p>Aktualizowane są: imię, nazwisko, e-mail i numer telefonu.
     * Data rejestracji i status aktywności pozostają bez zmian.</p>
     *
     * @param id     identyfikator czytelnika do zaktualizowania (z URL)
     * @param reader nowe dane kontaktowe deserializowane z ciała żądania
     * @return {@code 200 OK} po poprawnej aktualizacji;
     *         {@code 400 Bad Request} gdy czytelnik o podanym {@code id} nie istnieje
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable long id,
            @RequestBody Reader reader
    ) {
        try {
            service.update(id, reader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
