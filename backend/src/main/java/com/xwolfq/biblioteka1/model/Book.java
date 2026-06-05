package com.xwolfq.biblioteka1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

/**
 * Encja JPA reprezentująca książkę w systemie bibliotecznym.
 *
 * <p>Mapowana na tabelę {@code books} w bazie danych PostgreSQL.
 * Każda książka może posiadać wiele egzemplarzy ({@code availableCopies} + {@code borrowedCopies})
 * oraz być powiązana z wieloma wypożyczeniami ({@link Loan}) poprzez relację
 * {@code @OneToMany}.</p>
 *
 * <p>Usuwanie książek realizowane jest jako <em>soft delete</em> — pole {@code active}
 * ustawiane jest na {@code false}, a rekord pozostaje w bazie danych.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see Loan
 * @see com.xwolfq.biblioteka1.repository.BookRepository
 * @see com.xwolfq.biblioteka1.service.BookService
 */
@Entity
@Table(name = "books")
public class Book {

    /**
     * Unikalny identyfikator książki generowany automatycznie przez bazę danych.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tytuł książki. */
    private String title;

    /** Imię i nazwisko autora książki. */
    private String author;

    /** Kategoria tematyczna książki (np. literatura piękna, informatyka). */
    private String category;

    /**
     * Numer ISBN książki — unikalny w skali całej tabeli.
     * Ograniczenie unikalności narzucone przez {@code @Column(unique = true)}.
     */
    @Column(unique = true)
    private String isbn;

    /**
     * Liczba egzemplarzy aktualnie dostępnych do wypożyczenia.
     * Dekrementowana przy tworzeniu wypożyczenia, inkrementowana przy zwrocie.
     */
    @Column(name = "available_copies")
    private Integer availableCopies;

    /**
     * Łączna liczba egzemplarzy aktualnie wypożyczonych przez czytelników.
     * Inkrementowana przy tworzeniu wypożyczenia. Używana do rankingu
     * najczęściej wypożyczanych książek.
     */
    @Column(name = "borrowed_copies")
    private Integer borrowedCopies;

    /** Rok wydania książki. */
    @Column(name = "published_year")
    private Integer publishedYear;

    /**
     * Lista wszystkich wypożyczeń powiązanych z tą książką.
     * Relacja odwrotna do {@link Loan#book} — zarządzana przez stronę
     * {@code Loan}.
     */
    @OneToMany(mappedBy = "book")
    private List<Loan> loans;

    /**
     * Flaga aktywności książki — obsługuje mechanizm soft delete.
     * Wartość domyślna: {@code true}. Ustawiana na {@code false}
     * przy dezaktywacji zamiast fizycznego usunięcia rekordu.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Zwraca flagę aktywności książki.
     *
     * @return {@code true} jeśli książka jest aktywna (nie usunięta), {@code false} po dezaktywacji
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Ustawia flagę aktywności książki.
     *
     * <p><strong>Uwaga:</strong> parametr ma literówkę w oryginalnym kodzie
     * ({@code acive} zamiast {@code active}) — nie zmieniono go, aby nie wprowadzać
     * nieoczekiwanych modyfikacji.</p>
     *
     * @param acive nowa wartość flagi aktywności
     */
    public void setActive(boolean acive) {
        this.active = acive;
    }

    /**
     * Zwraca numer ISBN książki.
     *
     * <p>Nazwa metody ({@code getISBN}) odbiega od konwencji JavaBeans
     * ({@code getIsbn}), lecz jest zachowana w oryginalnej formie.</p>
     *
     * @return numer ISBN lub {@code null}, jeśli nie został podany
     */
    public String getISBN() {
        return isbn;
    }

    /**
     * Ustawia numer ISBN książki.
     *
     * @param isbn numer ISBN do przypisania
     */
    public void setISBN(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPublishedYear() {
        return publishedYear;

    }
    public void setPublishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;

    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getBorrowedCopies() {
        return borrowedCopies;
    }
    public void setBorrowedCopies(Integer borrowedCopies) {
        this.borrowedCopies = borrowedCopies;
    }

}
