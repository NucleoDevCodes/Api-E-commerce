package com.ecommerce.aplication.services;


import com.ecommerce.aplication.records.UsersRecords.DataUserLogin;
import com.ecommerce.aplication.records.UsersRecords.DataUserResponse;
import com.ecommerce.infra.exceptions.UnauthorizedActionException;
import com.ecommerce.model.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class ServiceAuth {

    private final Logger logger = LoggerFactory.getLogger(ServiceAuth.class);
    private final AuthenticationManager authenticationManager;

    public ServiceAuth(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public DataUserResponse login(DataUserLogin data) {
        try {
            logger.info("Tentativa de login para o e-mail: {}", data.email());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.email(), data.password())
            );

            Users user = (Users) authentication.getPrincipal();
            logger.info("Login realizado com sucesso para o usuário ID: {}", user.getId());

            return new DataUserResponse(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getRole());

        } catch (AuthenticationException ex) {
            logger.warn("Falha na autenticação para o e-mail: {}", data.email());
            throw new UnauthorizedActionException("Credenciais inválidas.");
        }

    }
}
