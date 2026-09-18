package com.platform.web;

import com.platform.application.ApplicationStatus;
import com.platform.application.SubsidyApplication;
import com.platform.application.SubsidyApplicationRepository;
import com.platform.scheme.Scheme;
import com.platform.scheme.SchemeService;
import com.platform.user.Beneficiary;
import com.platform.user.User;
import com.platform.user.UserRegistrationDto;
import com.platform.user.UserRegistrationException;
import com.platform.user.UserRepository;
import com.platform.user.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
public class WebController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private SchemeService schemeService;
    @Autowired private SubsidyApplicationRepository subsidyApplicationRepository;


    // =========================================================
    // PUBLIC HOME PAGE
    // =========================================================
    @GetMapping("/")
    public String showPublicPortal(Model model) {
        model.addAttribute("schemes", schemeService.getActiveSchemes());
        return "index";
    }


    // =========================================================
    // REGISTRATION
    // =========================================================
    @GetMapping("/register")
    public String showRegistrationPage(
            @RequestParam(value = "selectedScheme", required = false) String selectedScheme,
            Model model) {
        UserRegistrationDto dto = new UserRegistrationDto();
        if (selectedScheme != null && !selectedScheme.isBlank()) {
            dto.setSchemeName(selectedScheme);
        }
        model.addAttribute("user", dto);
        model.addAttribute("schemes", schemeService.getActiveSchemes());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserForm(
            @Valid @ModelAttribute("user") UserRegistrationDto dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("schemes", schemeService.getActiveSchemes());
            return "register";
        }

        try {
            User entityUser = new User();
            entityUser.setEmail(dto.getEmail());
            entityUser.setPassword(dto.getPassword());

            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setFullName(dto.getFullName());
            beneficiary.setGovernmentId(dto.getGovernmentId().trim().toUpperCase());
            beneficiary.setContactNumber(dto.getContactNumber());
            beneficiary.setAge(dto.getAge());
            beneficiary.setState(dto.getState());
            beneficiary.setGender(dto.getGender());
            beneficiary.setDisabilityStatus(dto.getDisabilityStatus());
            beneficiary.setResidenceType(dto.getResidenceType());
            beneficiary.setAddress(dto.getAddress());
            beneficiary.setSchemeName(dto.getSchemeName());
            beneficiary.setIncome(dto.getIncome());
            beneficiary.setOccupation(dto.getOccupation());

            beneficiary.setUser(entityUser);
            entityUser.setBeneficiary(beneficiary);

            userService.registerUser(entityUser);
            return "redirect:/login?success";

        } catch (UserRegistrationException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("schemes", schemeService.getActiveSchemes());
            return "register";
        }
    }


    // =========================================================
    // LOGIN
    // =========================================================
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }


    // =========================================================
    // DASHBOARD ROUTER
    // =========================================================
    @GetMapping("/dashboard")
    public String routeUserDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        boolean isOfficer = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FIELD_OFFICER")
                        || a.getAuthority().equals("ROLE_DISTRICT_OFFICER")
                        || a.getAuthority().equals("ROLE_FINANCE_APPROVER")
                        || a.getAuthority().equals("FIELD_OFFICER")
                        || a.getAuthority().equals("DISTRICT_OFFICER")
                        || a.getAuthority().equals("FINANCE_APPROVER"));

        return isOfficer ? "redirect:/dashboard/officer" : "redirect:/dashboard/citizen";
    }


    // =========================================================
    // OFFICER DASHBOARD — Role-Filtered
    // =========================================================
    @GetMapping("/dashboard/officer")
    public String showOfficerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login?error=SessionExpired";

        model.addAttribute("user", user);

        boolean isFieldOfficer   = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("FIELD_OFFICER"));
        boolean isDistrictOfficer = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("DISTRICT_OFFICER") || a.getAuthority().contains("DISTRICT_ADMIN"));
        boolean isFinanceApprover = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("FINANCE_APPROVER"));

        model.addAttribute("fieldQueue",    isFieldOfficer    ? subsidyApplicationRepository.findByApplicationStatus(ApplicationStatus.FIELD_VERIFICATION) : Collections.emptyList());
        model.addAttribute("districtQueue", isDistrictOfficer ? subsidyApplicationRepository.findByApplicationStatus(ApplicationStatus.DISTRICT_REVIEW)      : Collections.emptyList());
        model.addAttribute("financeQueue",  isFinanceApprover ? subsidyApplicationRepository.findByApplicationStatus(ApplicationStatus.FINANCE_PENDING)       : Collections.emptyList());

        return "dashboard-officer";
    }


    // =========================================================
    // CITIZEN DASHBOARD
    // =========================================================
    @GetMapping("/dashboard/citizen")
    public String showCitizenDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login?error=SessionExpired";

        model.addAttribute("user", user);
        model.addAttribute("schemes", schemeService.getActiveSchemes());
        model.addAttribute("trackingLedger", subsidyApplicationRepository.findByUser(user));
        return "dashboard-citizen";
    }


    // =========================================================
    // EDIT CITIZEN PROFILE
    // =========================================================
    @PostMapping("/profile/update")
    public String updateCitizenProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String contactNumber,
            @RequestParam String address,
            @RequestParam String state,
            @RequestParam String occupation,
            @RequestParam double income,
            @RequestParam String schemeName,
            @RequestParam String gender,
            @RequestParam String disabilityStatus,
            @RequestParam String residenceType) {

        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Beneficiary b = user.getBeneficiary();
        if (b != null) {
            b.setFullName(fullName);
            b.setContactNumber(contactNumber);
            b.setAddress(address);
            b.setState(state);
            b.setOccupation(occupation);
            b.setIncome(income);
            b.setSchemeName(schemeName);
            b.setGender(gender);
            b.setDisabilityStatus(disabilityStatus);
            b.setResidenceType(residenceType);
            userRepository.save(user);
        }
        return "redirect:/dashboard/citizen?updated=true";
    }


    // =========================================================
    // ELIGIBILITY FORM
    // =========================================================
    @GetMapping("/eligibility/{schemeId}")
    public String showEligibilityCheckForm(
            @PathVariable Long schemeId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Scheme scheme = schemeService.getSchemeById(schemeId);
        Beneficiary targetBeneficiary = new Beneficiary();

        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            model.addAttribute("user", user);

            if (user != null && user.getBeneficiary() != null) {
                Beneficiary existing = user.getBeneficiary();
                targetBeneficiary.setUser(user);
                targetBeneficiary.setFullName(existing.getFullName());
                targetBeneficiary.setGovernmentId(existing.getGovernmentId());
                targetBeneficiary.setContactNumber(existing.getContactNumber());
                targetBeneficiary.setAge(existing.getAge());
                targetBeneficiary.setState(existing.getState());
                targetBeneficiary.setIncome(existing.getIncome());
                targetBeneficiary.setAddress(existing.getAddress());
                targetBeneficiary.setOccupation(existing.getOccupation());
                targetBeneficiary.setGender(existing.getGender());
                targetBeneficiary.setDisabilityStatus(existing.getDisabilityStatus());
                targetBeneficiary.setResidenceType(existing.getResidenceType());
                targetBeneficiary.setSchemeName(scheme.getSchemeName());
            }
        }

        model.addAttribute("scheme", scheme);
        model.addAttribute("beneficiary", targetBeneficiary);
        return "eligibility-form";
    }


    // =========================================================
    // CHECK ELIGIBILITY + CREATE APPLICATION
    // =========================================================
    @PostMapping("/eligibility/{schemeId}/check")
    public String evaluateFormEligibility(
            @PathVariable Long schemeId,
            @ModelAttribute("beneficiary") Beneficiary inputFormDetails,
            @RequestParam("aadhaarFile") MultipartFile aadhaarFile,
            @RequestParam("bankFile") MultipartFile bankFile,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Scheme scheme = schemeService.getSchemeById(schemeId);
        User registeredUser = null;

        if (userDetails != null) {
            registeredUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            model.addAttribute("user", registeredUser);
        }

        if (registeredUser == null) return "redirect:/login";

        // ── Evaluate individual criteria against this scheme's own rules ──
        boolean ageEligible      = inputFormDetails.getAge() >= scheme.getEligibleAgeMin() && inputFormDetails.getAge() <= scheme.getEligibleAgeMax();
        boolean incomeEligible   = inputFormDetails.getIncome() <= scheme.getMaxIncomeThreshold();
        boolean identityEligible = inputFormDetails.getGovernmentId() != null && !inputFormDetails.getGovernmentId().isBlank();

        // ── Scheme-specific extra criteria (only enforced when the scheme actually requires them) ──
        boolean occupationEligible = true;
        if (scheme.isOccupationRestricted()) {
            String applicantOcc = inputFormDetails.getOccupation() != null ? inputFormDetails.getOccupation().trim() : "";
            occupationEligible = java.util.Arrays.stream(scheme.getRequiredOccupation().split(","))
                    .map(String::trim)
                    .anyMatch(allowed -> allowed.equalsIgnoreCase(applicantOcc));
        }

        boolean genderEligible = true;
        if (scheme.isGenderRestricted()) {
            genderEligible = inputFormDetails.getGender() != null
                    && inputFormDetails.getGender().trim().equalsIgnoreCase(scheme.getRequiredGender());
        }

        boolean disabilityEligible = true;
        if (scheme.isRequiresDisability()) {
            disabilityEligible = inputFormDetails.getDisabilityStatus() != null
                    && inputFormDetails.getDisabilityStatus().trim().equalsIgnoreCase("Yes");
        }

        boolean residenceEligible = true;
        if (scheme.isResidenceRestricted()) {
            residenceEligible = inputFormDetails.getResidenceType() != null
                    && inputFormDetails.getResidenceType().trim().equalsIgnoreCase(scheme.getRequiredResidenceType());
        }

        int totalScore = 0;
        if (ageEligible)      totalScore += 30;
        if (incomeEligible)   totalScore += 40;
        if (identityEligible) totalScore += 30;

        boolean eligible = ageEligible && incomeEligible && identityEligible
                && occupationEligible && genderEligible && disabilityEligible && residenceEligible;

        SubsidyApplication application = new SubsidyApplication();
        application.setUser(registeredUser);
        application.setScheme(scheme);
        application.setEligibilityStatus(eligible);
        application.setApplicationDate(LocalDateTime.now());
        application.setEligibilityScore(totalScore);

        if (eligible) {
            application.setSanctionAmount(scheme.getMaxIncomeThreshold() >= 500000.0 ? 100000.0 : 75000.0);
            application.setApplicationStatus(ApplicationStatus.FIELD_VERIFICATION);
        } else {
            application.setSanctionAmount(0.0);
            application.setApplicationStatus(ApplicationStatus.REJECTED);
        }

        if (aadhaarFile != null && !aadhaarFile.isEmpty()) application.setAadhaarFilename(aadhaarFile.getOriginalFilename());
        if (bankFile   != null && !bankFile.isEmpty())    application.setBankPassbookFilename(bankFile.getOriginalFilename());

        SubsidyApplication savedApplication = subsidyApplicationRepository.save(application);

        // Pass individual criterion results so the template can show accurate green/red ticks
        model.addAttribute("application",       savedApplication);
        model.addAttribute("scheme",            scheme);
        model.addAttribute("eligible",          eligible);
        model.addAttribute("score",             totalScore);
        model.addAttribute("agePass",           ageEligible);
        model.addAttribute("incomePass",        incomeEligible);
        model.addAttribute("identityPass",      identityEligible);
        model.addAttribute("agePts",            ageEligible      ? 30 : 0);
        model.addAttribute("incomePts",         incomeEligible   ? 40 : 0);
        model.addAttribute("identityPts",       identityEligible ? 30 : 0);
        model.addAttribute("occupationPass",    occupationEligible);
        model.addAttribute("genderPass",        genderEligible);
        model.addAttribute("disabilityPass",    disabilityEligible);
        model.addAttribute("residencePass",     residenceEligible);

        return "application-success";
    }


    // =========================================================
    // OFFICER WORKFLOW ACTION
    // =========================================================
    @PostMapping("/officer/action")
    public String executeWorkflowGovernanceAction(
            @RequestParam Long appId,
            @RequestParam String workflowAction) {

        SubsidyApplication app = subsidyApplicationRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        switch (workflowAction.toUpperCase()) {
            case "APPROVE":
                if (app.getApplicationStatus() == ApplicationStatus.FIELD_VERIFICATION)  app.setApplicationStatus(ApplicationStatus.DISTRICT_REVIEW);
                else if (app.getApplicationStatus() == ApplicationStatus.DISTRICT_REVIEW) app.setApplicationStatus(ApplicationStatus.FINANCE_PENDING);
                else if (app.getApplicationStatus() == ApplicationStatus.FINANCE_PENDING) app.setApplicationStatus(ApplicationStatus.APPROVED);
                break;
            case "REJECT":
                app.setApplicationStatus(ApplicationStatus.REJECTED);
                break;
            case "REVERIFY":
                app.setApplicationStatus(ApplicationStatus.FIELD_VERIFICATION);
                break;
        }

        subsidyApplicationRepository.save(app);
        return "redirect:/dashboard";
    }
}




//package com.platform.web;
//
//import com.platform.application.ApplicationStatus;
//import com.platform.application.SubsidyApplication;
//import com.platform.application.SubsidyApplicationRepository;
//import com.platform.scheme.Scheme;
//import com.platform.scheme.SchemeService;
//import com.platform.user.Beneficiary;
//import com.platform.user.User;
//import com.platform.user.UserRegistrationDto;
//import com.platform.user.UserRegistrationException;
//import com.platform.user.UserRepository;
//import com.platform.user.UserService;
//
//import jakarta.validation.Valid;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Controller
//public class WebController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private SchemeService schemeService;
//
//    @Autowired
//    private SubsidyApplicationRepository subsidyApplicationRepository;
//
//
//    // =========================================================
//    // PUBLIC HOME PAGE
//    // =========================================================
//
//    // =========================================================
//    // PUBLIC HOME PAGE + AUTOMATIC SECURITY DESK SEEDER
//    // =========================================================
//    @GetMapping("/")
//    public String showPublicPortal(Model model) {
//        // Officer account seeding is handled once at startup by SchemeDataLoader
//        // (a CommandLineRunner). Seeding here too caused two seeders to drift out
//        // of sync with different role sets for the same accounts — removed.
//        model.addAttribute("schemes", schemeService.getActiveSchemes());
//        return "index";
//    }
//
//
//
//    // =========================================================
//    // REGISTRATION PAGE
//    // =========================================================
//
//    @GetMapping("/register")
//    public String showRegistrationPage(
//            @RequestParam(value = "selectedScheme", required = false) String selectedScheme,
//            Model model) {
//
//        UserRegistrationDto dto = new UserRegistrationDto();
//
//        if (selectedScheme != null && !selectedScheme.isBlank()) {
//            dto.setSchemeName(selectedScheme);
//        }
//
//        model.addAttribute("user", dto);
//        return "register";
//    }
//
//
//    // =========================================================
//    // REGISTER USER
//    // =========================================================
//
//    @PostMapping("/register")
//    public String registerUserForm(
//            @Valid @ModelAttribute("user") UserRegistrationDto dto,
//            BindingResult result,
//            Model model) {
//
//        if (result.hasErrors()) {
//            return "register";
//        }
//
//        try {
//            User entityUser = new User();
//            entityUser.setEmail(dto.getEmail());
//            entityUser.setPassword(dto.getPassword());
//
//            Beneficiary beneficiary = new Beneficiary();
//            beneficiary.setGovernmentId(dto.getGovernmentId().trim().toUpperCase());
//            beneficiary.setContactNumber(dto.getContactNumber());
//            beneficiary.setAge(dto.getAge());
//            beneficiary.setAddress(dto.getAddress());
//            beneficiary.setSchemeName(dto.getSchemeName());
//            beneficiary.setIncome(dto.getIncome());
//            beneficiary.setOccupation(dto.getOccupation());
//
//            beneficiary.setUser(entityUser);
//            entityUser.setBeneficiary(beneficiary);
//
//            userService.registerUser(entityUser);
//            return "redirect:/login?success";
//
//        } catch (UserRegistrationException e) {
//            model.addAttribute("error", e.getMessage());
//            return "register";
//        }
//    }
//
//
//    // =========================================================
//    // LOGIN PAGE
//    // =========================================================
//
//    @GetMapping("/login")
//    public String showLoginPage() {
//        return "login";
//    }
//
//
//    // =========================================================
//    // DASHBOARD
//    // =========================================================
//
//    // =========================================================
//    // 🧭 ROLE-BASED DASHBOARD SPLIT HUB ROUTER
//    // =========================================================
//
//    @GetMapping("/dashboard")
//    public String routeUserDashboard(@AuthenticationPrincipal UserDetails userDetails) {
//        if (userDetails == null) {
//            return "redirect:/login";
//        }
//
//        // 🔍 VERIFIED FIX: Check for BOTH variations ("ROLE_..." and stripped versions) to ensure it never crashes
//        boolean isOfficer = userDetails.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_FIELD_OFFICER")
//                        || a.getAuthority().equals("ROLE_DISTRICT_OFFICER")
//                        || a.getAuthority().equals("ROLE_FINANCE_APPROVER")
//                        || a.getAuthority().equals("FIELD_OFFICER")
//                        || a.getAuthority().equals("DISTRICT_OFFICER")
//                        || a.getAuthority().equals("FINANCE_APPROVER"));
//
//        if (isOfficer) {
//            return "redirect:/dashboard/officer";
//        }
//
//        return "redirect:/dashboard/citizen";
//    }
//
//
//
//
//    // =========================================================
//    // 💼 ADMINISTRATIVE DASHBOARD CONTROL PANEL (Officers Only)
//    // =========================================================
//    @GetMapping("/dashboard/officer")
//    public String showOfficerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        if (userDetails == null) {
//            return "redirect:/login";
//        }
//        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
//        if (user == null) {
//            return "redirect:/login?error=SessionExpired";
//        }
//
//        model.addAttribute("user", user);
//        model.addAttribute("fieldQueue", subsidyApplicationRepository.findByApplicationStatus(ApplicationStatus.FIELD_VERIFICATION));
//        model.addAttribute("districtQueue", subsidyApplicationRepository.findByApplicationStatus(ApplicationStatus.DISTRICT_REVIEW));
//        model.addAttribute("financeQueue", subsidyApplicationRepository.findByApplicationStatus(ApplicationStatus.FINANCE_PENDING));
//
//        return "dashboard-officer";
//    }
//
//    // =========================================================
//    // 👤 CITIZEN BENEFICIARY PORTAL WORKSPACE (Beneficiaries Only)
//    // =========================================================
//
//    // =========================================================
//    // 👤 CITIZEN BENEFICIARY PORTAL WORKSPACE (Beneficiaries Only)
//    // =========================================================
//    @GetMapping("/dashboard/citizen")
//    public String showCitizenDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        if (userDetails == null) {
//            return "redirect:/login";
//        }
//        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
//        if (user == null) {
//            return "redirect:/login?error=SessionExpired";
//        }
//
//        model.addAttribute("user", user);
//        model.addAttribute("schemes", schemeService.getActiveSchemes());
//
//        List<SubsidyApplication> trackingLedger = subsidyApplicationRepository.findByUser(user);
//        model.addAttribute("trackingLedger", trackingLedger);
//
//        return "dashboard-citizen"; // Professional template resolution return
//    }
//
//
//    // =========================================================
//    // ELIGIBILITY FORM
//    // =========================================================
//
//    @GetMapping("/eligibility/{schemeId}")
//    public String showEligibilityCheckForm(
//            @PathVariable Long schemeId,
//            @AuthenticationPrincipal UserDetails userDetails,
//            Model model) {
//
//        Scheme scheme = schemeService.getSchemeById(schemeId);
//        Beneficiary targetBeneficiary = new Beneficiary();
//
//        if (userDetails != null) {
//            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
//            model.addAttribute("user", user);
//
//            if (user != null && user.getBeneficiary() != null) {
//                Beneficiary existing = user.getBeneficiary();
//                targetBeneficiary.setUser(user);
//                targetBeneficiary.setGovernmentId(existing.getGovernmentId());
//                targetBeneficiary.setContactNumber(existing.getContactNumber());
//                targetBeneficiary.setAge(existing.getAge());
//                targetBeneficiary.setIncome(existing.getIncome());
//                targetBeneficiary.setAddress(existing.getAddress());
//                targetBeneficiary.setOccupation(existing.getOccupation());
//                targetBeneficiary.setSchemeName(scheme.getSchemeName());
//            }
//        }
//
//        model.addAttribute("scheme", scheme);
//        model.addAttribute("beneficiary", targetBeneficiary);
//
//        return "eligibility-form";
//    }
//
//
//    // =========================================================
//    // CHECK ELIGIBILITY + CREATE APPLICATION (4-Row Fixed Matrix)
//    // =========================================================
//
//    @PostMapping("/eligibility/{schemeId}/check")
//    public String evaluateFormEligibility(
//            @PathVariable Long schemeId,
//            @ModelAttribute("beneficiary") Beneficiary inputFormDetails,
//            @RequestParam("aadhaarFile") MultipartFile aadhaarFile,
//            @RequestParam("bankFile") MultipartFile bankFile,
//            @RequestParam("jurisdictionState") String jurisdictionState,
//            @AuthenticationPrincipal UserDetails userDetails,
//            Model model) {
//
//        Scheme scheme = schemeService.getSchemeById(schemeId);
//        User registeredUser = null;
//
//        if (userDetails != null) {
//            registeredUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
//            model.addAttribute("user", registeredUser);
//        }
//
//        if (registeredUser == null) {
//            return "redirect:/login";
//        }
//
//        // =====================================================
//        // 👍 FIXED 4-ROW EVALUATION SYSTEM MATRIX LOGIC
//        // =====================================================
//        int totalScore = 0;
//
//        // 1. MIN_AGE CHECK (Weight: 20 pts)
//        boolean ageEligible = inputFormDetails.getAge() >= 21 && inputFormDetails.getAge() <= scheme.getEligibleAgeMax();
//        if (ageEligible) {
//            totalScore += 20;
//        }
//
//        // 2. MAX_INCOME CHECK (Weight: 30 pts)
//        boolean incomeEligible = inputFormDetails.getIncome() <= 600000.0;
//        if (incomeEligible) {
//            totalScore += 30;
//        }
//
//        // 3. STATE_CHECK (Weight: 20 pts)
//        boolean stateEligible = jurisdictionState != null && !jurisdictionState.isBlank();
//        if (stateEligible) {
//            totalScore += 20;
//        }
//
//        // 4. IDENTITY_VERIFIED CHECK (Weight: 30 pts)
//        boolean identityEligible = inputFormDetails.getGovernmentId() != null && !inputFormDetails.getGovernmentId().isBlank();
//        if (identityEligible) {
//            totalScore += 30;
//        }
//
//        boolean eligible = ageEligible && incomeEligible && stateEligible && identityEligible;
//
//        SubsidyApplication application = new SubsidyApplication();
//        application.setUser(registeredUser);
//        application.setScheme(scheme);
//        application.setEligibilityStatus(eligible);
//        application.setApplicationDate(LocalDateTime.now());
//        application.setEligibilityScore(totalScore);
//
//        // 🔍 ADD THIS EXACT LINE HERE TO PREVENT #SUB-null:
////        application.setFieldInspectionRemarks(inputFormDetails.getGovernmentId());
//
//        if (eligible) {
//            if (scheme.getMaxIncomeThreshold() >= 500000.0) {
//                application.setSanctionAmount(100000.0);
//            } else {
//                application.setSanctionAmount(75000.0);
//            }
//            application.setApplicationStatus(ApplicationStatus.FIELD_VERIFICATION);
//        } else {
//            application.setSanctionAmount(0.0);
//            application.setApplicationStatus(ApplicationStatus.REJECTED);
//        }
//
//        if (aadhaarFile != null && !aadhaarFile.isEmpty()) {
//            application.setAadhaarFilename(aadhaarFile.getOriginalFilename());
//        }
//        if (bankFile != null && !bankFile.isEmpty()) {
//            application.setBankPassbookFilename(bankFile.getOriginalFilename());
//        }
//
//        // 🔍 SAVE CODES TO CAPTURE THE ID KEY:
//        SubsidyApplication savedApplication = subsidyApplicationRepository.save(application);
//        model.addAttribute("application", savedApplication);
//        model.addAttribute("scheme", scheme);
//        model.addAttribute("eligible", eligible);
//        model.addAttribute("score", totalScore);
//        model.addAttribute("jurisdictionState", jurisdictionState);
////        subsidyApplicationRepository.save(application);
////        model.addAttribute("application", application);
////        model.addAttribute("scheme", scheme);
////        model.addAttribute("eligible", eligible);
////        model.addAttribute("score", totalScore);
////        model.addAttribute("jurisdictionState", jurisdictionState);
//
//        return "application-success";
//    }
//
//// =========================================================
//// OFFICER WORKFLOW ACTION
//// =========================================================
//
//    // =========================================================
//// UNIFIED OFFICER WORKFLOW ACTION WITH EXACT STEP ROUTINGS
//// =========================================================
//    @PostMapping("/officer/action")
//    public String executeWorkflowGovernanceAction(
//            @RequestParam Long appId,
//            @RequestParam String workflowAction) {
//
//        SubsidyApplication app = subsidyApplicationRepository.findById(appId)
//                .orElseThrow(() -> new IllegalArgumentException("Target application record not found"));
//
//        switch (workflowAction.toUpperCase()) {
//            case "APPROVE":
//                if (app.getApplicationStatus() == ApplicationStatus.FIELD_VERIFICATION) {
//                    app.setApplicationStatus(ApplicationStatus.DISTRICT_REVIEW);
//                } else if (app.getApplicationStatus() == ApplicationStatus.DISTRICT_REVIEW) {
//                    app.setApplicationStatus(ApplicationStatus.FINANCE_PENDING);
//                } else if (app.getApplicationStatus() == ApplicationStatus.FINANCE_PENDING) {
//                    app.setApplicationStatus(ApplicationStatus.APPROVED); // Ready for Disbursement
//                }
//                break;
//
//            case "REJECT":
//                app.setApplicationStatus(ApplicationStatus.REJECTED);
//                break;
//
//            case "REVERIFY":
//                // Demotes the application cleanly back down to the Field Verification desk entrypoint
//                app.setApplicationStatus(ApplicationStatus.FIELD_VERIFICATION);
//                break;
//        }
//
//        subsidyApplicationRepository.save(app);
//        return "redirect:/dashboard";
//    }
//}