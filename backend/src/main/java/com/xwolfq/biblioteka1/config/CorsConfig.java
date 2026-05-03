package com.xwolfq.biblioteka1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfiguracja CORS (Cross-Origin Resource Sharing) dla całego API.
 *
 * <p>Klasa implementuje {@link WebMvcConfigurer} i rejestruje globalne reguły CORS
 * poprzez nadpisanie metody {@link #addCorsMappings(CorsRegistry)}.
 * Konfiguracja pozwala frontendowi działającemu na innym porcie lub domenie
 * na komunikację z backendowym API.</p>
 *
 * <p>Aktualne ustawienia zezwalają na żądania z dowolnego źródła
 * ({@code allowedOriginPatterns("*")}) dla wszystkich standardowych metod HTTP.
 * Włączone są credentials, co umożliwia przesyłanie ciasteczek sesyjnych.</p>
 *
 * <p><strong>Uwaga:</strong> niektóre kontrolery (np. {@link com.xwolfq.biblioteka1.controller.LoanController})
 * posiadają dodatkowo adnotację {@code @CrossOrigin} z węższym zakresem — w razie
 * konfliktu pierwszeństwo ma bardziej szczegółowa konfiguracja.</p>
 *
 * @author xWolfQ
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Rejestruje globalne reguły CORS dla wszystkich ścieżek API.
     *
     * <p>Konfiguracja obejmuje:</p>
     * <ul>
     *   <li>ścieżki: {@code /**} — wszystkie endpointy</li>
     *   <li>dozwolone źródła: {@code *} (dowolna domena/port)</li>
     *   <li>dozwolone metody: {@code GET, POST, PUT, DELETE, OPTIONS}</li>
     *   <li>dozwolone nagłówki: {@code *} (wszystkie)</li>
     *   <li>credentials: {@code true} (przesyłanie ciasteczek)</li>
     * </ul>
     *
     * @param registry rejestr CORS Spring MVC, do którego dodawane są reguły
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
