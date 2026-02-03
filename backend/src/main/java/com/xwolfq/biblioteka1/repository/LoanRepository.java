package com.xwolfq.biblioteka1.repository;

import com.xwolfq.biblioteka1.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);

    boolean existsByReaderIdAndReturnDateIsNull(Long readerId);

}
