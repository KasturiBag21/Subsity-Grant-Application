package com.platform.scheme;

import jakarta.persistence.*;

@Entity
@Table(name = "schemes")
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String schemeName;

    private int eligibleAgeMin;
    private int eligibleAgeMax;
    private double maxIncomeThreshold;
    private double totalRegionalBudget;
    private double remainingBudget;

    // NEW: Scheme-specific eligibility criteria (from the criteria-update table)
    // Null/blank means "no restriction" (i.e. "Any") for that field.
    private String requiredOccupation;     // e.g. "Student", "Farmer,Agriculturist" (comma-separated = any of these accepted)
    private String requiredGender;         // e.g. "Female"; null = any gender
    private boolean requiresDisability;    // true = applicant must have Disability Status: Yes (PwD)
    private String requiredResidenceType;  // e.g. "Rural"; null = any residence type

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
    public int getEligibleAgeMin() { return eligibleAgeMin; }
    public void setEligibleAgeMin(int eligibleAgeMin) { this.eligibleAgeMin = eligibleAgeMin; }
    public int getEligibleAgeMax() { return eligibleAgeMax; }
    public void setEligibleAgeMax(int eligibleAgeMax) { this.eligibleAgeMax = eligibleAgeMax; }
    public double getMaxIncomeThreshold() { return maxIncomeThreshold; }
    public void setMaxIncomeThreshold(double maxIncomeThreshold) { this.maxIncomeThreshold = maxIncomeThreshold; }
    public double getTotalRegionalBudget() { return totalRegionalBudget; }
    public void setTotalRegionalBudget(double totalRegionalBudget) { this.totalRegionalBudget = totalRegionalBudget; }
    public double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(double remainingBudget) { this.remainingBudget = remainingBudget; }

    public String getRequiredOccupation() { return requiredOccupation; }
    public void setRequiredOccupation(String requiredOccupation) { this.requiredOccupation = requiredOccupation; }
    public String getRequiredGender() { return requiredGender; }
    public void setRequiredGender(String requiredGender) { this.requiredGender = requiredGender; }
    public boolean isRequiresDisability() { return requiresDisability; }
    public void setRequiresDisability(boolean requiresDisability) { this.requiresDisability = requiresDisability; }
    public String getRequiredResidenceType() { return requiredResidenceType; }
    public void setRequiredResidenceType(String requiredResidenceType) { this.requiredResidenceType = requiredResidenceType; }

    // Convenience flags for templates (th:if reads cleanly off these)
    public boolean isOccupationRestricted() { return requiredOccupation != null && !requiredOccupation.isBlank(); }
    public boolean isGenderRestricted() { return requiredGender != null && !requiredGender.isBlank(); }
    public boolean isResidenceRestricted() { return requiredResidenceType != null && !requiredResidenceType.isBlank(); }
}




//package com.platform.scheme;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "schemes")
//public class Scheme {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true, nullable = false)
//    private String schemeName;
//
//    private int eligibleAgeMin;
//    private int eligibleAgeMax;
//    private double maxIncomeThreshold;
//    private double totalRegionalBudget;
//    private double remainingBudget;
//
//    // Getters and Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getSchemeName() { return schemeName; }
//    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
//    public int getEligibleAgeMin() { return eligibleAgeMin; }
//    public void setEligibleAgeMin(int eligibleAgeMin) { this.eligibleAgeMin = eligibleAgeMin; }
//    public int getEligibleAgeMax() { return eligibleAgeMax; }
//    public void setEligibleAgeMax(int eligibleAgeMax) { this.eligibleAgeMax = eligibleAgeMax; }
//    public double getMaxIncomeThreshold() { return maxIncomeThreshold; }
//    public void setMaxIncomeThreshold(double maxIncomeThreshold) { this.maxIncomeThreshold = maxIncomeThreshold; }
//    public double getTotalRegionalBudget() { return totalRegionalBudget; }
//    public void setTotalRegionalBudget(double totalRegionalBudget) { this.totalRegionalBudget = totalRegionalBudget; }
//    public double getRemainingBudget() { return remainingBudget; }
//    public void setRemainingBudget(double remainingBudget) { this.remainingBudget = remainingBudget; }
//}
