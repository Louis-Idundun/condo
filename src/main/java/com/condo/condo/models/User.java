package com.condo.condo.models;

import jakarta.persistence.*;
import com.condo.condo.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User extends BaseEntity implements UserDetails {

    @NotEmpty
    @Column(name = "first_name")
    private String firstName;

    @NotEmpty
    @Column(name = "last_name")
    private String lastName;

    @NotEmpty
    @Column(name = "username")
    private String userName;

    @NotEmpty
    @Email
    @Column(name = "email_address")
    private String emailAddress;

    @NotEmpty
    @Column(name = "password")
    private String password;

    @Column(name = "password_recovery")
    private Boolean passwordRecovery = false;

    @Column(name = "is_owner_verified")
    private Boolean isOwnerVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role; // OWNER, CUSTOMER, ADMIN

    private boolean enabled = true;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Property> properties;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }
}
