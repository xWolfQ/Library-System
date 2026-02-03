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

@RestController
@RequestMapping("/api/readers")
@CrossOrigin
public class ReaderController {

    private final ReaderService service;
    private final Logger log = LoggerFactory.getLogger(ReaderController.class);

    public ReaderController(ReaderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Reader> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Reader reader) {
        try {
            service.create(reader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


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

    @GetMapping("/select")
    public List<ReaderSelectDTO> getForSelect() {
        return service.getForSelect();
    }


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
