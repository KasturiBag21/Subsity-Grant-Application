package com.platform.security;

import com.platform.audit.AuditLogService;
import com.platform.user.User;
import com.platform.user.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final LoginOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${sagar.otp.expiry-minutes:5}")
    private long expiryMinutes;

    @Value("${sagar.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${sagar.otp.resend-seconds:60}")
    private long resendSeconds;

    @Value("${sagar.otp.dev-display:true}")
    private boolean devDisplay;

    @Value("${sagar.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public OtpService(LoginOtpRepository otpRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AuditLogService auditLogService,
                      ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Transactional
    public String issueOtp(String email, boolean resend) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        LoginOtp latest = otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user).orElse(null);
        if (resend && latest != null
                && Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds() < resendSeconds) {
            throw new IllegalStateException("Please wait before requesting another OTP.");
        }

        for (LoginOtp old : otpRepository.findByUserAndUsedFalse(user)) {
            old.setUsed(true);
            otpRepository.save(old);
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        LoginOtp otp = new LoginOtp();
        otp.setUser(user);
        otp.setOtpHash(passwordEncoder.encode(code));
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otp.setAttemptCount(0);
        otp.setUsed(false);
        otpRepository.save(otp);

        sendOtpEmail(user.getEmail(), code);
        auditLogService.recordAs(user.getEmail(), primaryRole(user),
                "AUTHENTICATION", user.getId(), "OTP_ISSUED",
                null, null, "A login OTP was issued.");

        return devDisplay ? code : null;
    }

    @Transactional
    public boolean verifyOtp(String email, String submittedCode) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        LoginOtp otp = otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user).orElse(null);
        if (otp == null) return false;

        LocalDateTime now = LocalDateTime.now();
        if (otp.getExpiresAt().isBefore(now) || otp.getAttemptCount() >= maxAttempts) {
            otp.setUsed(true);
            otpRepository.save(otp);
            auditLogService.recordAs(user.getEmail(), primaryRole(user),
                    "AUTHENTICATION", user.getId(), "OTP_FAILED",
                    null, null, "Login OTP expired or was locked.");
            return false;
        }

        String candidate = submittedCode == null ? "" : submittedCode.trim();
        if (!candidate.matches("\\d{6}") || !passwordEncoder.matches(candidate, otp.getOtpHash())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            if (otp.getAttemptCount() >= maxAttempts) otp.setUsed(true);
            otpRepository.save(otp);
            auditLogService.recordAs(user.getEmail(), primaryRole(user),
                    "AUTHENTICATION", user.getId(), "OTP_FAILED",
                    null, null, "Incorrect login OTP.");
            return false;
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        auditLogService.recordAs(user.getEmail(), primaryRole(user),
                "AUTHENTICATION", user.getId(), "OTP_VERIFIED",
                null, null, "Login OTP verified.");
        return true;
    }

    public boolean isDevDisplay() {
        return devDisplay;
    }

    private void sendOtpEmail(String recipient, String code) {
        if (!mailEnabled) return;

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("Mail is enabled but JavaMailSender is not configured.");
        }

        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("Mail is enabled but spring.mail.username is not configured.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom.trim());
        message.setTo(recipient);
        message.setSubject("SAGAR Portal Login OTP");
        message.setText("Your SAGAR Portal login OTP is " + code
                + ". It expires in " + expiryMinutes
                + " minutes. Do not share this code.");
        sender.send(message);
    }

    private String primaryRole(User user) {
        return user.getRoles() == null || user.getRoles().isEmpty()
                ? "UNKNOWN"
                : user.getRoles().iterator().next();
    }
}
