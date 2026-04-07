package org.revature.revconnect.repository;

import org.revature.revconnect.model.CollaborationContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaborationContractRepository extends JpaRepository<CollaborationContract, Long> {

    Optional<CollaborationContract> findByCollaborationId(Long collaborationId);

    Optional<CollaborationContract> findTopByCollaborationIdOrderByCreatedAtDesc(Long collaborationId);
}
