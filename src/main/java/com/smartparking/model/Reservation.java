
package com.smartparking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "vehicle")
    private String vehicle;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "slot")
    private String slot;

    @Column(name = "parking_hours")
    private int parkingHours;

    @Column(name = "amount")
    private double amount;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    public Reservation() {
    }

    public Reservation(String name, String vehicle,
                       String vehicleType, String slot,
                       int parkingHours, double amount) {
        this.name = name;
        this.vehicle = vehicle;
        this.vehicleType = vehicleType;
        this.slot = slot;
        this.parkingHours = parkingHours;
        this.amount = amount;

        this.startTime = LocalDateTime.now();
        this.endTime = this.startTime.plusHours(parkingHours);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public int getParkingHours() {
        return parkingHours;
    }

    public void setParkingHours(int parkingHours) {
        this.parkingHours = parkingHours;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
