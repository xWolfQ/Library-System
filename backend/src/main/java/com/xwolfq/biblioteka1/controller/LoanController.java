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


@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {

    private final LoanService service;
    private final LoanRepository repo;
    private final Logger log = LoggerFactory.getLogger(LoanController.class);

    public LoanController(LoanService service, LoanRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAll() {
        List<Loan> loans = repo.findAll();
        return ResponseEntity.ok(loans);
    }

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
