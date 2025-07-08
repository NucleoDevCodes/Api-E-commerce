package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataPasswordChanged;
import com.ecommerce.aplication.records.DataPublicProfile;
import com.ecommerce.aplication.records.DataUsers;
import com.ecommerce.aplication.services.ServiceUsers;
import com.ecommerce.infra.exceptions.RegraNegocio;
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

    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            serviceUsers.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RegraNegocio e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/usuario/{id}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable("id") Long id,
            @RequestBody DataUsers data,
            @AuthenticationPrincipal Users requester
    ) {
        if (!id.equals(requester.getId())) {
            throw new RegraNegocio("Somente o dono do perfil pode editar.");
        }

        Users user = serviceUsers.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setName(data.name());
        serviceUsers.save(user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<DataPublicProfile> getPublicProfile(@PathVariable Long id) {
        Users user = serviceUsers.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var profile = new DataPublicProfile(user.getId(), user.getUsername(), user.getName());

        return ResponseEntity.ok(profile);
    }
}
