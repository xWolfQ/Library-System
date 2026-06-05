package com.xwolfq.biblioteka1.controller;

import com.xwolfq.biblioteka1.dto.LoanCreateRequest;
import com.xwolfq.biblioteka1.dto.LoanReturnRequest;
import com.xwolfq.biblioteka1.model.Loan;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.service.LoanService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Kontroler REST obsługujący żądania HTTP dotyczące wypożyczeń.
 *
 * <p>Mapowany na ścieżkę bazową {@code /api/loans}. Udostępnia endpointy
 * do pobierania listy wszystkich wypożyczeń, tworzenia nowego wypożyczenia
 * oraz rejestrowania zwrotu książki.</p>
 *
 * <p>Adnotacja {@code @CrossOrigin} ogranicza CORS wyłącznie do frontendu
 * działającego na {@code http://localhost:5173}.</p>
 *
 * <p><strong>Uwaga implementacyjna:</strong> endpoint {@code GET /api/loans}
 * odwołuje się bezpośrednio do {@link LoanRepository} z pominięciem warstwy
 * serwisu — pobiera wszystkie wypożyczenia (aktywne i zakończone) bez filtrowania.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see LoanService
 * @see com.xwolfq.biblioteka1.model.Loan
 */
@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {

    /** Serwis biznesowy delegujący operacje tworzenia i zwrotu wypożyczeń. */
    private final LoanService service;

    /**
     * Repozytorium używane bezpośrednio do pobierania listy wszystkich wypożyczeń,
     * z pominięciem warstwy serwisu.
     */
    private final LoanRepository repo;

    /** Logger używany do rejestrowania ostrzeżeń i błędów na poziomie kontrolera. */
    private final Logger log = LoggerFactory.getLogger(LoanController.class);

    /**
     * Tworzy kontroler z wstrzykniętymi zależnościami (wstrzykiwanie przez konstruktor).
     *
     * @param service serwis zarządzający logiką biznesową wypożyczeń
     * @param repo    repozytorium wypożyczeń używane do bezpośredniego odczytu listy
     */
    public LoanController(LoanService service, LoanRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    /**
     * Zwraca listę wszystkich wypożyczeń (aktywnych i zakończonych).
     *
     * <p>Endpoint: {@code GET /api/loans}</p>
     * <p>Dane pobierane są bezpośrednio z repozytorium ({@code repo.findAll()})
     * bez filtrowania — lista zawiera zarówno wypożyczenia aktywne
     * ({@code returnDate = null}), jak i zakończone.</p>
     *
     * @return {@code 200 OK} z listą wszystkich encji {@link Loan} serializowaną do JSON
     */
    @GetMapping
    public ResponseEntity<List<Loan>> getAll() {
        List<Loan> loans = repo.findAll();
        return ResponseEntity.ok(loans);
    }

    /**
     * Tworzy nowe wypożyczenie książki przez czytelnika.
     *
     * <p>Endpoint: {@code POST /api/loans}</p>
     * <p>Ciało żądania: obiekt JSON z polami {@code bookId}, {@code readerId}
     * i {@code dueDate} (planowana data zwrotu).</p>
     *
     * <p>Logika biznesowa (walidacja, sprawdzenie dostępności, aktualizacja
     * liczników egzemplarzy) realizowana jest w
     * {@link LoanService#createLoan(Long, Long, java.time.LocalDate)}.</p>
     *
     * @param request dane żądania wypożyczenia deserializowane z ciała żądania
     * @return {@code 201 Created} po poprawnym utworzeniu wypożyczenia;
     *         {@code 400 Bad Request} przy błędnych lub brakujących danych;
     *         {@code 409 Conflict} gdy brak dostępnych egzemplarzy;
     *         {@code 500 Internal Server Error} przy nieoczekiwanym błędzie
     */
    @PostMapping
    public ResponseEntity<String> create(@RequestBody LoanCreateRequest request) {
        try {
            service.createLoan(
                    request.getBookId(),
                    request.getReaderId(),
                    request.getDueDate().toLocalDate()
            );
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException ex) {
            // złe dane od klienta
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            // konflikt biznesowy (np. brak dostępnych egzemplarzy)
            log.warn("Conflict creating loan for bookId={} readerId={}: {}",
                    request.getBookId(), request.getReaderId(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error creating loan", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd serwera");
        }
    }

    /**
     * Rejestruje zwrot wypożyczonej książki.
     *
     * <p>Endpoint: {@code PUT /api/loans/return}</p>
     * <p>Ciało żądania: obiekt JSON z polem {@code loanId} — identyfikatorem
     * wypożyczenia do zakończenia.</p>
     *
     * <p>Logika zwrotu (ustawienie {@code returnDate}, inkrementacja
     * {@code availableCopies}) realizowana jest w
     * {@link LoanService#returnLoan(Long)}.</p>
     *
     * @param request dane żądania zwrotu z identyfikatorem wypożyczenia
     * @return {@code 200 OK} po poprawnym zwrocie;
     *         {@code 400 Bad Request} przy błędnych danych;
     *         {@code 409 Conflict} gdy wypożyczenie nie istnieje lub zostało już zakończone;
     *         {@code 500 Internal Server Error} przy nieoczekiwanym błędzie
     */
    @PutMapping("/return")
    public ResponseEntity<String> returnLoan(@RequestBody LoanReturnRequest request) {
        try {
            service.returnLoan(request.getLoanId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            log.warn("Conflict returning loan id={}: {}", request.getLoanId(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error returning loan", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd serwera");
        }
    }
}
