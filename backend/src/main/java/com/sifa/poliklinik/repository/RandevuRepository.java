package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.Randevu;
import com.sifa.poliklinik.model.enums.RandevuDurumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RandevuRepository extends JpaRepository<Randevu, Long> {
    List<Randevu> findByHastaId(Long hastaId);
    List<Randevu> findByDoktorId(Long doktorId);
    List<Randevu> findByDurum(RandevuDurumu durum);
    List<Randevu> findByDoktorIdAndDurum(Long doktorId, RandevuDurumu durum);

    /**
     * Doktorun belirli tarih aralığındaki iptal edilmemiş randevularını getirir.
     * Müsait saat listesi oluşturmak için kullanılır.
     */
    @Query("SELECT r FROM Randevu r WHERE r.doktor.id = :doktorId " +
           "AND r.durum != :iptalDurum " +
           "AND r.tarihSaat BETWEEN :baslangic AND :bitis")
    List<Randevu> findCakisanRandevular(
        @Param("doktorId") Long doktorId,
        @Param("baslangic") LocalDateTime baslangic,
        @Param("bitis") LocalDateTime bitis,
        @Param("iptalDurum") RandevuDurumu iptalDurum
    );

    /**
     * Aynı doktor ve aynı saat için iptal dışı randevu var mı?
     * Çakışma kontrolünde kullanılır.
     */
    boolean existsByDoktorIdAndTarihSaatAndDurumNot(Long doktorId, LocalDateTime tarihSaat, RandevuDurumu durum);

    boolean existsByDoktorIdAndTarihSaatAndDurumNotAndIdNot(Long doktorId, LocalDateTime tarihSaat, RandevuDurumu durum, Long id);

    /**
     * Belirli tarih aralığındaki randevuları getirir.
     */
    List<Randevu> findByTarihSaatBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
