package com.condo.condo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "properties")
@Data
public class Property extends BaseEntity{
    @NotEmpty
    @Column(name = "property_address")
    private String address;

    @Min(1)
    @Column(name = "number_of_rooms")
    private int rooms;

    @Positive
    @Column(name = "rent_per_year")
    private double rent;

    @NotEmpty
    @Column(name = "property_description")
    private String description;

    @NotEmpty
    @Column(name = "image")
    private String images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    // Additional fields: description, images, etc.
}

