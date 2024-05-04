package net.dnadas.training_portal.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dnadas.training_portal.model.user.InvitationDao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvitationCleanupService {
  private final InvitationDao invitationDao;
  private final long cleanupInterval = 24 * 60 * 60 * 1000L; // 1 day in milliseconds

  @Transactional(rollbackFor = Exception.class)
  @Scheduled(initialDelay = 0, fixedRate = cleanupInterval)
  public void removeExpiredInvitations() {
    log.info("Removing expired invitations...");
    Integer removedCount = invitationDao.deleteExpiredInvitations(Instant.now());
    log.info(removedCount + " expired invitations were removed");
  }
}
