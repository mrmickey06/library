package com.successpoint.library.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(unique = true)
    private String mobileNumber;
    
    private String email;
    
    private LocalDate joiningDate;           // Current active period start (updates on renewal)
    private LocalDate originalJoiningDate;   // When student first joined (never changes)
    private LocalDate lastRenewalDate;       // Date of most recent renewal
    private int renewalCount = 0;            // How many times fees renewed
    private double totalFeesCollected = 0;   // Running total of all fees ever paid

    private int feesPeriodMonths; // e.g., 1, 2, or 3 months
    private double feesPaid;      // Current period fees
    private LocalDate dueDate;
    private String applicationStatus = "APPROVED";
    private String feeStatus = "CLEAR";

    // As you requested, default password for all students
    private String password = "SP@123";

    // Generate Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public LocalDate getOriginalJoiningDate() { return originalJoiningDate; }
    public void setOriginalJoiningDate(LocalDate originalJoiningDate) { this.originalJoiningDate = originalJoiningDate; }
    public LocalDate getLastRenewalDate() { return lastRenewalDate; }
    public void setLastRenewalDate(LocalDate lastRenewalDate) { this.lastRenewalDate = lastRenewalDate; }
    public int getRenewalCount() { return renewalCount; }
    public void setRenewalCount(int renewalCount) { this.renewalCount = renewalCount; }
    public double getTotalFeesCollected() { return totalFeesCollected; }
    public void setTotalFeesCollected(double totalFeesCollected) { this.totalFeesCollected = totalFeesCollected; }
    public int getFeesPeriodMonths() { return feesPeriodMonths; }
    public void setFeesPeriodMonths(int feesPeriodMonths) { this.feesPeriodMonths = feesPeriodMonths; }
    public double getFeesPaid() { return feesPaid; }
    public void setFeesPaid(double feesPaid) { this.feesPaid = feesPaid; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }
    public String getFeeStatus() { return feeStatus; }
    public void setFeeStatus(String feeStatus) { this.feeStatus = feeStatus; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
