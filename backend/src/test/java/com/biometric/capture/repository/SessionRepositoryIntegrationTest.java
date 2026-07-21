package com.biometric.capture.repository;

import com.biometric.capture.domain.Session;
import com.biometric.capture.domain.SessionResult;
import com.biometric.capture.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SessionRepositoryIntegrationTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    SessionRepository sessionRepository;

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsOnlySessionsForThatUser() {
        User ana = entityManager.persistAndFlush(new User("Ana", "ana@example.com"));
        User bruno = entityManager.persistAndFlush(new User("Bruno", "bruno@example.com"));

        Session anaSession = entityManager.persistAndFlush(new Session(ana));
        entityManager.persistAndFlush(new Session(bruno));

        List<Session> results = sessionRepository.findByUserIdOrderByCreatedAtDesc(ana.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(anaSession.getId());
        assertThat(results.get(0).getResult()).isEqualTo(SessionResult.PENDING);
    }

    @Test
    void findAllWithUser_eagerlyLoadsUserWithoutLazyInitException() {
        User ana = entityManager.persistAndFlush(new User("Ana", "ana@example.com"));
        entityManager.persistAndFlush(new Session(ana));
        entityManager.clear();

        List<Session> results = sessionRepository.findAllWithUser();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getUser().getName()).isEqualTo("Ana");
    }
}
