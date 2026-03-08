package com.booking.stadium.repository;

import com.booking.stadium.entity.DepositPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositPolicyRepository extends JpaRepository<DepositPolicy, Long> {

    Optional<DepositPolicy> findByStadiumId(Long stadiumId);
}
