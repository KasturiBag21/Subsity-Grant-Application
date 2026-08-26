package org.example.subsidyplatformtest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    @Autowired
    private UserService userService;

    // 🔴 ADD THESE TWO LINES RIGHT HERE TO FIX THE ERROR
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

@PostMapping("/register")
    public String registerUserForm(@Valid @ModelAttribute("user") UserRegistrationDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Map form attributes into our actual database User entity
            User entityUser = new User();
            entityUser.setEmail(dto.getEmail());
            entityUser.setPassword(dto.getPassword()); // Clean value validated cleanly before hashing
            entityUser.setGovernmentId(dto.getGovernmentId().trim().toUpperCase());
            entityUser.setContactNumber(dto.getContactNumber());
            entityUser.setAge(dto.getAge());
            entityUser.setAddress(dto.getAddress());
            entityUser.setSchemeName(dto.getSchemeName());

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
    public String showDashboard(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Try to find the full registration entity rows by their email context
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

        // Safety check: If database lookup returned null, bounce out to prevent a Status 500 crash
        if (user == null) {
            return "redirect:/login?error=SessionExpired";
        }

        model.addAttribute("user", user);
        return "dashboard";
    }

}
