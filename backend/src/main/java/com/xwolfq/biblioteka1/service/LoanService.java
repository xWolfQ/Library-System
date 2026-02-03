package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.model.Book;
import com.xwolfq.biblioteka1.model.Loan;
import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.repository.BookRepository;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LoanService {

    private final LoanRepository loanRepo;
    private final BookRepository bookRepo;
    private final ReaderRepository readerRepo;

    public LoanService(
            LoanRepository loanRepo,
            BookRepository bookRepo,
            ReaderRepository readerRepo
    ) {
        this.loanRepo = loanRepo;
        this.bookRepo = bookRepo;
        this.readerRepo = readerRepo;
    }

    // ===== CREATE LOAN =====
    @Transactional
    public void createLoan(Long bookId, Long readerId, LocalDate dueDate) {

        if (bookId == null || readerId == null || dueDate == null) {
            throw new IllegalArgumentException(
                    "Książka, czytelnik i termin zwrotu są wymagane"
            );
        }

        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Książka nie istnieje"));

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Brak dostępnych egzemplarzy książki");
        }

        Reader reader = readerRepo.findById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("Czytelnik nie istnieje"));

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setReader(reader);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(dueDate);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setBorrowedCopies(book.getBorrowedCopies() + 1);

        loanRepo.save(loan);
        bookRepo.save(book);
    }

    @Transactional
    public void returnLoan(Long loanId) {

        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new IllegalStateException("Wypożyczenie nie istnieje"));

        if (loan.getDueDate() != null && loan.getReturnDate() != null) {
            throw new IllegalStateException("Wypożyczenie zostało już zakończone");
        }

        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        loanRepo.save(loan);
        bookRepo.save(book);
    }
}
