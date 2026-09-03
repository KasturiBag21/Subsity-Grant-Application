package com.platform.application;

import com.platform.user.User;
import com.platform.scheme.Scheme;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subsidy_applications")
public class SubsidyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(nullable = false)
    private boolean eligibilityStatus;

    // FIX: Changed from String to Enum to power multi-level routing controls safely
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    @Column(nullable = false)
    private LocalDateTime applicationDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }
    public boolean isEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(boolean eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }
    public ApplicationStatus getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(ApplicationStatus applicationStatus) { this.applicationStatus = applicationStatus; }
    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }
}
