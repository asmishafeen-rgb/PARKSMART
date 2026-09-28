package com.parksmart.parksmart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_outs")
public class CheckInOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Long overstayMinutes;
    private BigDecimal penaltyAmount;

    @Enumerated(EnumType.STRING)
    private CheckInOutStatus status;

    @OneToOne(optional = false)
    private Booking booking;

    public Long getId() { return id; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public Long getOverstayMinutes() { return overstayMinutes; }
    public void setOverstayMinutes(Long overstayMinutes) { this.overstayMinutes = overstayMinutes; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }
    public CheckInOutStatus getStatus() { return status; }
    public void setStatus(CheckInOutStatus status) { this.status = status; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}
