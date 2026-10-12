package com.platform.security;

import com.platform.config.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OtpController {

    private final OtpService otpService;
    private final CustomUserDetailsService userDetailsService;

    public OtpController(OtpService otpService,
                         CustomUserDetailsService userDetailsService) {
        this.otpService = otpService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/verify-otp")
    public String showOtpPage(HttpSession session) {
        String email = (String) session.getAttribute(OtpAuthenticationSuccessHandler.PENDING_EMAIL);
        return email == null ? "redirect:/login" : "redirect:/login?otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        String email = session == null ? null
                : (String) session.getAttribute(OtpAuthenticationSuccessHandler.PENDING_EMAIL);

        if (email == null) return "redirect:/login";

        if (!otpService.verifyOtp(email, otp)) {
            redirectAttributes.addFlashAttribute("otpError", "Invalid, expired or locked OTP.");
            return "redirect:/login?otp";
        }

        var details = userDetailsService.loadUserByUsername(email);
        var authentication = new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        session.removeAttribute(OtpAuthenticationSuccessHandler.PENDING_EMAIL);
        session.removeAttribute(OtpAuthenticationSuccessHandler.DEV_OTP);

        return "redirect:/dashboard";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute(OtpAuthenticationSuccessHandler.PENDING_EMAIL);
        if (email == null) return "redirect:/login";

        try {
            String developmentOtp = otpService.issueOtp(email, true);
            if (developmentOtp != null) {
                session.setAttribute(OtpAuthenticationSuccessHandler.DEV_OTP, developmentOtp);
            } else {
                session.removeAttribute(OtpAuthenticationSuccessHandler.DEV_OTP);
            }
            return "redirect:/login?otp&resent";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("otpError", ex.getMessage());
            return "redirect:/login?otp";
        }
    }

}
