package com.platform.scheme;

import com.platform.user.User;
import com.platform.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

@Configuration
public class SchemeDataLoader {

    @Bean
    CommandLineRunner loadSchemes(SchemeRepository schemeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            // Runs on EVERY startup, on every teammate's own local database.
            // For each scheme: if it doesn't exist yet, create it. If it already
            // exists (e.g. from before this update), just refresh its rules —
            // remainingBudget is left untouched so nobody's live budget tracking
            // gets reset. This means nobody has to run any manual SQL; pulling
            // the code and restarting the app is enough to sync everyone's DB.

            upsertScheme(schemeRepository, "Education Subsidy",
                    18, 30, 500000.0, 5000000.0,
                    "Student", null, false, null);

            upsertScheme(schemeRepository, "Farmer Support Grant",
                    18, 65, 400000.0, 8000000.0,
                    "Farmer,Agriculturist", null, false, null);

            upsertScheme(schemeRepository, "Housing Assistance Scheme",
                    21, 70, 300000.0, 12000000.0,
                    null, null, false, null);

            upsertScheme(schemeRepository, "Women Entrepreneurship Grant",
                    21, 55, 600000.0, 6000000.0,
                    null, "Female", false, null);

            upsertScheme(schemeRepository, "Senior Citizen Assistance",
                    60, 100, 300000.0, 4000000.0,
                    null, null, false, null);

            upsertScheme(schemeRepository, "Divyang Disability Support",
                    18, 80, 350000.0, 3500000.0,
                    null, null, true, null);

            upsertScheme(schemeRepository, "Rural Skill Development Grant",
                    18, 45, 450000.0, 7000000.0,
                    null, null, false, "Rural");

            System.out.println(">> Master Scheme Rules Sync Complete. 7 schemes verified/updated.");

            if (userRepository.count() == 0) {
                User field = new User();
                field.setEmail("field.officer@gmail.com");
                field.setPassword(passwordEncoder.encode("field123"));
                field.setRoles(new HashSet<>(Collections.singletonList("ROLE_FIELD_OFFICER")));
                userRepository.save(field);

                User district = new User();
                district.setEmail("district.officer@gmail.com");
                district.setPassword(passwordEncoder.encode("district123"));
                district.setRoles(new HashSet<>(Collections.singletonList("ROLE_DISTRICT_OFFICER")));
                userRepository.save(district);

                User finance = new User();
                finance.setEmail("finance.approver@gmail.com");
                finance.setPassword(passwordEncoder.encode("finance123"));
                finance.setRoles(new HashSet<>(Collections.singletonList("ROLE_FINANCE_APPROVER")));
                userRepository.save(finance);
            }
        };
    }

    /**
     * Creates the scheme if it's missing, or refreshes its eligibility rules
     * if it already exists. remainingBudget is intentionally left alone on
     * existing rows so live budget consumption isn't reset by a restart.
     */
    private void upsertScheme(SchemeRepository schemeRepository, String name,
                              int ageMin, int ageMax, double maxIncome, double totalBudget,
                              String requiredOccupation, String requiredGender,
                              boolean requiresDisability, String requiredResidenceType) {

        Scheme scheme = schemeRepository.findBySchemeName(name).orElseGet(Scheme::new);
        boolean isNew = scheme.getId() == null;

        scheme.setSchemeName(name);
        scheme.setEligibleAgeMin(ageMin);
        scheme.setEligibleAgeMax(ageMax);
        scheme.setMaxIncomeThreshold(maxIncome);
        scheme.setTotalRegionalBudget(totalBudget);
        scheme.setRequiredOccupation(requiredOccupation);
        scheme.setRequiredGender(requiredGender);
        scheme.setRequiresDisability(requiresDisability);
        scheme.setRequiredResidenceType(requiredResidenceType);

        if (isNew) {
            scheme.setRemainingBudget(totalBudget);
        }

        schemeRepository.save(scheme);
    }
}


//package com.platform.scheme;
//
//import com.platform.user.User;
//import com.platform.user.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Collections;
//import java.util.HashSet;
//
//@Configuration
//public class SchemeDataLoader {
//
//    @Bean
//    CommandLineRunner loadSchemes(SchemeRepository schemeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            if (schemeRepository.count() == 0) {
//
//                // 1. Education Subsidy
//                Scheme education = new Scheme();
//                education.setSchemeName("Education Subsidy");
//                education.setEligibleAgeMin(18);
//                education.setEligibleAgeMax(30);
//                education.setMaxIncomeThreshold(500000.0);
//                education.setTotalRegionalBudget(5000000.0);
//                education.setRemainingBudget(5000000.0);
//                education.setRequiredOccupation("Student");
//                schemeRepository.save(education);
//
//                // 2. Farmer Support Grant
//                Scheme farmer = new Scheme();
//                farmer.setSchemeName("Farmer Support Grant");
//                farmer.setEligibleAgeMin(18);
//                farmer.setEligibleAgeMax(65);
//                farmer.setMaxIncomeThreshold(400000.0);
//                farmer.setTotalRegionalBudget(8000000.0);
//                farmer.setRemainingBudget(8000000.0);
//                farmer.setRequiredOccupation("Farmer,Agriculturist");
//                schemeRepository.save(farmer);
//
//                // 3. Housing Assistance Scheme
//                Scheme housing = new Scheme();
//                housing.setSchemeName("Housing Assistance Scheme");
//                housing.setEligibleAgeMin(21);
//                housing.setEligibleAgeMax(70);
//                housing.setMaxIncomeThreshold(300000.0);
//                housing.setTotalRegionalBudget(12000000.0);
//                housing.setRemainingBudget(12000000.0);
//                schemeRepository.save(housing);
//
//                // 4. Women Entrepreneurship Grant
//                Scheme business = new Scheme();
//                business.setSchemeName("Women Entrepreneurship Grant");
//                business.setEligibleAgeMin(21);
//                business.setEligibleAgeMax(55);
//                business.setMaxIncomeThreshold(600000.0);
//                business.setTotalRegionalBudget(6000000.0);
//                business.setRemainingBudget(6000000.0);
//                business.setRequiredGender("Female");
//                schemeRepository.save(business);
//
//                // 5. Senior Citizen Assistance
//                Scheme senior = new Scheme();
//                senior.setSchemeName("Senior Citizen Assistance");
//                senior.setEligibleAgeMin(60);
//                senior.setEligibleAgeMax(100);
//                senior.setMaxIncomeThreshold(300000.0);
//                senior.setTotalRegionalBudget(4000000.0);
//                senior.setRemainingBudget(4000000.0);
//                schemeRepository.save(senior);
//
//                // NEW 6. Divyang Disability Support Scheme
//                Scheme divyang = new Scheme();
//                divyang.setSchemeName("Divyang Disability Support");
//                divyang.setEligibleAgeMin(18);
//                divyang.setEligibleAgeMax(80);
//                divyang.setMaxIncomeThreshold(350000.0);
//                divyang.setTotalRegionalBudget(3500000.0);
//                divyang.setRemainingBudget(3500000.0);
//                divyang.setRequiresDisability(true);
//                schemeRepository.save(divyang);
//
//                // NEW 7. Rural Skill Development Grant
//                Scheme skill = new Scheme();
//                skill.setSchemeName("Rural Skill Development Grant");
//                skill.setEligibleAgeMin(18);
//                skill.setEligibleAgeMax(45);
//                skill.setMaxIncomeThreshold(450000.0);
//                skill.setTotalRegionalBudget(7000000.0);
//                skill.setRemainingBudget(7000000.0);
//                skill.setRequiredResidenceType("Rural");
//                schemeRepository.save(skill);
//
//                System.out.println(">> Master Scheme Rules Bootstrap Complete. 7 schemes loaded.");
//            }
//
//            if (userRepository.count() == 0) {
//                User field = new User();
//                field.setEmail("field.officer@gmail.com");
//                field.setPassword(passwordEncoder.encode("field123"));
//                field.setRoles(new HashSet<>(Collections.singletonList("ROLE_FIELD_OFFICER")));
//                userRepository.save(field);
//
//                User district = new User();
//                district.setEmail("district.officer@gmail.com");
//                district.setPassword(passwordEncoder.encode("district123"));
//                district.setRoles(new HashSet<>(Collections.singletonList("ROLE_DISTRICT_OFFICER")));
//                userRepository.save(district);
//
//                User finance = new User();
//                finance.setEmail("finance.approver@gmail.com");
//                finance.setPassword(passwordEncoder.encode("finance123"));
//                finance.setRoles(new HashSet<>(Collections.singletonList("ROLE_FINANCE_APPROVER")));
//                userRepository.save(finance);
//            }
//        };
//    }
//}
//
//
//
//
//
