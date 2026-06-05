package com.xwolfq.biblioteka1.repository;

import com.xwolfq.biblioteka1.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repozytorium Spring Data JPA dla encji {@link Reader}.
 *
 * <p>Rozszerza {@link JpaRepository}, dostarczając standardowe operacje CRUD
 * oraz niestandardowe zapytania generowane przez Spring Data na podstawie
 * nazw metod (<em>query derivation</em>).</p>
 *
 * <p>Metody filtrujące po {@code active = true} realizują mechanizm
 * <em>soft delete</em> — dezaktywowani czytelnicy są pomijani w wynikach.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see Reader
 * @see com.xwolfq.biblioteka1.service.ReaderService
 */
@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    /**
     * Zwraca listę wszystkich aktywnych czytelników ({@code active = true}).
     *
     * <p>Używana jako główne źródło danych dla endpointów wyświetlających
     * listę czytelników — ukrywa rekordy dezaktywowane (soft-deleted).</p>
     *
     * @return lista aktywnych czytelników; pusta lista, jeśli żaden nie istnieje
     */
    List<Reader> findByActiveTrue();

    /**
     * Wyszukuje aktywnego czytelnika po jego identyfikatorze.
     *
     * <p>Zwraca pusty {@link Optional}, jeśli czytelnik o podanym {@code id}
     * nie istnieje lub został dezaktywowany.</p>
     *
     * @param id identyfikator szukanego czytelnika
     * @return {@link Optional} zawierający czytelnika lub pusty, jeśli nie znaleziono
     */
    Optional<Reader> findByIdAndActiveTrue(Long id);
}
