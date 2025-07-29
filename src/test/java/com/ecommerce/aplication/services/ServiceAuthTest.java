package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.UsersRecords.DataUserLogin;
import com.ecommerce.aplication.records.UsersRecords.DataUserResponse;
import com.ecommerce.infra.exceptions.UnauthorizedActionException;
import com.ecommerce.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceAuthTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private ServiceAuth serviceAuth;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_success() {
        DataUserLogin login = new DataUserLogin("user@email.com", "senha123");
        Users user = new Users();
        user.setId(1L);
        user.setName("Nome");
        user.setEmail("user@email.com");
        user.setRole(null);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        DataUserResponse response = serviceAuth.login(login);

        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    void login_fail_throws() {
        DataUserLogin login = new DataUserLogin("user@email.com", "senha123");

        when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () -> serviceAuth.login(login));
        assertEquals("Credenciais inválidas.", ex.getMessage());
    }

}