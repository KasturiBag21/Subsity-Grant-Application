package com.platform.application;

import com.platform.user.Beneficiary;
import com.platform.user.BeneficiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/submit")
    public String executeApplicationPipeline(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Optional<Beneficiary> beneficiaryOpt = beneficiaryRepository.findByUserEmail(userDetails.getUsername());
        if (beneficiaryOpt.isEmpty()) {
            return "redirect:/dashboard?error=profile_not_found";
        }
        Beneficiary beneficiary = beneficiaryOpt.get();
        EligibilityResult report = eligibilityService.checkEligibility(beneficiary);

        if (!report.isEligible()) {
            return "redirect:/eligibility/check?rejected=true";
        }

        SubsidyApplication savedApp = eligibilityService.registerAndRouteApplication(beneficiary, report);
        model.addAttribute("application", savedApp);
        model.addAttribute("report", report);
        return "application-success";
    }

    // Role Route 1: Field Officer Ground Verification Desk
    @GetMapping("/officer/field")
    public String showFieldOfficerDesk(Model model) {
        model.addAttribute("apps", eligibilityService.getApplicationsForStatus(ApplicationStatus.FIELD_VERIFICATION));
        model.addAttribute("roleTitle", "Field Verification Officer");
        model.addAttribute("currentQueue", "FIELD_VERIFICATION");
        return "officer-desk";
    }

    // Role Route 2: District Officer Review Core Portal
    @GetMapping("/officer/district")
    public String showDistrictOfficerDesk(Model model) {
        model.addAttribute("apps", eligibilityService.getApplicationsForStatus(ApplicationStatus.DISTRICT_REVIEW));
        model.addAttribute("roleTitle", "District Appraisal Admin");
        model.addAttribute("currentQueue", "DISTRICT_REVIEW");
        return "officer-desk";
    }

    // Role Route 3: Finance Approver Treasury Release desk
    @GetMapping("/officer/finance")
    public String showFinanceOfficerDesk(Model model) {
        model.addAttribute("apps", eligibilityService.getApplicationsForStatus(ApplicationStatus.FINANCE_PENDING));
        model.addAttribute("roleTitle", "Finance Payout Approver");
        model.addAttribute("currentQueue", "FINANCE_PENDING");
        return "officer-desk";
    }

    // Role Route 4: Global System Administrator Monitor Matrix Dashboard
    @GetMapping("/admin/monitor")
    public String showSystemAdminMonitor(Model model) {
        model.addAttribute("apps", eligibilityService.getAllApplicationsForAdmin());
        return "admin-monitor";
    }

    // Unified Workflow Processing Endpoint for Handling Decisions, Rejections, and Re-verifications
    @PostMapping("/officer/action")
    public String processGovernanceAction(@RequestParam Long appId,
                                          @RequestParam String workflowAction,
                                          @RequestParam(required = false) String remarks,
                                          @RequestParam String originQueue) {
        eligibilityService.processWorkflowDecision(appId, workflowAction, remarks);

        // Dynamic redirection to cleanly drop back to your respective role desk workspace
        if ("DISTRICT_REVIEW".equals(originQueue)) return "redirect:/eligibility/officer/district";
        if ("FINANCE_PENDING".equals(originQueue)) return "redirect:/eligibility/officer/finance";
        return "redirect:/eligibility/officer/field";
    }
}
