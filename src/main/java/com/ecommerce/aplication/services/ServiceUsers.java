package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.DataPasswordChanged;
import com.ecommerce.aplication.records.DataUsers;
import com.ecommerce.infra.exceptions.RegraNegocio;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class ServiceUsers implements UserDetailsService {

    private final UsersRepositroy usersRepository;
    private final PasswordEncoder encoder;

    public ServiceUsers(UsersRepositroy usersRepository, PasswordEncoder encoder) {
        this.usersRepository = usersRepository;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com e-mail: " + email));
    }

    @Transactional
    public Long registerUser(DataUsers data) {
        if (usersRepository.findByEmail(data.email()).isPresent()) {
            throw new RegraNegocio("E-mail já está em uso.");
        }

        String encryptedPassword = encoder.encode(data.password());

        Users user = new Users();
        user.setName(data.name());
        user.setEmail(data.email());
        user.setPassword(encryptedPassword);
        user.setRole(data.role());

        return usersRepository.save(user).getId();
    }

    public void changePassword(DataPasswordChanged data, Users user) {
        if (!encoder.matches(data.currentPassword(), user.getPassword())) {
            throw new RegraNegocio("Senha atual está incorreta.");
        }

        if (!data.newPassword().equals(data.confirmNewPassword())) {
            throw new RegraNegocio("Nova senha e confirmação não conferem.");
        }

        user.alterPassword(encoder.encode(data.newPassword()));
        usersRepository.save(user);
    }

    public void delete(Long id) {
        usersRepository.deleteById(id);
    }
}
