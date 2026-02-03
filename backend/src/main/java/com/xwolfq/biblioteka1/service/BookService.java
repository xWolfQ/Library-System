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

@Service
public class BookService {

    private final BookRepository bookRepo;
    private final LoanRepository loanRepo;

    public BookService(BookRepository bookRepo, LoanRepository loanRepo) {
        this.bookRepo = bookRepo;
        this.loanRepo = loanRepo;
    }

    // ===== GET ALL (TYLKO AKTYWNE) =====
    public List<Book> getAll() {
        return bookRepo.findByActiveTrue();
    }

    // ===== CREATE =====
    public Book create(Book book) {
        if (book.getBorrowedCopies() == null) book.setBorrowedCopies(0);
        book.setActive(true);
        return bookRepo.save(book);
    }

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

    // ===== TOP BORROWED (TYLKO AKTYWNE) =====
    public List<Book> getTopBorrowed(int limit) {
        if (limit <= 0) return Collections.emptyList();
        // PageRequest użyty tylko dla ograniczenia liczby wyników
        return bookRepo.findByActiveTrueOrderByBorrowedCopiesDesc(PageRequest.of(0, limit));
    }

    public List<BookSelectDTO> getForSelect() {
        return bookRepo.findByActiveTrue()
                .stream()
                .map(b -> new BookSelectDTO(b.getId(), b.getTitle()))
                .collect(Collectors.toList());
    }
}
