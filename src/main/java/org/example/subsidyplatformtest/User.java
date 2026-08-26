package org.example.subsidyplatformtest;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Accepts either an Aadhaar Card (12 continuous digits) OR a PAN Card (5 letters, 4 digits, 1 letter)
    @NotBlank(message = "Government ID cannot be empty")
    @Pattern(regexp = "^([0-9]{12}|[A-Z]{5}[0-9]{4}[A-Z]{1})$",
            message = "Must be a 12-digit Aadhaar Number or a valid PAN Card (e.g., ABCDE1234F)")
    @Column(nullable = false, unique = true)
    private String governmentId;

    @NotBlank(message = "Contact number cannot be empty")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Contact number must be a valid 10-digit mobile number starting with 6-9")
    @Column(nullable = false)
    private String contactNumber;

    // Requires at least one letter, followed by at least one digit, then an @ provider like gmail, yahoo, outlook, or hotmail
    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+[0-9]+@(gmail|yahoo|outlook|hotmail)\\.com$",
            message = "Email must be professional and contain name followed by digits (e.g., name123@gmail.com)")
    @Column(nullable = false, unique = true)
    private String email;


    // Keep validation broad enough for the encrypted database string
    @NotBlank(message = "Password cannot be empty")
    @Column(nullable = false, length = 100) // Changed to 100 to fit the secure 60-character hash string safely!
    private String password;


    // Integer automatically blocks text letters. Added range protection.
    @Min(value = 18, message = "Age must be at least 18 years to enroll")
    @Max(value = 100, message = "Please enter a valid age under 100")
    private int age;

    @NotBlank(message = "Residential address is required")
    @Size(min = 5, max = 150, message = "Address must be between 5 and 150 characters")
    private String address;

    @NotBlank(message = "Please select a specific subsidy scheme")
    private String schemeName;
}
