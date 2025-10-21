package org.sp.payroll_service.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.domain.auth.service.TokenInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for token maintenance tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final TokenInfoService tokenInfoService;

    /**
     * Cleans up expired tokens every hour.
     * This task runs at the beginning of every hour (0 minutes, 0 seconds).
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void cleanupExpiredTokens() {
        try {
            log.info("Starting scheduled token cleanup");
            int deletedCount = tokenInfoService.cleanupExpiredTokens();
            log.info("Token cleanup completed. Deleted {} expired tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during scheduled token cleanup", e);
        }
    }
}