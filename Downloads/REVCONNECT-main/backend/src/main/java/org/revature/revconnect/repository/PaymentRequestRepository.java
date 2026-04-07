package org.revature.revconnect.repository;

import org.revature.revconnect.enums.PaymentRequestStatus;
import org.revature.revconnect.model.PaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.payer.id = :userId AND pr.status = :status ORDER BY pr.createdAt DESC")
    Page<PaymentRequest> findPendingForUser(Long userId, PaymentRequestStatus status, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.requester.id = :userId OR pr.payer.id = :userId ORDER BY pr.createdAt DESC")
    Page<PaymentRequest> findByUserId(Long userId, Pageable pageable);

    long countByPayerIdAndStatus(Long payerId, PaymentRequestStatus status);
}
