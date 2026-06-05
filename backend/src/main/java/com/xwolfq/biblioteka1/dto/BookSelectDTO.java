package com.xwolfq.biblioteka1.dto;

/**
 * DTO (Data Transfer Object) reprezentujące uproszczone dane książki
 * przeznaczone do wyświetlenia w listach rozwijanych (select) interfejsu użytkownika.
 *
 * <p>Zwracane przez endpoint {@code GET /api/books/select}. Zawiera wyłącznie
 * identyfikator i tytuł książki — minimalizuje ilość przesyłanych danych
 * w porównaniu do pełnej encji {@link com.xwolfq.biblioteka1.model.Book}.</p>
 *
 * <p>Zaimplementowane jako Java Record — wszystkie pola są {@code final},
 * a konstruktor, gettery, {@code equals}, {@code hashCode} i {@code toString}
 * generowane są automatycznie przez kompilator.</p>
 *
 * @param id    unikalny identyfikator książki
 * @param title tytuł książki wyświetlany na liście wyboru
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 * @see com.xwolfq.biblioteka1.model.Book
 * @see com.xwolfq.biblioteka1.service.BookService#getForSelect()
 */
public record BookSelectDTO(
        Long id,
        String title
) {}
