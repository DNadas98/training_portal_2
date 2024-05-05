package net.dnadas.training_portal.model.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationDao extends JpaRepository<Invitation, Long> {
  Optional<Invitation> findByUsername(String username);

  Optional<Invitation> findByInvitationCodeAndUsername(UUID invitationCode, String username);

  @Modifying
  @Query("DELETE FROM Invitation i WHERE i.expiresAt < :now")
  Integer deleteExpiredInvitations(Instant now);

  Optional<Invitation> findByInvitationCode(UUID invitationCodeUuid);
}
