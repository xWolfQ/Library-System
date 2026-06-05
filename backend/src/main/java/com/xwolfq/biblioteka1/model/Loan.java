package com.xwolfq.biblioteka1.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Encja JPA reprezentująca wypożyczenie książki przez czytelnika.
 *
 * <p>Mapowana na tabelę {@code loans} w bazie danych PostgreSQL.
 * Łączy encję {@link Book} z encją {@link Reader} w relacji
 * wiele-do-jednego po obu stronach ({@code @ManyToOne}).</p>
 *
 * <p>Wypożyczenie uznaje się za aktywne, gdy pole {@code returnDate}
 * ma wartość {@code null}. Zwrot książki polega na ustawieniu
 * {@code returnDate} na bieżącą datę — rekordu nie usuwa się fizycznie.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see Book
 * @see Reader
 * @see com.xwolfq.biblioteka1.repository.LoanRepository
 * @see com.xwolfq.biblioteka1.service.LoanService
 */
@Entity
@Table(name = "loans")
public class Loan {

    /**
     * Unikalny identyfikator wypożyczenia generowany automatycznie przez bazę danych.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data wypożyczenia książki. Pole wymagane ({@code nullable = false}).
     * Ustawiane automatycznie na bieżącą datę w momencie tworzenia wypożyczenia.
     */
    @Column(nullable = false)
    private LocalDate loanDate;

    /**
     * Planowana data zwrotu książki. Pole wymagane ({@code nullable = false}).
     * Przekazywana przez klienta w żądaniu utworzenia wypożyczenia.
     */
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * Faktyczna data zwrotu książki. Wartość {@code null} oznacza,
     * że wypożyczenie jest wciąż aktywne. Ustawiana przez
     * {@link com.xwolfq.biblioteka1.service.LoanService#returnLoan(Long)}
     * przy obsłudze zwrotu.
     */
    private LocalDate returnDate;

    /**
     * Wypożyczona książka. Relacja wieloznaczna — wiele wypożyczeń może dotyczyć
     * tej samej książki. Pole wymagane ({@code optional = false}).
     * Klucz obcy przechowywany w kolumnie {@code book_id}.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    /**
     * Czytelnik, który wypożyczył książkę. Relacja wieloznaczna — czytelnik
     * może mieć wiele wypożyczeń. Pole wymagane ({@code optional = false}).
     * Klucz obcy przechowywany w kolumnie {@code reader_id}.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "reader_id")
    private Reader reader;

    // ===== getters & setters =====

    public Long getId() {
        return id;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public Book getBook() {
        return book;
    }

    public Reader getReader() {
        return reader;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }
}
