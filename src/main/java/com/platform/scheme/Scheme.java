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
}
