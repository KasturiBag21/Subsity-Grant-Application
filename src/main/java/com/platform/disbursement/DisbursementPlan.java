package com.platform.disbursement;

import com.platform.application.SubsidyApplication;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "disbursement_plans")
public class DisbursementPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private SubsidyApplication application;

    @Column(nullable = false)
    private double totalPlannedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisbursementPlanStatus status = DisbursementPlanStatus.PLANNED;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<DisbursementMilestone> milestones = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public SubsidyApplication getApplication() { return application; }
    public void setApplication(SubsidyApplication application) { this.application = application; }
    public double getTotalPlannedAmount() { return totalPlannedAmount; }
    public void setTotalPlannedAmount(double totalPlannedAmount) { this.totalPlannedAmount = totalPlannedAmount; }
    public DisbursementPlanStatus getStatus() { return status; }
    public void setStatus(DisbursementPlanStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<DisbursementMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<DisbursementMilestone> milestones) { this.milestones = milestones; }

    @Transient
    public double getReleasedAmount() {
        return milestones.stream().mapToDouble(DisbursementMilestone::getReleasedAmount).sum();
    }

    @Transient
    public double getPendingAmount() {
        return Math.max(0.0, totalPlannedAmount - getReleasedAmount());
    }

    @Transient
    public double getUtilizationPercentage() {
        return totalPlannedAmount > 0 ? (getReleasedAmount() * 100.0 / totalPlannedAmount) : 0.0;
    }
}
