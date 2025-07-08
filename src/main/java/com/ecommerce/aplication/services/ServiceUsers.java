package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.DataPasswordChanged;
import com.ecommerce.aplication.records.DataUsers;
import com.ecommerce.infra.exceptions.RegraNegocio;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
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
        return usersRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado ou inativo com e-mail: " + email));
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
        user.setAtivo(true);

        return usersRepository.save(user).getId();
    }

    public void changePassword(DataPasswordChanged data, Users user) {
        if (!user.isAtivo()) {
            throw new RegraNegocio("Usuário inativo não pode alterar senha.");
        }

        if (!encoder.matches(data.currentPassword(), user.getPassword())) {
            throw new RegraNegocio("Senha atual está incorreta.");
        }

        if (!data.newPassword().equals(data.confirmNewPassword())) {
            throw new RegraNegocio("Nova senha e confirmação não conferem.");
        }

        user.alterPassword(encoder.encode(data.newPassword()));
        usersRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RegraNegocio("Usuário não encontrado."));
        if (!user.isAtivo()) {
            throw new RegraNegocio("Usuário já está inativo.");
        }
        user.setAtivo(false);
        usersRepository.save(user);
    }

    public java.util.Optional<Users> findById(Long id) {
        return usersRepository.findByIdAndAtivoTrue(id);
    }

    public Users save(Users user) {
        if (user == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
        return usersRepository.save(user);
    }
}
