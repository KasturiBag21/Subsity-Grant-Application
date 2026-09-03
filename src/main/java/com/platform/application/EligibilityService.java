package com.platform.application;

import com.platform.user.Beneficiary;
import com.platform.scheme.Scheme;
import com.platform.scheme.SchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class EligibilityService {

    @Autowired
    private SchemeRepository schemeRepository;

    public EligibilityResult checkEligibility(Beneficiary beneficiary) {
        EligibilityResult result = new EligibilityResult();
        result.setUserEmail(beneficiary.getUser().getEmail());
        result.setTargetScheme(beneficiary.getSchemeName());

        Optional<Scheme> schemeOpt = schemeRepository.findBySchemeName(beneficiary.getSchemeName());

        if (schemeOpt.isEmpty()) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("Error: Scheme pattern target missing from database records.");
            return result;
        }

        Scheme scheme = schemeOpt.get();

        // 1. Boundary Criteria Rejection Checks
        if (beneficiary.getAge() < scheme.getEligibleAgeMin() || beneficiary.getAge() > scheme.getEligibleAgeMax()) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("Rejected: Evaluation drops outside eligible age profile gaps.");
            return result;
        }

        if (beneficiary.getIncome() > scheme.getMaxIncomeThreshold()) {
            result.setEligible(false);
            result.setEligibilityScore(0);
            result.setRemarks("Rejected: Household wealth cap exceeds budget bounds.");
            return result;
        }

        // 2. Inverse Scale Revenue Scoring Matrix
        double incomeRatio = beneficiary.getIncome() / scheme.getMaxIncomeThreshold();
        int finalScore = (int) Math.round((1.0 - incomeRatio) * 100);

        result.setEligibilityScore(finalScore);
        result.setEligible(true);
        result.setRemarks("Passed criteria matching pipeline metrics successfully.");

        return result;
    }
}
