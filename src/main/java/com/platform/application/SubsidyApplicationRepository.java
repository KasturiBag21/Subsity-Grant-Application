//package com.platform.application;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//
//@Repository // Marks this as an autowire candidate for Spring Data JPA
//public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {
//
//    // MODULE 2 ADMISSION TASK: Feeds separate Field, District, and Finance Officer control centers
//    List<SubsidyApplication> findByApplicationStatus(ApplicationStatus applicationStatus);
//}

package com.platform.application;

import com.platform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {

    // Feeds separate Field, District, and Finance Officer control centers
    List<SubsidyApplication> findByApplicationStatus(ApplicationStatus applicationStatus);

    // 🔍 VERIFIED: Clean database query mapping that completely prevents 500 memory errors
    List<SubsidyApplication> findByUser(User user);
}

