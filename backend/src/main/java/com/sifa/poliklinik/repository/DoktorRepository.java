package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.Doktor;
import com.sifa.poliklinik.model.Klinik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoktorRepository extends JpaRepository<Doktor, Long> {
    List<Doktor> findByKlinik(Klinik klinik);
    List<Doktor> findByKlinikId(Long klinikId);
    Optional<Doktor> findByKullaniciId(Long kullaniciId);
    List<Doktor> findByKlinikIdAndMusaitMiTrue(Long klinikId);
}
