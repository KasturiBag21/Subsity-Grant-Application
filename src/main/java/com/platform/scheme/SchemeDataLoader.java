package com.platform.scheme;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemeDataLoader {

    @Bean
    CommandLineRunner loadSchemes(SchemeRepository schemeRepository) {
        return args -> {
            if (schemeRepository.count() == 0) {

                // 1. Education Subsidy
                Scheme education = new Scheme();
                education.setSchemeName("Education Subsidy");
                education.setEligibleAgeMin(18);
                education.setEligibleAgeMax(30);
                education.setMaxIncomeThreshold(500000.0);
                education.setTotalRegionalBudget(5000000.0);
                education.setRemainingBudget(5000000.0);
                schemeRepository.save(education);

                // 2. Farmer Support Grant
                Scheme farmer = new Scheme();
                farmer.setSchemeName("Farmer Support Grant");
                farmer.setEligibleAgeMin(18);
                farmer.setEligibleAgeMax(65);
                farmer.setMaxIncomeThreshold(400000.0);
                farmer.setTotalRegionalBudget(8000000.0);
                farmer.setRemainingBudget(8000000.0);
                schemeRepository.save(farmer);

                // 3. Housing Assistance Scheme
                Scheme housing = new Scheme();
                housing.setSchemeName("Housing Assistance Scheme");
                housing.setEligibleAgeMin(18);
                housing.setEligibleAgeMax(70);
                housing.setMaxIncomeThreshold(300000.0);
                housing.setTotalRegionalBudget(12000000.0);
                housing.setRemainingBudget(12000000.0);
                schemeRepository.save(housing);

                // 4. Women Entrepreneurship Grant
                Scheme business = new Scheme();
                business.setSchemeName("Women Entrepreneurship Grant");
                business.setEligibleAgeMin(18);
                business.setEligibleAgeMax(60);
                business.setMaxIncomeThreshold(600000.0);
                business.setTotalRegionalBudget(6000000.0);
                business.setRemainingBudget(6000000.0);
                schemeRepository.save(business);

                // 5. Senior Citizen Assistance
                Scheme senior = new Scheme();
                senior.setSchemeName("Senior Citizen Assistance");
                senior.setEligibleAgeMin(60);
                senior.setEligibleAgeMax(100);
                senior.setMaxIncomeThreshold(300000.0);
                senior.setTotalRegionalBudget(4000000.0);
                senior.setRemainingBudget(4000000.0);
                schemeRepository.save(senior);

                System.out.println(">> Database Bootstrapping Complete: Master Scheme Rules Loaded Successfully.");
            }
        };
    }
}
