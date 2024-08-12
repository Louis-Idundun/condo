package com.condo.condo.repositories;

import com.condo.condo.models.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Page<Property> findByIsAvailableTrueOrderByCreatedAtDesc(Pageable pageable);

   // Page<Property> findByOwner_EmailAddress(String emailAddress, Pageable pageable);

    List<Property> findByDescriptionContainsIgnoreCase(String description);
}
