package org.revature.revconnect.repository;

import org.revature.revconnect.enums.CollaborationStatus;
import org.revature.revconnect.model.Collaboration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {

    Optional<Collaboration> findByBusinessIdAndCreatorId(Long businessId, Long creatorId);

    Page<Collaboration> findByBusinessIdAndStatus(Long businessId, CollaborationStatus status, Pageable pageable);

    Page<Collaboration> findByCreatorIdAndStatus(Long creatorId, CollaborationStatus status, Pageable pageable);

    Page<Collaboration> findByBusinessId(Long businessId, Pageable pageable);

    Page<Collaboration> findByCreatorId(Long creatorId, Pageable pageable);

    List<Collaboration> findByBusinessIdAndStatusIn(Long businessId, List<CollaborationStatus> statuses);

    List<Collaboration> findByCreatorIdAndStatusIn(Long creatorId, List<CollaborationStatus> statuses);

    boolean existsByBusinessIdAndCreatorIdAndStatus(Long businessId, Long creatorId, CollaborationStatus status);
}
