package com.platform.application;

import com.platform.scheme.SchemeRepository;
import com.platform.user.Beneficiary;
import com.platform.user.BeneficiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Legacy eligibility controller — kept for backward compatibility with any
 * existing routes that reference /eligibility/officer/field etc.
 * New role-specific workflow endpoints are in WebController.
 */
@Controller
@RequestMapping("/eligibility")
public class EligibilityController {

    @Autowired private EligibilityService eligibilityService;
    @Autowired private BeneficiaryRepository beneficiaryRepository;
    @Autowired private SubsidyApplicationRepository applicationRepository;
    @Autowired private SchemeRepository schemeRepository;

    // Legacy field officer route — redirects to new dashboard
    @GetMapping("/officer/field")
    public String showFieldOfficerDesk() {
        return "redirect:/dashboard/officer";
    }

    // Legacy district officer route
    @GetMapping("/officer/district")
    public String showDistrictOfficerDesk() {
        return "redirect:/dashboard/officer";
    }

    // Legacy finance approver route
    @GetMapping("/officer/finance")
    public String showFinanceOfficerDesk() {
        return "redirect:/dashboard/officer";
    }

    // Admin monitor — moved to WebController#showAdminMonitor
    // (GET /eligibility/admin/monitor) to avoid ambiguous mapping.

    // Legacy unified action — now split into role-specific endpoints in WebController
    // Kept here as fallback
    @PostMapping("/officer/action")
    public String legacyProcessGovernanceAction(@RequestParam Long appId,
                                                @RequestParam String workflowAction,
                                                @RequestParam(required = false) String remarks,
                                                @RequestParam(required = false) String originQueue) {
        // Route to appropriate service method based on the origin queue
        if ("DISTRICT_REVIEW".equals(originQueue)) {
            eligibilityService.districtOfficerAction(appId, workflowAction, remarks);
        } else if ("FINANCE_PENDING".equals(originQueue)) {
            eligibilityService.financeApproverAction(appId, workflowAction, remarks, 0);
        } else if ("FUND_DISBURSEMENT".equals(originQueue)) {
            // Milestone 3 is now Finance-owned. Legacy pages must enter the
            // staged control desk instead of bypassing the milestone workflow.
            return "redirect:/milestone3/disbursement/" + appId;
        } else {
            eligibilityService.fieldOfficerAction(appId, workflowAction, remarks);
        }
        return "redirect:/dashboard/officer";
    }
}