package com.platform.admin;

import com.platform.application.ApplicationStatus;
import com.platform.application.SubsidyApplication;
import com.platform.application.SubsidyApplicationRepository;
import com.platform.disbursement.DisbursementAnalyticsService;
import com.platform.scheme.Scheme;
import com.platform.scheme.SchemeService;
import com.platform.user.User;
import com.platform.user.UserRepository;
import com.platform.audit.AuditLogService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private SchemeService schemeService;
    @Autowired private SubsidyApplicationRepository applicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DisbursementAnalyticsService analyticsService;
    @Autowired private AuditLogService auditLogService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "edit", required = false) Long editId,
                            Model model) {
        if (userDetails == null) return "redirect:/login";

        List<Scheme> schemes = schemeService.getAllSchemes();
        List<SubsidyApplication> applications = applicationRepository.findAllByOrderByApplicationDateDesc();
        List<User> users = userRepository.findAll();

        Map<ApplicationStatus, Long> statusCounts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusCounts.put(status, applications.stream()
                    .filter(a -> a.getApplicationStatus() == status)
                    .count());
        }

        SchemeAdminForm form = new SchemeAdminForm();
        if (editId != null) {
            form = SchemeAdminForm.from(schemeService.getSchemeById(editId));
        }

        List<SubsidyApplication> recentApplications = new ArrayList<>(applications);
        recentApplications.sort(Comparator.comparing(SubsidyApplication::getApplicationDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (recentApplications.size() > 12) {
            recentApplications = new ArrayList<>(recentApplications.subList(0, 12));
        }

        model.addAttribute("schemes", schemes);
        model.addAttribute("schemeForm", form);
        model.addAttribute("applications", applications);
        model.addAttribute("recentApplications", recentApplications);
        model.addAttribute("users", users);
        model.addAttribute("statusCounts", statusCounts);
        addNamedStatusCounts(model, statusCounts);
        addRoleCounts(model, users);
        model.addAttribute("analytics", analyticsService.buildSnapshot());
        List<com.platform.audit.AuditLog> auditLogs = auditLogService.getRecentLogs();
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("recentAuditLogs", auditLogs.size() > 8 ? auditLogs.subList(0, 8) : auditLogs);
        model.addAttribute("applicationStatuses", ApplicationStatus.values());
        return "admin-dashboard";
    }

    @PostMapping("/schemes/save")
    public String saveScheme(@Valid @ModelAttribute("schemeForm") SchemeAdminForm form,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            return rebuildDashboardAfterValidation(model, form);
        }

        try {
            boolean creating = form.getId() == null;
            Scheme saved = schemeService.createOrUpdateFromAdmin(form);
            auditLogService.record("SCHEME", saved.getId(), creating ? "SCHEME_CREATED" : "SCHEME_UPDATED",
                    null, saved.getSchemeName(), creating
                            ? "Administrator created a new scheme master record."
                            : "Administrator updated scheme master data.");
            redirectAttributes.addFlashAttribute("success", creating
                    ? "Scheme created successfully."
                    : "Scheme updated successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", readableError(ex));
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/schemes/delete/{id}")
    public String deleteScheme(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Scheme scheme = schemeService.getSchemeById(id);
            String schemeName = scheme.getSchemeName();
            schemeService.deleteByAdmin(id);
            auditLogService.record("SCHEME", id, "SCHEME_DELETED", schemeName, null,
                    "Administrator deleted an unused scheme master record.");
            redirectAttributes.addFlashAttribute("success", "Scheme deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", readableError(ex));
        }
        return "redirect:/admin/dashboard";
    }

    private String rebuildDashboardAfterValidation(Model model, SchemeAdminForm form) {
        List<Scheme> schemes = schemeService.getAllSchemes();
        List<SubsidyApplication> applications = applicationRepository.findAllByOrderByApplicationDateDesc();
        List<User> users = userRepository.findAll();
        Map<ApplicationStatus, Long> statusCounts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusCounts.put(status, applications.stream()
                    .filter(a -> a.getApplicationStatus() == status)
                    .count());
        }
        model.addAttribute("schemes", schemes);
        model.addAttribute("schemeForm", form);
        model.addAttribute("applications", applications);
        model.addAttribute("recentApplications", applications.stream().limit(12).toList());
        model.addAttribute("users", users);
        model.addAttribute("statusCounts", statusCounts);
        addNamedStatusCounts(model, statusCounts);
        addRoleCounts(model, users);
        model.addAttribute("analytics", analyticsService.buildSnapshot());
        List<com.platform.audit.AuditLog> auditLogs = auditLogService.getRecentLogs();
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("recentAuditLogs", auditLogs.size() > 8 ? auditLogs.subList(0, 8) : auditLogs);
        model.addAttribute("applicationStatuses", ApplicationStatus.values());
        return "admin-dashboard";
    }

    private void addNamedStatusCounts(Model model, Map<ApplicationStatus, Long> counts) {
        model.addAttribute("fieldCount", counts.getOrDefault(ApplicationStatus.FIELD_VERIFICATION, 0L));
        model.addAttribute("districtCount", counts.getOrDefault(ApplicationStatus.DISTRICT_REVIEW, 0L));
        model.addAttribute("financeCount", counts.getOrDefault(ApplicationStatus.FINANCE_PENDING, 0L));
        model.addAttribute("disbursementCount", counts.getOrDefault(ApplicationStatus.FUND_DISBURSEMENT, 0L));
        model.addAttribute("approvedCount", counts.getOrDefault(ApplicationStatus.APPROVED, 0L));
        model.addAttribute("rejectedCount", counts.getOrDefault(ApplicationStatus.REJECTED, 0L));
        model.addAttribute("reverificationCount", counts.getOrDefault(ApplicationStatus.APPLICANT_REVERIFICATION, 0L));
    }

    private void addRoleCounts(Model model, List<User> users) {
        long admins = users.stream().filter(u -> hasRole(u, "ROLE_ADMIN")).count();
        long field = users.stream().filter(u -> hasRole(u, "ROLE_FIELD_OFFICER")).count();
        long district = users.stream().filter(u -> hasRole(u, "ROLE_DISTRICT_OFFICER")).count();
        long finance = users.stream().filter(u -> hasRole(u, "ROLE_FINANCE_APPROVER")).count();
        long disbursement = users.stream().filter(u -> hasRole(u, "ROLE_DISBURSEMENT_OFFICER")).count();
        long beneficiaries = users.stream().filter(u -> hasRole(u, "ROLE_BENEFICIARY")).count();
        model.addAttribute("adminUsers", admins);
        model.addAttribute("fieldUsers", field);
        model.addAttribute("districtUsers", district);
        model.addAttribute("financeUsers", finance);
        model.addAttribute("disbursementUsers", disbursement);
        model.addAttribute("beneficiaryUsers", beneficiaries);
    }

    private boolean hasRole(User user, String role) {
        return user.getRoles() != null && user.getRoles().contains(role);
    }

    private String readableError(RuntimeException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "The requested administrator operation could not be completed."
                : ex.getMessage();
    }
}
