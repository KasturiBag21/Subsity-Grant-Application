package com.platform.web;

import com.platform.application.ApplicationStatus;
import com.platform.application.EligibilityResult;
import com.platform.application.EligibilityService;
import com.platform.application.SubsidyApplication;
import com.platform.application.SubsidyApplicationRepository;
import com.platform.scheme.Scheme;
import com.platform.scheme.SchemeService;
import com.platform.user.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeService schemeService;

    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private SubsidyApplicationRepository subsidyApplicationRepository;

    @GetMapping("/")
    public String showPublicPortal(Model model) {
        model.addAttribute("schemes", schemeService.getActiveSchemes());
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationPage(@RequestParam(value = "selectedScheme", required = false) String selectedScheme, Model model) {
        UserRegistrationDto dto = new UserRegistrationDto();
        if (selectedScheme != null) {
            dto.setSchemeName(selectedScheme);
        }
        model.addAttribute("user", dto);
        return "register";
    }

    @PostMapping("/register")
    public String registerUserForm(
            @Valid @ModelAttribute("user") UserRegistrationDto dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            User entityUser = new User();
            entityUser.setEmail(dto.getEmail());
            entityUser.setPassword(dto.getPassword());

            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setGovernmentId(dto.getGovernmentId().trim().toUpperCase());
            beneficiary.setContactNumber(dto.getContactNumber());
            beneficiary.setAge(dto.getAge());
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
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElse(null);

        if (user == null) {
            return "redirect:/login?error=SessionExpired";
        }

        model.addAttribute("user", user);
        model.addAttribute("schemes", schemeService.getActiveSchemes());

        return "dashboard";
    }

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
                targetBeneficiary.setUser(user);
                targetBeneficiary.setGovernmentId(user.getBeneficiary().getGovernmentId());
                targetBeneficiary.setContactNumber(user.getBeneficiary().getContactNumber());
                targetBeneficiary.setAge(user.getBeneficiary().getAge());
            }
        } else {
            User anonymousUser = new User();
            anonymousUser.setEmail("Guest");
            Beneficiary blankBeneficiary = new Beneficiary();
            blankBeneficiary.setGovernmentId("");
            anonymousUser.setBeneficiary(blankBeneficiary);
            model.addAttribute("user", anonymousUser);
            targetBeneficiary.setUser(anonymousUser);
        }

        String targetName = scheme.getSchemeName().trim();
        if (targetName.equalsIgnoreCase("Education Subsidy")) {
            targetBeneficiary.setOccupation("Student");
        } else if (targetName.equalsIgnoreCase("Farmer Support Grant")) {
            targetBeneficiary.setOccupation("Farmer");
        } else if (targetName.equalsIgnoreCase("Women Entrepreneurship Grant")) {
            targetBeneficiary.setOccupation("Self Employed");
        } else {
            targetBeneficiary.setOccupation("Any");
        }

        model.addAttribute("scheme", scheme);
        model.addAttribute("beneficiary", targetBeneficiary);
        return "eligibility-form";
    }

    @PostMapping("/eligibility/{schemeId}/check")
    public String evaluateFormEligibility(
            @PathVariable Long schemeId,
            @ModelAttribute("beneficiary") Beneficiary inputFormDetails,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Scheme scheme = schemeService.getSchemeById(schemeId);

        if (userDetails != null) {
            User registeredUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            model.addAttribute("user", registeredUser);

            if (registeredUser != null && registeredUser.getBeneficiary() != null) {
                String regId = registeredUser.getBeneficiary().getGovernmentId().trim().toUpperCase();
                String inputId = inputFormDetails.getGovernmentId().trim().toUpperCase();

                if (!regId.equals(inputId)) {
                    model.addAttribute("scheme", scheme);
                    model.addAttribute("error", "Security Mismatch: Entered Identity Key does not match your registered Aadhaar/PAN configuration profile!");
                    model.addAttribute("beneficiary", inputFormDetails);
                    return "eligibility-form";
                }
            }
        } else {
            User anonymousUser = new User();
            anonymousUser.setEmail("Guest");
            model.addAttribute("user", anonymousUser);
        }

        User tempUser = new User();
        tempUser.setBeneficiary(inputFormDetails);
        if (userDetails != null) {
            tempUser.setEmail(userDetails.getUsername());
        }

        // FIX: Routes validation logic through schemeService to populate criteria checklist arrays safely
        EligibilityResult result = schemeService.checkEligibility(tempUser, scheme);

        model.addAttribute("scheme", scheme);
        model.addAttribute("result", result);
        model.addAttribute("inputDetails", inputFormDetails);

        return "eligibility-result";
    }

    @PostMapping("/apply/{schemeId}")
    public String applyForScheme(
            @PathVariable Long schemeId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElse(null);

        if (user == null || user.getBeneficiary() == null) {
            return "redirect:/login?error=SessionExpired";
        }

        Scheme scheme = schemeService.getSchemeById(schemeId);
        // FIX: Evaluates through schemeService custom evaluation checks
        EligibilityResult result = schemeService.checkEligibility(user, scheme);

        if (!result.isEligible()) {
            model.addAttribute("user", user);
            model.addAttribute("scheme", scheme);
            model.addAttribute("result", result);
            model.addAttribute("error", "You are not eligible for this scheme.");
            return "eligibility-result";
        }

        SubsidyApplication application = new SubsidyApplication();
        application.setUser(user);
        application.setScheme(scheme);
        application.setEligibilityStatus(true);
        application.setApplicationStatus(ApplicationStatus.SUBMITTED);
        application.setApplicationDate(java.time.LocalDateTime.now());

        subsidyApplicationRepository.save(application);

        model.addAttribute("application", application);
        return "application-success";
    }
}
