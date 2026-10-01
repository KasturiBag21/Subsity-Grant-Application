package com.platform.disbursement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceReminderRepository extends JpaRepository<ComplianceReminder, Long> {
    boolean existsByMilestoneIdAndReminderType(Long milestoneId, ReminderType reminderType);
    boolean existsByMilestoneId(Long milestoneId);
}
