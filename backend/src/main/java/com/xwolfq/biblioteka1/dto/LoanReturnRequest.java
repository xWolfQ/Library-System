package com.xwolfq.biblioteka1.dto;

/**
 * DTO (Data Transfer Object) reprezentujące żądanie zwrotu wypożyczonej książki.
 *
 * <p>Przekazywane w ciele żądania {@code PUT /api/loans/return} jako obiekt JSON.
 * Zawiera wyłącznie identyfikator wypożyczenia, które ma zostać zakończone.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.service.LoanService#returnLoan(Long)
 * @see com.xwolfq.biblioteka1.controller.LoanController
 */
public class LoanReturnRequest {

    /** Identyfikator wypożyczenia, które czytelnik chce zwrócić. */
    private long loanId;

    public long getLoanId() {
        return loanId;
    }

    public void setLoanId(long loanId) {
        this.loanId = loanId;
    }
}
