package com.booking.stadium.repository;

import com.booking.stadium.entity.Team;
import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.SkillLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByCaptainId(Long captainId);

    Page<Team> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.isActive = true " +
            "AND (:city IS NULL OR LOWER(t.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:fieldType IS NULL OR t.preferredFieldType = :fieldType) " +
            "AND (:skillLevel IS NULL OR t.skillLevel = :skillLevel)")
    Page<Team> searchTeams(
            @Param("city") String city,
            @Param("fieldType") FieldType fieldType,
            @Param("skillLevel") SkillLevel skillLevel,
            Pageable pageable);

    @Query("SELECT t FROM Team t JOIN t.members tm " +
            "WHERE tm.user.id = :userId AND tm.status = 'ACTIVE'")
    List<Team> findTeamsByMemberId(@Param("userId") Long userId);
}
