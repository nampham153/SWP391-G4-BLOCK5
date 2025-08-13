package com.example.swp.entity;

import com.example.swp.enums.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity

public class Staff implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int staffid;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    @Enumerated(EnumType.STRING)
    private RoleName roleName;
    private Boolean sex;
    private String idCitizenCard;


    @OneToMany(mappedBy = "staff")
    private List<Contact> contacts;

    @OneToMany(mappedBy = "staff")
    private List<Feedback> feedbacks;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("STAFF"));
    }

    @OneToMany(mappedBy = "staff")
    @JsonIgnore  // ✅ Ngăn JSON vòng lặp
    private List<Attendance> attendances;

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
