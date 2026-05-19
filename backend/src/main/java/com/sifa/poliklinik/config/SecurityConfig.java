package com.sifa.poliklinik.config;

import com.sifa.poliklinik.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security yapılandırması.
 * JWT tabanlı, stateless kimlik doğrulama.
 * RBAC (Role-Based Access Control) ile endpoint koruması.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Herkese açık
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Swagger / OpenAPI
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()

                // Hasta yönetimi - Kayıt Görevlisi ve Yönetici
                .requestMatchers("/api/hastalar/**").hasAnyRole("KAYIT_GOREVLISI", "YONETICI", "DOKTOR", "RANDEVU_GOREVLISI")

                // Randevu yönetimi - Randevu Görevlisi ve Yönetici
                .requestMatchers("/api/randevular/**").hasAnyRole("RANDEVU_GOREVLISI", "YONETICI", "DOKTOR")

                // Muayene - Doktor ve Yönetici
                .requestMatchers("/api/muayeneler/**").hasAnyRole("DOKTOR", "YONETICI")

                // Ödeme - Veznedar ve Yönetici
                .requestMatchers("/api/odemeler/**").hasAnyRole("VEZNEDAR", "YONETICI")

                // Yönetici paneli
                .requestMatchers("/api/yonetici/**").hasRole("YONETICI")

                // Klinik ve doktor listesi - herkes görebilir (giriş yapmışsa)
                .requestMatchers("/api/klinikler/**").authenticated()
                .requestMatchers("/api/doktorlar/**").authenticated()

                // Diğer tüm istekler kimlik doğrulaması gerektirir
                .anyRequest().authenticated()
            )
            // H2 Console için frame'lere izin ver
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
