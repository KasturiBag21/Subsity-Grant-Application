package org.example.subsidyplatformtest;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+[0-9]+@(gmail|yahoo|outlook|hotmail)\\.com$",
            message = "Email must contain name followed by digits (e.g., name123@gmail.com)")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 15, message = "Password length must be between 8 and 15 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "Password must include uppercase, lowercase, number, and special character")
    private String password;

    @NotBlank(message = "Government ID cannot be empty")
    @Pattern(regexp = "^([0-9]{12}|[A-Z]{5}[0-9]{4}[A-Z]{1})$",
            message = "Must be a 12-digit Aadhaar or a valid PAN Card Layout")
    private String governmentId;

    @NotBlank(message = "Contact number cannot be empty")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Contact number must be a valid 10-digit mobile number")
    private String contactNumber;

    @Min(value = 18, message = "Age must be at least 18 years")
    private int age;

    @NotBlank(message = "Residential address is required")
    private String address;

    @NotBlank(message = "Please select a specific subsidy scheme")
    private String schemeName;
}
