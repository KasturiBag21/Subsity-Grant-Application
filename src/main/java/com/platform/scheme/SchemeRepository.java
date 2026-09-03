package com.platform.scheme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Ensures Spring explicitly captures it as an infrastructure bean
public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    java.util.Optional<Scheme> findBySchemeName(String schemeName);
}
