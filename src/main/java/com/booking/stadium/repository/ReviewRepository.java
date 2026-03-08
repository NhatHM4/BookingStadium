package com.booking.stadium.repository;

import com.booking.stadium.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByStadiumId(Long stadiumId, Pageable pageable);

    Page<Review> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.stadium.id = :stadiumId")
    Double getAverageRatingByStadiumId(@Param("stadiumId") Long stadiumId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.stadium.id = :stadiumId")
    Long countByStadiumId(@Param("stadiumId") Long stadiumId);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getOverallAverageRating();
}
