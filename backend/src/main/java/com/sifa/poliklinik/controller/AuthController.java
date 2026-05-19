package com.sifa.poliklinik.controller;

import com.sifa.poliklinik.dto.AuthResponse;
import com.sifa.poliklinik.dto.LoginRequest;
import com.sifa.poliklinik.dto.RegisterRequest;
import com.sifa.poliklinik.model.Kullanici;
import com.sifa.poliklinik.repository.KullaniciRepository;
import com.sifa.poliklinik.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Kimlik doğrulama controller'ı.
 * Login ve register endpoint'leri.
 */
@Tag(name = "Kimlik Doğrulama", description = "Giriş ve kayıt işlemleri")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * POST /api/auth/login
     * Kullanıcı girişi — JWT token döner.
     */
    @Operation(summary = "Kullanıcı girişi — JWT token döner")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSifre())
        );

        String token = tokenProvider.generateToken(authentication);

        Kullanici kullanici = kullaniciRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .id(kullanici.getId())
                .ad(kullanici.getAd())
                .soyad(kullanici.getSoyad())
                .email(kullanici.getEmail())
                .rol(kullanici.getRol())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register
     * Yeni kullanıcı kaydı (Yönetici tarafından kullanılır).
     */
    @Operation(summary = "Yeni kullanıcı kaydı")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (kullaniciRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kullanılıyor: " + request.getEmail());
        }

        Kullanici kullanici = Kullanici.builder()
                .ad(request.getAd())
                .soyad(request.getSoyad())
                .email(request.getEmail())
                .sifre(passwordEncoder.encode(request.getSifre()))
                .rol(request.getRol())
                .telefon(request.getTelefon())
                .build();

        kullanici = kullaniciRepository.save(kullanici);

        AuthResponse response = AuthResponse.builder()
                .id(kullanici.getId())
                .ad(kullanici.getAd())
                .soyad(kullanici.getSoyad())
                .email(kullanici.getEmail())
                .rol(kullanici.getRol())
                .build();

        return ResponseEntity.ok(response);
    }
}
