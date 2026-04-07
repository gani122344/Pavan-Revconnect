package org.revature.revconnect.repository;

import org.revature.revconnect.enums.PromotionStatus;
import org.revature.revconnect.model.PostPromotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostPromotionRepository extends JpaRepository<PostPromotion, Long> {

    Optional<PostPromotion> findByPostIdAndCreatorId(Long postId, Long creatorId);

    List<PostPromotion> findByPostIdAndStatus(Long postId, PromotionStatus status);

    Page<PostPromotion> findByCreatorIdAndStatus(Long creatorId, PromotionStatus status, Pageable pageable);

    Page<PostPromotion> findByBusinessId(Long businessId, Pageable pageable);

    Page<PostPromotion> findByCreatorId(Long creatorId, Pageable pageable);

    boolean existsByPostIdAndCreatorIdAndStatus(Long postId, Long creatorId, PromotionStatus status);

    List<PostPromotion> findByCollaborationId(Long collaborationId);
}
