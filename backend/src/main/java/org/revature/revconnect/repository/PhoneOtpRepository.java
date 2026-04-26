package org.revature.revconnect.repository;

import org.revature.revconnect.model.PhoneOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhoneOtpRepository extends JpaRepository<PhoneOtp, Long> {

    Optional<PhoneOtp> findTopByPhoneOrderByCreatedAtDesc(String phone);

    void deleteByPhone(String phone);

    @Modifying
    @Query("DELETE FROM PhoneOtp p WHERE p.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
