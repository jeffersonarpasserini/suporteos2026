package com.curso.suporteos.security;

import com.curso.suporteos.domain.ContaAcesso;
import com.curso.suporteos.repository.ContaAcessoRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaAcessoUserDetailsService implements UserDetailsService {

    private final ContaAcessoRepository repository;

    public ContaAcessoUserDetailsService(ContaAcessoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        ContaAcesso conta = repository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new UsernameNotFoundException("Login ou senha inválidos"));

        String[] autoridades = conta.getPerfis().stream()
                .map(perfil -> "ROLE_" + perfil.name())
                .toArray(String[]::new);

        return User.withUsername(conta.getLogin())
                .password(conta.getSenhaHash())
                .authorities(autoridades)
                .disabled(!conta.podeAutenticar())
                .build();
    }
}
