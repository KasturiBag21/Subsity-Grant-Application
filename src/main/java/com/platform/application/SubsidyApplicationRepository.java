package com.platform.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Marks this as an autowire candidate for Spring Data JPA
public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {
}
