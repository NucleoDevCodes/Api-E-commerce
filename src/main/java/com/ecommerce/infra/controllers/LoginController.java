package com.ecommerce.infra.controllers;

import com.ecommerce.aplication.records.UsersRecords.*;
import com.ecommerce.aplication.services.ServiceAuth;
import com.ecommerce.aplication.services.ServiceUsers;
import com.ecommerce.infra.exceptions.ResourceNotFoundException;
import com.ecommerce.infra.exceptions.UnauthorizedActionException;
import com.ecommerce.model.users.TypeRole;
import com.ecommerce.model.users.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final ServiceUsers serviceUsers;
    private final ServiceAuth authService;

    public LoginController(ServiceUsers serviceUsers, ServiceAuth authService) {
        this.serviceUsers = serviceUsers;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Long> register(@Valid  @RequestBody DataUsersRegister data) {
        Long userId = serviceUsers.registerUser(data);
        logger.info("Usuário registrado com ID: {}", userId);
        return new ResponseEntity<>(userId, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<DataUserResponse> login(@Valid @RequestBody DataUserLogin data, HttpServletRequest request) {
        DataUserResponse response = authService.login(data);

        request.getSession().setAttribute("USER_EMAIL", response.email());

        logger.info("Login realizado com sucesso para o usuário: {}", response.email());
        return ResponseEntity.ok(response);

    }


    @PutMapping("/alterar-senha")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody DataPasswordChanged data,
            @AuthenticationPrincipal Users requester
    ) {

        if (requester == null) {
            logger.warn("Tentativa de alterar senha sem autenticação.");
            throw new UnauthorizedActionException("Usuário deve estar autenticado para alterar senha.");
        }

        serviceUsers.changePassword(data, requester);
        logger.info("Senha alterada com sucesso para o usuário: {}", requester.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Users requester
    ) {
        if (requester == null) {
            logger.warn("Tentativa de deletar conta sem autenticação.");
            throw new UnauthorizedActionException("Usuário deve estar autenticado para deletar conta.");
        }
        boolean isOwner = id.equals(requester.getId());
        boolean isAdmin = requester.getRole() == TypeRole.ADMIN;

        if (!isOwner && !isAdmin) {
            logger.warn("Usuário {} tentou deletar conta {} sem permissão.", requester.getEmail(), id);
            throw new UnauthorizedActionException("Somente o dono do perfil ou admin pode deletar esta conta.");
        }

        serviceUsers.delete(id);
        logger.info("Conta deletada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/usuario/{id}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable("id") Long id,
            @Valid @RequestBody DataUsersRegister data,
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
    public ResponseEntity<DataPublicProfile> getPublicProfile(@PathVariable("id") Long id) {
        Users user = serviceUsers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        var profile = new DataPublicProfile(user.getId(), user.getUsername(), user.getName());
        logger.info("Perfil público consultado. ID: {}", id);

        return ResponseEntity.ok(profile);
    }
}
