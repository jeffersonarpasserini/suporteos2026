package com.curso.suporteos.repository;

import com.curso.suporteos.domain.MotivoRevogacaoRefreshToken;
import com.curso.suporteos.domain.SessaoRefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SessaoRefreshTokenRepository
        extends JpaRepository<SessaoRefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select sessao
            from SessaoRefreshToken sessao
            join fetch sessao.conta conta
            join fetch conta.pessoa
            where sessao.tokenHash = :tokenHash
            """)
    Optional<SessaoRefreshToken> buscarPorHashParaAtualizacao(
            @Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SessaoRefreshToken sessao
            set sessao.revogadaEm = :agora,
                sessao.motivoRevogacao = :motivo
            where sessao.familiaId = :familiaId
              and sessao.revogadaEm is null
            """)
    int revogarFamilia(
            @Param("familiaId") String familiaId,
            @Param("agora") Instant agora,
            @Param("motivo") MotivoRevogacaoRefreshToken motivo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SessaoRefreshToken sessao
            set sessao.revogadaEm = :agora,
                sessao.motivoRevogacao = :motivo
            where sessao.conta.id = :contaId
              and sessao.revogadaEm is null
            """)
    int revogarTodasDaConta(
            @Param("contaId") Long contaId,
            @Param("agora") Instant agora,
            @Param("motivo") MotivoRevogacaoRefreshToken motivo);
}
