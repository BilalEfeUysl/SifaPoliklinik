package com.sifa.poliklinik.repository;

import com.sifa.poliklinik.model.Odeme;
import com.sifa.poliklinik.model.enums.OdemeDurumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OdemeRepository extends JpaRepository<Odeme, Long> {
    Optional<Odeme> findByMuayeneKaydiId(Long muayeneKaydiId);
    List<Odeme> findByHastaId(Long hastaId);
    List<Odeme> findByOdemeDurumu(OdemeDurumu odemeDurumu);
    List<Odeme> findByHastaIdAndOdemeDurumu(Long hastaId, OdemeDurumu odemeDurumu);

    /**
     * Hastanın ödenmemiş borçlarının toplamını hesaplar.
     */
    @Query("SELECT COALESCE(SUM(o.netTutar), 0) FROM Odeme o " +
           "WHERE o.hasta.id = :hastaId AND o.odemeDurumu = 'BEKLIYOR'")
    Double getToplamBorc(@Param("hastaId") Long hastaId);
}
