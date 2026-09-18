package com.platform.application;

import com.platform.user.Beneficiary;
import com.platform.scheme.Scheme;
import com.platform.scheme.SchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EligibilityService {

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SubsidyApplicationRepository applicationRepository;

    public EligibilityResult checkEligibility(Beneficiary beneficiary) {
        EligibilityResult result = new EligibilityResult();
        result.setUserEmail(beneficiary.getUser().getEmail());
        result.setTargetScheme(beneficiary.getSchemeName());

        Optional<Scheme> schemeOpt = schemeRepository.findBySchemeName(beneficiary.getSchemeName());
        if (schemeOpt.isEmpty()) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("Error: Scheme target missing from database records.");
            return result;
        }

        Scheme scheme = schemeOpt.get();
        if (beneficiary.getAge() < scheme.getEligibleAgeMin() || beneficiary.getAge() > scheme.getEligibleAgeMax()) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("Rejected: Age drops outside eligible profile gaps.");
            return result;
        }

        if (beneficiary.getIncome() > scheme.getMaxIncomeThreshold()) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("Rejected: Household wealth cap exceeds budget bounds.");
            return result;
        }

        double incomeRatio = beneficiary.getIncome() / scheme.getMaxIncomeThreshold();
        int finalScore = (int) Math.round((1.0 - incomeRatio) * 100);

        result.setEligibilityScore(finalScore);
        result.setEligible(true);
        result.setRemarks("Passed criteria matching pipeline metrics successfully.");
        return result;
    }

    public SubsidyApplication registerAndRouteApplication(Beneficiary beneficiary, EligibilityResult report) {
        Optional<Scheme> schemeOpt = schemeRepository.findBySchemeName(beneficiary.getSchemeName());
        Scheme scheme = schemeOpt.orElseThrow(() -> new IllegalArgumentException("Scheme reference context corrupted"));

        SubsidyApplication application = new SubsidyApplication();
        application.setUser(beneficiary.getUser());
        application.setScheme(scheme);
        application.setEligibilityStatus(report.isEligible());
        application.setApplicationDate(LocalDateTime.now());
        application.setApplicationStatus(ApplicationStatus.FIELD_VERIFICATION);
        return applicationRepository.save(application);
    }

    public List<SubsidyApplication> getApplicationsForStatus(ApplicationStatus status) {
        return applicationRepository.findByApplicationStatus(status);
    }

    public List<SubsidyApplication> getAllApplicationsForAdmin() {
        return applicationRepository.findAll();
    }

    /**
     * 🔒 SEQUENTIAL GOVERNANCE APPROVAL LOOP
     * Field (Approve) -> District (Approve) -> Finance (Approve) -> Approved
     * Reverify -> Always drops straight back down to Field verification stage.
     */
    public void processWorkflowDecision(Long appId, String action, String remarks) {
        SubsidyApplication app = applicationRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid application ID"));

        switch (action.toUpperCase()) {
            case "APPROVE":
                if (app.getApplicationStatus() == ApplicationStatus.FIELD_VERIFICATION) {
                    app.setApplicationStatus(ApplicationStatus.DISTRICT_REVIEW);
                } else if (app.getApplicationStatus() == ApplicationStatus.DISTRICT_REVIEW) {
                    app.setApplicationStatus(ApplicationStatus.FINANCE_PENDING);
                } else if (app.getApplicationStatus() == ApplicationStatus.FINANCE_PENDING) {
                    app.setApplicationStatus(ApplicationStatus.APPROVED);
                }
                break;

            case "REJECT":
                app.setApplicationStatus(ApplicationStatus.REJECTED);
                break;

            case "REVERIFY":
                // Strictly routes the application back down the chain to the initial field desk
                app.setApplicationStatus(ApplicationStatus.FIELD_VERIFICATION);
                break;
        }
        applicationRepository.save(app);
    }
}
