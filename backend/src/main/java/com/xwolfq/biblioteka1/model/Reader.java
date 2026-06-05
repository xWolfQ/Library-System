package com.xwolfq.biblioteka1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Encja JPA reprezentująca czytelnika zarejestrowanego w bibliotece.
 *
 * <p>Mapowana na tabelę {@code readers} w bazie danych PostgreSQL.
 * Czytelnik może posiadać wiele wypożyczeń ({@link Loan}) powiązanych
 * relacją {@code @OneToMany}. Lista wypożyczeń jest ukryta podczas
 * serializacji JSON ({@code @JsonIgnore}), aby uniknąć cyklicznych
 * referencji.</p>
 *
 * <p>Dezaktywacja czytelnika realizowana jest jako <em>soft delete</em>
 * — pole {@code active} ustawiane jest na {@code false}.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see Loan
 * @see com.xwolfq.biblioteka1.repository.ReaderRepository
 * @see com.xwolfq.biblioteka1.service.ReaderService
 */
@Entity
@Table(name = "readers")
public class Reader {

    /**
     * Unikalny identyfikator czytelnika generowany automatycznie przez bazę danych.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Imię czytelnika. */
    private String firstName;

    /** Nazwisko czytelnika. */
    private String lastName;

    /** Adres e-mail czytelnika. */
    private String email;

    /** Numer telefonu czytelnika. */
    private String phoneNumber;

    /** Data rejestracji czytelnika w systemie bibliotecznym. */
    private LocalDate registrationDate;

    /**
     * Flaga aktywności czytelnika — obsługuje mechanizm soft delete.
     * Wartość domyślna: {@code true}. Ustawiana na {@code false}
     * przy dezaktywacji zamiast fizycznego usunięcia rekordu.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Lista wszystkich wypożyczeń powiązanych z tym czytelnikiem.
     * Relacja odwrotna do {@link Loan#reader} — zarządzana przez stronę {@code Loan}.
     * Ukryta w JSON ({@code @JsonIgnore}), aby zapobiec nieskończonej rekurencji
     * przy serializacji.
     */
    @OneToMany(mappedBy = "reader")
    @JsonIgnore
    private List<Loan> loans;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

}
