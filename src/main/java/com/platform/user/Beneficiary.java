package com.platform.user;

import jakarta.persistence.*;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String governmentId;

    // FIX: Added missing fields that WebController.java requires.
    // Without these, setFullName() and setState() cause compile errors
    // and fullName/state are never persisted to the database.
    private String fullName;
    private String state;

    // NEW: needed to evaluate scheme-specific criteria (gender/disability/residence requirements)
    private String gender;            // Male / Female / Other
    private String disabilityStatus;  // "Yes" or "No"
    private String residenceType;     // Urban / Rural

    private String contactNumber;
    private int age;
    private String address;
    private String schemeName;
    private double income;
    private String occupation;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGovernmentId() { return governmentId; }
    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDisabilityStatus() { return disabilityStatus; }
    public void setDisabilityStatus(String disabilityStatus) { this.disabilityStatus = disabilityStatus; }

    public String getResidenceType() { return residenceType; }
    public void setResidenceType(String residenceType) { this.residenceType = residenceType; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }

    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}



//package com.platform.user;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "beneficiaries")
//public class Beneficiary {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true, nullable = false)
//    private String governmentId;
//
//    private String contactNumber;
//    private int age;
//    private String address;
//    private String schemeName;
//    private double income;
//    private String occupation;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    // Getters and Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getGovernmentId() { return governmentId; }
//    public void setGovernmentId(String governmentId) { this.governmentId = governmentId; }
//    public String getContactNumber() { return contactNumber; }
//    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
//    public int getAge() { return age; }
//    public void setAge(int age) { this.age = age; }
//    public String getAddress() { return address; }
//    public void setAddress(String address) { this.address = address; }
//    public String getSchemeName() { return schemeName; }
//    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
//    public double getIncome() { return income; }
//    public void setIncome(double income) { this.income = income; }
//    public String getOccupation() { return occupation; }
//    public void setOccupation(String occupation) { this.occupation = occupation; }
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//}