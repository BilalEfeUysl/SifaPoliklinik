package com.sifa.poliklinik.security;

import com.sifa.poliklinik.model.Kullanici;
import com.sifa.poliklinik.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security için kullanıcı bilgilerini veritabanından yükler.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Kullanıcı bulunamadı: " + email
            ));

        return new User(
            kullanici.getEmail(),
            kullanici.getSifre(),
            Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + kullanici.getRol().name())
            )
        );
    }
}
