package com.xwolfq.biblioteka1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Główna klasa uruchomieniowa aplikacji bibliotecznej Library-System.
 *
 * <p>Adnotacja {@code @SpringBootApplication} łączy trzy mechanizmy Spring Boot:</p>
 * <ul>
 *   <li>{@code @Configuration} — klasa jest źródłem definicji beanów</li>
 *   <li>{@code @EnableAutoConfiguration} — włącza automatyczną konfigurację
 *       na podstawie zależności z {@code pom.xml} (JPA, web, itp.)</li>
 *   <li>{@code @ComponentScan} — skanuje pakiet {@code com.xwolfq.biblioteka1}
 *       i podpakiety w poszukiwaniu komponentów Spring</li>
 * </ul>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class Biblioteka1Application {

    /**
     * Punkt wejścia aplikacji — uruchamia kontener Spring Boot.
     *
     * @param args argumenty wiersza poleceń przekazywane do {@link SpringApplication#run}
     */
    public static void main(String[] args) {
        SpringApplication.run(Biblioteka1Application.class, args);
    }

}
