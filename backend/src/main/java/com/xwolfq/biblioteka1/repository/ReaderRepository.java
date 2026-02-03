package com.xwolfq.biblioteka1.repository;

import com.xwolfq.biblioteka1.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    List<Reader> findByActiveTrue();

    Optional<Reader> findByIdAndActiveTrue(Long id);
}
