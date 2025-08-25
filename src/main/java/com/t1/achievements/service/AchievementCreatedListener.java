package com.t1.achievements.service;
import com.t1.achievements.RR.AchievementCreatedEvent;
import com.t1.achievements.service.AdminAchievementSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Component
@RequiredArgsConstructor
public class AchievementCreatedListener {

    private final AdminAchievementSeedService seedService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(AchievementCreatedEvent e) {
        if (!e.massSeed()) return;

        seedService.seedProgressZero(e.achievementId(), false);
    }
}
