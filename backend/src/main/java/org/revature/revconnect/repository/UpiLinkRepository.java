package org.revature.revconnect.repository;

import org.revature.revconnect.model.UpiLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpiLinkRepository extends JpaRepository<UpiLink, Long> {

    List<UpiLink> findByUserId(Long userId);

    Optional<UpiLink> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndUpiId(Long userId, String upiId);
}
