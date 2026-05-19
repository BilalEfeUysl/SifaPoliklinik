package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.MuayeneKaydi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MuayeneRepository extends JpaRepository<MuayeneKaydi, Long> {
    Optional<MuayeneKaydi> findByRandevuId(Long randevuId);
    List<MuayeneKaydi> findByRandevuHastaId(Long hastaId);
    List<MuayeneKaydi> findByRandevuDoktorId(Long doktorId);
}
