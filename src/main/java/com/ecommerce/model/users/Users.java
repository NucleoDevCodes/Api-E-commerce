package com.ecommerce.model.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Usuarios")
public class Users implements UserDetails {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotBlank
    @Column(name = "nome")
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    @Column(name = "senha")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TypeRole role;

    @Column(name = "token")
    private String token;

    @Column(name = "expiracao_token")
    private LocalDateTime expiracaoToken;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    public void alterPassword(String encryptedpassword) {
        this.password = encryptedpassword;
    }

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
