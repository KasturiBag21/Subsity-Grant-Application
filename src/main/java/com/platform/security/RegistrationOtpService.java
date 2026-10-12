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
public class RegistrationOtpService {

    public static final String PENDING_EMAIL = "SAGAR_REGISTRATION_OTP_EMAIL";
    public static final String DEV_OTP = "SAGAR_REGISTRATION_DEV_OTP";

    private final RegistrationOtpRepository otpRepository;
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

    public RegistrationOtpService(RegistrationOtpRepository otpRepository,
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
                .orElseThrow(() -> new IllegalArgumentException("Pending registration not found."));

        if (!"PENDING_EMAIL_VERIFICATION".equals(user.getAccountStatus())) {
            throw new IllegalStateException("This account is not awaiting email verification.");
        }

        RegistrationOtp latest =
                otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user).orElse(null);

        if (resend && latest != null
                && Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds() < resendSeconds) {
            throw new IllegalStateException("Please wait before requesting another OTP.");
        }

        for (RegistrationOtp old : otpRepository.findByUserAndUsedFalse(user)) {
            old.setUsed(true);
            otpRepository.save(old);
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        RegistrationOtp otp = new RegistrationOtp();
        otp.setUser(user);
        otp.setOtpHash(passwordEncoder.encode(code));
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otp.setAttemptCount(0);
        otp.setUsed(false);
        otpRepository.save(otp);

        sendEmail(email, code);

        auditLogService.recordAs(email, "ROLE_BENEFICIARY",
                "REGISTRATION", user.getId(), "REGISTRATION_OTP_ISSUED",
                null, null, "Citizen registration OTP issued.");

        return devDisplay ? code : null;
    }

    @Transactional
    public boolean verifyAndActivate(String email, String submittedCode) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !"PENDING_EMAIL_VERIFICATION".equals(user.getAccountStatus())) {
            return false;
        }

        RegistrationOtp otp =
                otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user).orElse(null);
        if (otp == null) return false;

        if (otp.getExpiresAt().isBefore(LocalDateTime.now()) || otp.getAttemptCount() >= maxAttempts) {
            otp.setUsed(true);
            otpRepository.save(otp);
            auditLogService.recordAs(email, "ROLE_BENEFICIARY",
                    "REGISTRATION", user.getId(), "REGISTRATION_OTP_FAILED",
                    null, null, "Citizen registration OTP expired or was locked.");
            return false;
        }

        String candidate = submittedCode == null ? "" : submittedCode.trim();
        if (!candidate.matches("\\d{6}") || !passwordEncoder.matches(candidate, otp.getOtpHash())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            if (otp.getAttemptCount() >= maxAttempts) otp.setUsed(true);
            otpRepository.save(otp);
            auditLogService.recordAs(email, "ROLE_BENEFICIARY",
                    "REGISTRATION", user.getId(), "REGISTRATION_OTP_FAILED",
                    null, null, "Incorrect citizen registration OTP.");
            return false;
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        user.setAccountStatus("ACTIVE");
        userRepository.save(user);

        auditLogService.recordAs(email, "ROLE_BENEFICIARY",
                "REGISTRATION", user.getId(), "EMAIL_VERIFIED",
                null, null, "Citizen email verified and account activated.");

        return true;
    }

    public boolean isDevDisplay() {
        return devDisplay;
    }

    private void sendEmail(String recipient, String code) {
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
        message.setSubject("SAGAR Portal Registration OTP");
        message.setText("Your SAGAR Portal registration OTP is " + code
                + ". It expires in " + expiryMinutes
                + " minutes. Do not share this code.");
        sender.send(message);
    }
}
