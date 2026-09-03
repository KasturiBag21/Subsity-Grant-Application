package com.platform.application;

public enum ApplicationStatus {
    SUBMITTED,           // Fresh applicant submission phase
    FIELD_VERIFICATION,  // Assigned to Field Officer queue
    DISTRICT_REVIEW,     // Awaiting District Admin risk appraisal
    FINANCE_PENDING,     // Cleared for milestone payout treasury validation
    APPROVED,            // Active subsidy tracking status
    REJECTED             // Application drop state
}
