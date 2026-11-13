package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.Role;
import br.ufscar.streaming.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

/**
 * Repositório JPA para operações de persistência da entidade {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(Long id);
    long countByRoleAndBlockedFalse(Role role);
}
