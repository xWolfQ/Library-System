package com.xwolfq.biblioteka1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.xwolfq.biblioteka1.model.Book;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Repozytorium Spring Data JPA dla encji {@link Book}.
 *
 * <p>Rozszerza {@link JpaRepository}, dostarczając standardowe operacje CRUD
 * oraz niestandardowe zapytania generowane przez Spring Data na podstawie
 * nazw metod (<em>query derivation</em>).</p>
 *
 * <p>Wszystkie metody wyszukujące aktywne książki filtrują wyniki
 * po polu {@code active = true}, realizując tym samym mechanizm
 * <em>soft delete</em> — nieaktywne książki są transparentnie pomijane.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see Book
 * @see com.xwolfq.biblioteka1.service.BookService
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Zwraca listę wszystkich aktywnych książek ({@code active = true}).
     *
     * <p>Używana jako główne źródło danych dla endpointów wyświetlających
     * katalog biblioteki — ukrywa książki dezaktywowane (soft-deleted).</p>
     *
     * @return lista aktywnych książek; pusta lista, jeśli żadna nie istnieje
     */
    List<Book> findByActiveTrue();

    /**
     * Wyszukuje aktywną książkę po jej identyfikatorze.
     *
     * <p>Zwraca pusty {@link Optional}, jeśli książka o podanym {@code id}
     * nie istnieje lub została dezaktywowana.</p>
     *
     * @param id identyfikator szukanej książki
     * @return {@link Optional} zawierający książkę lub pusty, jeśli nie znaleziono
     */
    Optional<Book> findByIdAndActiveTrue(Long id);

    /**
     * Zwraca ograniczoną liczbę aktywnych książek posortowanych malejąco
     * według liczby wypożyczeń ({@code borrowedCopies}).
     *
     * <p>Używana przez endpoint {@code GET /api/books/top} do wyznaczania
     * rankingu najpopularniejszych pozycji. Parametr {@link Pageable}
     * służy wyłącznie do ograniczenia liczby zwracanych wyników
     * (np. {@code PageRequest.of(0, 5)}).</p>
     *
     * @param pageable obiekt stronicowania określający maksymalną liczbę wyników
     * @return lista aktywnych książek w kolejności od najczęściej wypożyczanej
     */
    List<Book> findByActiveTrueOrderByBorrowedCopiesDesc(Pageable pageable);

}
