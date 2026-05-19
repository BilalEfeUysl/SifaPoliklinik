package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.Kullanici;
import com.sifa.poliklinik.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    Optional<Kullanici> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Kullanici> findByRol(Rol rol);
}
