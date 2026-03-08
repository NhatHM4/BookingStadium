package com.booking.stadium.repository;

import com.booking.stadium.entity.Stadium;
import com.booking.stadium.enums.StadiumStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    Page<Stadium> findByStatus(StadiumStatus status, Pageable pageable);

    List<Stadium> findByOwnerId(Long ownerId);

    Page<Stadium> findByStatusAndCityContainingIgnoreCase(StadiumStatus status, String city, Pageable pageable);

    Page<Stadium> findByStatusAndDistrictContainingIgnoreCase(StadiumStatus status, String district, Pageable pageable);

    @Query("SELECT s FROM Stadium s WHERE s.status = :status " +
            "AND (:city IS NULL OR LOWER(s.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:district IS NULL OR LOWER(s.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
            "AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Stadium> searchStadiums(
            @Param("status") StadiumStatus status,
            @Param("city") String city,
            @Param("district") String district,
            @Param("name") String name,
            Pageable pageable);

    @Query("SELECT s FROM Stadium s WHERE s.status = 'APPROVED' " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) " +
            "* cos(radians(s.longitude) - radians(:lng)) + sin(radians(:lat)) " +
            "* sin(radians(s.latitude)))) <= :radius")
    List<Stadium> findNearbyStadiums(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radiusInKm);

    long countByStatus(StadiumStatus status);

    @Query("SELECT s FROM Stadium s WHERE s.status = :status " +
            "AND (:city IS NULL OR LOWER(s.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:district IS NULL OR LOWER(s.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
            "AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:fieldType IS NULL OR EXISTS (SELECT f FROM Field f WHERE f.stadium = s AND f.fieldType = :fieldType AND f.isActive = true))")
    Page<Stadium> searchStadiumsWithFieldType(
            @Param("status") StadiumStatus status,
            @Param("city") String city,
            @Param("district") String district,
            @Param("name") String name,
            @Param("fieldType") com.booking.stadium.enums.FieldType fieldType,
            Pageable pageable);
}
