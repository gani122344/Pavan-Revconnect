package org.revature.revconnect.repository;

import org.revature.revconnect.enums.TransactionStatus;
import org.revature.revconnect.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionRef(String transactionRef);

    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);

    @Query("SELECT t FROM Transaction t WHERE t.sender.id = :userId OR t.receiver.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sender.id = :userId OR t.receiver.id = :userId) AND t.status = :status ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.sender.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findSentByUserId(Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.receiver.id = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findReceivedByUserId(Long userId, Pageable pageable);
}
