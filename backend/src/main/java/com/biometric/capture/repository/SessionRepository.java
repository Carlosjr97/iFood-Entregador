package com.biometric.capture.repository;

import com.biometric.capture.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Session> findAllByOrderByCreatedAtDesc();

    @Query("select s from Session s join fetch s.user order by s.createdAt desc")
    List<Session> findAllWithUser();

    @Query("""
            select u.id as userId, u.name as userName, avg(s.score) as averageScore, count(s) as sessionsCount
            from Session s join s.user u
            where s.result <> com.biometric.capture.domain.SessionResult.PENDING
            group by u.id, u.name
            order by avg(s.score) desc
            """)
    List<UserRankingProjection> findRanking();
}
