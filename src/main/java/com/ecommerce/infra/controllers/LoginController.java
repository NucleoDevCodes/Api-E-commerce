package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataPasswordChanged;
import com.ecommerce.aplication.records.DataUsers;
import com.ecommerce.aplication.services.ServiceUsers;
import com.ecommerce.model.users.Users;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final ServiceUsers serviceUsers;
    private final AuthenticationManager authenticationManager;

    public LoginController(ServiceUsers serviceUsers, AuthenticationManager authenticationManager) {
        this.serviceUsers = serviceUsers;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody DataUsers data) {
        Long userId = serviceUsers.registerUser(data);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<Users> login(@RequestBody DataUsers data, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(data.email(), data.password())
        );

        Users user = (Users) auth.getPrincipal();

        request.getSession().setAttribute("USER_EMAIL", user.getEmail());

        return ResponseEntity.ok(user);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody DataPasswordChanged data,
            @AuthenticationPrincipal Users user) {
        serviceUsers.changePassword(data, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceUsers.delete(id);
        return ResponseEntity.noContent().build();
    }
}
