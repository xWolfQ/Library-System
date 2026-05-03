package com.xwolfq.biblioteka1.dto;

/**
 * DTO (Data Transfer Object) reprezentujące uproszczone dane czytelnika
 * przeznaczone do wyświetlenia w listach rozwijanych (select) interfejsu użytkownika.
 *
 * <p>Zwracane przez endpoint {@code GET /api/readers/select}. Zawiera identyfikator
 * oraz imię i nazwisko czytelnika jako osobne pola — minimalizuje ilość
 * przesyłanych danych w porównaniu do pełnej encji
 * {@link com.xwolfq.biblioteka1.model.Reader}.</p>
 *
 * <p>Zaimplementowane jako Java Record — wszystkie pola są {@code final},
 * a konstruktor, gettery, {@code equals}, {@code hashCode} i {@code toString}
 * generowane są automatycznie przez kompilator.</p>
 *
 * @param id       unikalny identyfikator czytelnika
 * @param fullName imię czytelnika (w oryginalnym kodzie pole nazwane {@code fullName},
 *                 lecz przekazywane jest do niego wyłącznie imię — {@code firstName})
 * @param lastName nazwisko czytelnika
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.model.Reader
 * @see com.xwolfq.biblioteka1.service.ReaderService#getForSelect()
 */
public record ReaderSelectDTO(
        Long id,
        String fullName,
        String lastName) {}
