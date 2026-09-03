package com.platform.application;

import com.platform.user.Beneficiary;
import com.platform.user.BeneficiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/eligibility")
public class EligibilityController {

    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @GetMapping("/check")
    public String evaluateUserEligibility(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Fetches linked profile properties by utilizing the mapped relationship metrics
        Optional<Beneficiary> beneficiaryOpt = beneficiaryRepository.findByUserEmail(userDetails.getUsername());

        if (beneficiaryOpt.isEmpty()) {
            model.addAttribute("error", "Beneficiary metrics record profile matching missing.");
            return "eligibility-result";
        }

        Beneficiary beneficiary = beneficiaryOpt.get();
        EligibilityResult evaluationReport = eligibilityService.checkEligibility(beneficiary);

        model.addAttribute("report", evaluationReport);
        model.addAttribute("beneficiary", beneficiary);

        return "eligibility-result";
    }
}
