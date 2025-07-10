package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.DataPasswordChanged;
import com.ecommerce.aplication.records.DataPublicProfile;
import com.ecommerce.aplication.records.DataUserResponse;
import com.ecommerce.aplication.records.DataUsers;
import com.ecommerce.aplication.services.ServiceUsers;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.infra.exceptions.UnauthorizedActionException;
import com.ecommerce.model.users.Users;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final ServiceUsers serviceUsers;
    private final AuthenticationManager authenticationManager;

    public LoginController(ServiceUsers serviceUsers, AuthenticationManager authenticationManager) {
        this.serviceUsers = serviceUsers;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody DataUsers data) {
        Long userId = serviceUsers.registerUser(data);
        logger.info("Usuário registrado com ID: {}", userId);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<DataUserResponse> login(@RequestBody DataUsers data, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(data.email(), data.password())
        );

        Users user = (Users) auth.getPrincipal();

        request.getSession().setAttribute("USER_EMAIL", user.getEmail());

        logger.info("Login realizado com sucesso para o usuário: {}", user.getEmail());

        DataUserResponse userResponse = new DataUserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody DataPasswordChanged data,
            @AuthenticationPrincipal Users user) {
        if (user == null) {
            logger.warn("Tentativa de alterar senha sem autenticação.");
            throw new UnauthorizedActionException("Usuário deve estar autenticado para alterar senha.");
        }
        serviceUsers.changePassword(data, user);
        logger.info("Senha alterada com sucesso para o usuário: {}", user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @AuthenticationPrincipal Users requester) {
        if (requester == null) {
            logger.warn("Tentativa de deletar conta sem autenticação.");
            throw new UnauthorizedActionException("Usuário deve estar autenticado para deletar conta.");
        }
        if (!id.equals(requester.getId())) {
            logger.warn("Tentativa de deletar conta de outro usuário. ID requisitado: {}, ID autenticado: {}", id, requester.getId());
            throw new UnauthorizedActionException("Somente o dono do perfil pode deletar.");
        }
        serviceUsers.delete(id);
        logger.info("Conta deletada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuario/{id}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable("id") Long id,
            @RequestBody DataUsers data,
            @AuthenticationPrincipal Users requester
    ) {
        if (requester == null) {
            logger.warn("Tentativa de atualizar perfil sem autenticação.");
            throw new UnauthorizedActionException("Usuário deve estar autenticado para editar perfil.");
        }
        if (!id.equals(requester.getId())) {
            logger.warn("Tentativa de editar perfil de outro usuário. ID requisitado: {}, ID autenticado: {}", id, requester.getId());
            throw new UnauthorizedActionException("Somente o dono do perfil pode editar.");
        }

        Users user = serviceUsers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setName(data.name());
        serviceUsers.save(user);
        logger.info("Perfil atualizado com sucesso para o usuário: {}", user.getEmail());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<DataPublicProfile> getPublicProfile(@PathVariable Long id) {
        Users user = serviceUsers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        var profile = new DataPublicProfile(user.getId(), user.getUsername(), user.getName());
        logger.info("Perfil público consultado. ID: {}", id);

        return ResponseEntity.ok(profile);
    }
}
