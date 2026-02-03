package com.xwolfq.biblioteka1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.xwolfq.biblioteka1.model.Book;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByActiveTrue();

    Optional<Book> findByIdAndActiveTrue(Long id);

    List<Book> findByActiveTrueOrderByBorrowedCopiesDesc(Pageable pageable);

}
