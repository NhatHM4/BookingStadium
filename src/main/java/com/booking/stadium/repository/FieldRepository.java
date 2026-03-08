package com.booking.stadium.repository;

import com.booking.stadium.entity.Field;
import com.booking.stadium.enums.FieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findByStadiumId(Long stadiumId);

    List<Field> findByStadiumIdAndIsActiveTrue(Long stadiumId);

    List<Field> findByStadiumIdAndFieldType(Long stadiumId, FieldType fieldType);

    List<Field> findByStadiumIdAndFieldTypeAndIsActiveTrue(Long stadiumId, FieldType fieldType);
}
