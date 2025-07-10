package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.DataPasswordChanged;
import com.ecommerce.aplication.records.DataUsers;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class ServiceUsers implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUsers.class);

    private final UsersRepositroy usersRepository;
    private final PasswordEncoder encoder;

    public ServiceUsers(UsersRepositroy usersRepository, PasswordEncoder encoder) {
        this.usersRepository = usersRepository;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Carregando usuário pelo e-mail: {}", email);
        return usersRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado ou inativo com e-mail: " + email));
    }

    @Transactional
    public Long registerUser(DataUsers data) {
        logger.info("Registrando novo usuário com email: {}", data.email());
        if (usersRepository.findByEmail(data.email()).isPresent()) {
            logger.warn("Tentativa de registro com email já em uso: {}", data.email());
            throw new BusinessRuleException("E-mail já está em uso.");
        }

        String encryptedPassword = encoder.encode(data.password());

        Users user = new Users();
        user.setName(data.name());
        user.setEmail(data.email());
        user.setPassword(encryptedPassword);
        user.setRole(data.role());
        user.setAtivo(true);

        Long savedId = usersRepository.save(user).getId();
        logger.info("Usuário registrado com sucesso. ID: {}", savedId);
        return savedId;
    }

    public void changePassword(DataPasswordChanged data, Users user) {
        logger.info("Alteração de senha solicitada para usuário ID: {}", user.getId());
        if (!user.isAtivo()) {
            logger.warn("Usuário inativo tentou alterar senha. ID: {}", user.getId());
            throw new BusinessRuleException("Usuário inativo não pode alterar senha.");
        }

        if (!encoder.matches(data.currentPassword(), user.getPassword())) {
            logger.warn("Senha atual incorreta para usuário ID: {}", user.getId());
            throw new BusinessRuleException("Senha atual está incorreta.");
        }

        if (!data.newPassword().equals(data.confirmNewPassword())) {
            logger.warn("Nova senha e confirmação não conferem para usuário ID: {}", user.getId());
            throw new BusinessRuleException("Nova senha e confirmação não conferem.");
        }

        user.alterPassword(encoder.encode(data.newPassword()));
        usersRepository.save(user);
        logger.info("Senha alterada com sucesso para usuário ID: {}", user.getId());
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Solicitação de exclusão (inativação) do usuário ID: {}", id);
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Usuário não encontrado."));
        if (!user.isAtivo()) {
            logger.warn("Tentativa de excluir usuário já inativo. ID: {}", id);
            throw new BusinessRuleException("Usuário já está inativo.");
        }
        user.setAtivo(false);
        usersRepository.save(user);
        logger.info("Usuário inativado com sucesso. ID: {}", id);
    }

    public Optional<Users> findById(Long id) {
        logger.debug("Buscando usuário ativo pelo ID: {}", id);
        return usersRepository.findByIdAndAtivoTrue(id);
    }

    public Users save(Users user) {
        if (user == null) {
            logger.error("Tentativa de salvar usuário nulo");
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        logger.debug("Salvando usuário ID: {}", user.getId());
        return usersRepository.save(user);
    }
}
