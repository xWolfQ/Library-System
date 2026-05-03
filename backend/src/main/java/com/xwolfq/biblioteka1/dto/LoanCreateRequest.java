package com.xwolfq.biblioteka1.dto;

import java.sql.Date;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) reprezentujące żądanie utworzenia nowego wypożyczenia.
 *
 * <p>Przekazywane w ciele żądania {@code POST /api/loans} jako obiekt JSON.
 * Zawiera identyfikatory książki i czytelnika oraz planowaną datę zwrotu.</p>
 *
 * <p><strong>Uwaga:</strong> pole {@code dueDate} używa typu {@link java.sql.Date}
 * (zamiast {@link LocalDate}), co wymaga konwersji przez wywołanie
 * {@code dueDate.toLocalDate()} przed przekazaniem do warstwy serwisu.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.service.LoanService#createLoan(Long, Long, LocalDate)
 * @see com.xwolfq.biblioteka1.controller.LoanController
 */
public class LoanCreateRequest {

    /** Identyfikator książki, która ma zostać wypożyczona. */
    private Long bookId;

    /** Identyfikator czytelnika, który wypożycza książkę. */
    private Long readerId;

    /**
     * Planowana data zwrotu książki.
     * Typ {@link java.sql.Date} — przed przekazaniem do serwisu należy
     * wywołać {@code dueDate.toLocalDate()}.
     */
    private Date dueDate;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getReaderId() {
        return readerId;
    }

    public void setReaderId(Long readerId) {
        this.readerId = readerId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
