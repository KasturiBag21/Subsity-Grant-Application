package com.platform.scheme;

import com.platform.application.EligibilityResult;
import com.platform.user.User;
import com.platform.user.Beneficiary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchemeService {

    @Autowired
    private SchemeRepository schemeRepository;

    public List<Scheme> getActiveSchemes() {
        return schemeRepository.findAll();
    }

    public Scheme getSchemeById(Long id) {
        return schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme configuration missing for ID: " + id));
    }

    /**
     * Executes targeted, custom eligibility evaluation routines based on
     * specific, unique parameters configured for individual scheme profiles.
     */
    public EligibilityResult checkEligibility(User user, Scheme scheme) {
        EligibilityResult result = new EligibilityResult();
        result.setUserEmail(user.getEmail() != null ? user.getEmail() : "Anonymous Guest");
        result.setTargetScheme(scheme.getSchemeName());

        Beneficiary beneficiary = user.getBeneficiary();
        if (beneficiary == null) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("REJECTED: Demographic profile parameters missing.");
            return result;
        }

        // Clean up input tags for matching evaluations
        String schemeName = scheme.getSchemeName().trim();
        String userOcc = beneficiary.getOccupation() != null ? beneficiary.getOccupation().trim() : "";
        int age = beneficiary.getAge();
        double income = beneficiary.getIncome();
        boolean passed = true;

        // =========================================================================
        // ROUTE LAYER 1: EDUCATION SUBSIDY RULES ENGINE
        // =========================================================================
        if (schemeName.equalsIgnoreCase("Education Subsidy")) {
            if (age < 18 || age > 30) {
                result.getFailedCriteria().add("Age floor/ceiling constraint match failed (Required: 18-30).");
                passed = false;
            } else {
                result.getPassedCriteria().add("Age check passed within 18-30 range.");
            }

            if (!userOcc.equalsIgnoreCase("Student")) {
                result.getFailedCriteria().add("Occupation mismatch: Educational grant tracking requires 'Student' profile tag.");
                passed = false;
            } else {
                result.getPassedCriteria().add("Verified 'Student' target occupation context.");
            }

            // =========================================================================
            // ROUTE LAYER 2: FARMER SUPPORT GRANT RULES ENGINE
            // =========================================================================
        } else if (schemeName.equalsIgnoreCase("Farmer Support Grant")) {
            if (age < 18 || age > 65) {
                result.getFailedCriteria().add("Age constraint match failed (Required: 18-65).");
                passed = false;
            } else {
                result.getPassedCriteria().add("Age eligibility parameters verified.");
            }

            if (!userOcc.equalsIgnoreCase("Farmer")) {
                result.getFailedCriteria().add("Occupation profile drop: Agriculture pool requires 'Farmer' status.");
                passed = false;
            } else {
                result.getPassedCriteria().add("Verified primary agriculture producer status.");
            }

            // =========================================================================
            // ROUTE LAYER 3: HOUSING ASSISTANCE SCHEME RULES ENGINE
            // =========================================================================
        } else if (schemeName.equalsIgnoreCase("Housing Assistance Scheme")) {
            if (age < 18 || age > 70) {
                result.getFailedCriteria().add("Age parameters out of bounds (Required: 18-70).");
                passed = false;
            } else {
                result.getPassedCriteria().add("Age parameter bounds passed.");
            }
            // Housing requires zero specific occupation barriers, but tightly controls wealth limits

            // =========================================================================
            // ROUTE LAYER 4: WOMEN ENTREPRENEURSHIP GRANT RULES ENGINE
            // =========================================================================
        } else if (schemeName.equalsIgnoreCase("Women Entrepreneurship Grant")) {
            if (age < 18 || age > 60) {
                result.getFailedCriteria().add("Age parameter bounds match failed (Required: 18-60).");
                passed = false;
            } else {
                result.getPassedCriteria().add("Age parameter bounds approved.");
            }

            if (!userOcc.equalsIgnoreCase("Self Employed") && !userOcc.equalsIgnoreCase("Business")) {
                result.getFailedCriteria().add("Occupation profile mismatch: Requires commercial 'Self Employed' indicator.");
                passed = false;
            } else {
                result.getPassedCriteria().add("Verified commercial self-employment background.");
            }

            // =========================================================================
            // ROUTE LAYER 5: SENIOR CITIZEN ASSISTANCE RULES ENGINE
            // =========================================================================
        } else if (schemeName.equalsIgnoreCase("Senior Citizen Assistance")) {
            if (age < 60) {
                result.getFailedCriteria().add("Age threshold gate error: Floor minimum requires 60 years.");
                passed = false;
            } else {
                result.getPassedCriteria().add("Senior age status criteria passed.");
            }
        }

        // =========================================================================
        // GLOBAL FINANCIAL WEALTH CEILING THRESHOLD CROSS-AUDIT
        // =========================================================================
        if (income > scheme.getMaxIncomeThreshold()) {
            result.getFailedCriteria().add("Wealth limit exceeded: Income ₹" + income + " crosses scheme ceiling of ₹" + scheme.getMaxIncomeThreshold());
            passed = false;
        } else {
            result.getPassedCriteria().add("Income criteria verified within ceiling limit.");
        }

        // Global government identity presence validator gate
        if (beneficiary.getGovernmentId() == null || beneficiary.getGovernmentId().trim().isEmpty()) {
            result.getFailedCriteria().add("Identity validation fault: Missing government Aadhaar/PAN key.");
            passed = false;
        } else {
            result.getPassedCriteria().add("Government Identity check passed.");
        }

        // =========================================================================
        // SCORING INTERCEPTOR AND SCORE COMPILATION
        // =========================================================================
        result.setEligible(passed);
        if (passed) {
            // Inverse Linear Scaling Logic: Closer to floor income = Higher priority score rank
            double wealthRatio = income / scheme.getMaxIncomeThreshold();
            int compiledPriorityScore = (int) Math.round((1.0 - wealthRatio) * 100);

            // Guarantee baseline minimum floor index so score never hits flat zero
            if (compiledPriorityScore < 10) compiledPriorityScore = 15;

            result.setEligibilityScore(compiledPriorityScore);
            result.setRemarks("PASSED: Profile successfully metrics-validated. Candidate fast-tracked into system queue.");
        } else {
            result.setEligibilityScore(0);
            result.setRemarks("REJECTED: Pipeline threshold constraint violation. Request dropped from workflow queues.");
        }

        return result;
    }
}
