package org.revature.revconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.*;
import org.revature.revconnect.enums.*;
import org.revature.revconnect.exception.ResourceNotFoundException;
import org.revature.revconnect.model.*;
import org.revature.revconnect.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationService {

    private final CollaborationRepository collaborationRepo;
    private final CollaborationContractRepository contractRepo;
    private final PostPromotionRepository promotionRepo;
    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final NotificationService notificationService;

    // ═══════ BUSINESS: Invite Creator ═══════
    @Transactional
    public CollaborationResponse inviteCreator(Long businessId, CollaborationRequest request) {
        User business = userRepo.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        if (business.getUserType() != UserType.BUSINESS) {
            throw new IllegalStateException("Only BUSINESS users can invite creators");
        }

        User creator = userRepo.findById(request.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));
        if (creator.getUserType() != UserType.CREATOR) {
            throw new IllegalStateException("Target user is not a CREATOR");
        }

        // Check existing collaboration
        collaborationRepo.findByBusinessIdAndCreatorId(businessId, creator.getId())
                .ifPresent(existing -> {
                    if (existing.getStatus() == CollaborationStatus.ACTIVE) {
                        throw new IllegalStateException("Active collaboration already exists");
                    }
                    if (existing.getStatus() == CollaborationStatus.PENDING) {
                        throw new IllegalStateException("Pending invitation already exists");
                    }
                });

        // Create collaboration
        Collaboration collab = Collaboration.builder()
                .business(business)
                .creator(creator)
                .status(CollaborationStatus.PENDING)
                .message(request.getMessage())
                .build();
        collab = collaborationRepo.save(collab);

        // Create contract
        CollaborationContract contract = CollaborationContract.builder()
                .collaboration(collab)
                .promotionRules(request.getPromotionRules() != null ? request.getPromotionRules() : "Standard promotion rules apply.")
                .contentGuidelines(request.getContentGuidelines())
                .fixedFee(request.getFixedFee() != null ? request.getFixedFee() : 0.0)
                .ratePerView(request.getRatePerView() != null ? request.getRatePerView() : 0.0)
                .viewMilestone(request.getViewMilestone() != null ? request.getViewMilestone() : 10)
                .paymentSchedule(request.getPaymentSchedule() != null ? request.getPaymentSchedule() : "MONTHLY")
                .brandName(request.getBrandName())
                .brandWebsite(request.getBrandWebsite())
                .durationDays(request.getDurationDays() != null ? request.getDurationDays() : 30)
                .build();
        contractRepo.save(contract);

        // Notify creator
        notificationService.createNotification(creator, business, NotificationType.COLLABORATION_INVITE,
                business.getName() + " invited you to collaborate", collab.getId());

        log.info("Business {} invited creator {} for collaboration", business.getUsername(), creator.getUsername());
        return toCollaborationResponse(collab, contract);
    }

    // ═══════ CREATOR: Accept Collaboration ═══════
    @Transactional
    public CollaborationResponse acceptCollaboration(Long creatorId, Long collaborationId) {
        Collaboration collab = collaborationRepo.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        if (!collab.getCreator().getId().equals(creatorId)) {
            throw new IllegalStateException("You are not the invited creator");
        }
        if (collab.getStatus() != CollaborationStatus.PENDING) {
            throw new IllegalStateException("This collaboration is not pending");
        }

        collab.setStatus(CollaborationStatus.ACTIVE);
        collaborationRepo.save(collab);

        // Update contract
        CollaborationContract contract = contractRepo.findTopByCollaborationIdOrderByCreatedAtDesc(collaborationId)
                .orElse(null);
        if (contract != null) {
            contract.setAcceptedByCreator(true);
            contract.setAcceptedAt(LocalDateTime.now());
            contract.setStartDate(LocalDateTime.now());
            contract.setEndDate(LocalDateTime.now().plusDays(contract.getDurationDays()));
            contractRepo.save(contract);
        }

        // Notify business
        notificationService.createNotification(collab.getBusiness(), collab.getCreator(),
                NotificationType.COLLABORATION_ACCEPTED,
                collab.getCreator().getName() + " accepted your collaboration invite", collab.getId());

        log.info("Creator {} accepted collaboration {}", collab.getCreator().getUsername(), collaborationId);
        return toCollaborationResponse(collab, contract);
    }

    // ═══════ CREATOR: Reject Collaboration ═══════
    @Transactional
    public CollaborationResponse rejectCollaboration(Long creatorId, Long collaborationId) {
        Collaboration collab = collaborationRepo.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        if (!collab.getCreator().getId().equals(creatorId)) {
            throw new IllegalStateException("You are not the invited creator");
        }
        if (collab.getStatus() != CollaborationStatus.PENDING) {
            throw new IllegalStateException("This collaboration is not pending");
        }

        collab.setStatus(CollaborationStatus.REJECTED);
        collaborationRepo.save(collab);

        notificationService.createNotification(collab.getBusiness(), collab.getCreator(),
                NotificationType.COLLABORATION_REJECTED,
                collab.getCreator().getName() + " declined your collaboration invite", collab.getId());

        return toCollaborationResponse(collab, null);
    }

    // ═══════ BUSINESS: Revoke Collaboration ═══════
    @Transactional
    public void revokeCollaboration(Long businessId, Long collaborationId) {
        Collaboration collab = collaborationRepo.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        if (!collab.getBusiness().getId().equals(businessId)) {
            throw new IllegalStateException("You are not the business owner of this collaboration");
        }

        collab.setStatus(CollaborationStatus.REVOKED);
        collaborationRepo.save(collab);

        // Revoke all active promotions under this collaboration
        List<PostPromotion> promos = promotionRepo.findByCollaborationId(collaborationId);
        for (PostPromotion p : promos) {
            if (p.getStatus() == PromotionStatus.APPROVED || p.getStatus() == PromotionStatus.PENDING) {
                p.setStatus(PromotionStatus.REVOKED);
            }
        }
        promotionRepo.saveAll(promos);

        notificationService.createNotification(collab.getCreator(), collab.getBusiness(),
                NotificationType.COLLABORATION_REVOKED,
                collab.getBusiness().getName() + " revoked the collaboration", collab.getId());
    }

    // ═══════ BUSINESS: Grant Promotion on a Post ═══════
    @Transactional
    public PostPromotionResponse grantPromotion(Long businessId, PostPromotionRequest request) {
        User business = userRepo.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Post post = postRepo.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(businessId)) {
            throw new IllegalStateException("You can only grant promotions on your own posts");
        }

        // Verify active collaboration exists
        Collaboration collab = collaborationRepo.findByBusinessIdAndCreatorId(businessId, request.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("No collaboration found with this creator"));

        if (collab.getStatus() != CollaborationStatus.ACTIVE) {
            throw new IllegalStateException("Collaboration is not active. Creator must accept the invite first.");
        }

        // Check contract not expired
        CollaborationContract contract = contractRepo.findTopByCollaborationIdOrderByCreatedAtDesc(collab.getId())
                .orElse(null);
        if (contract != null && contract.getEndDate() != null && contract.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Collaboration contract has expired");
        }

        // Check if already promoted
        if (promotionRepo.existsByPostIdAndCreatorIdAndStatus(post.getId(), request.getCreatorId(), PromotionStatus.APPROVED)) {
            throw new IllegalStateException("Creator already has an active promotion on this post");
        }

        User creator = collab.getCreator();

        PostPromotion promo = PostPromotion.builder()
                .post(post)
                .creator(creator)
                .business(business)
                .collaboration(collab)
                .status(PromotionStatus.APPROVED)
                .promotedAt(LocalDateTime.now())
                .expiresAt(contract != null && contract.getEndDate() != null ? contract.getEndDate() : LocalDateTime.now().plusDays(30))
                .ctaLabel(request.getCtaLabel())
                .ctaUrl(request.getCtaUrl())
                .build();
        promo = promotionRepo.save(promo);

        notificationService.createNotification(creator, business, NotificationType.PROMOTION_GRANTED,
                business.getName() + " granted you promotion access to a post", post.getId());

        return toPromotionResponse(promo);
    }

    // ═══════ BUSINESS: Revoke Promotion ═══════
    @Transactional
    public void revokePromotion(Long businessId, Long promotionId) {
        PostPromotion promo = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        if (!promo.getBusiness().getId().equals(businessId)) {
            throw new IllegalStateException("You are not the business owner");
        }

        promo.setStatus(PromotionStatus.REVOKED);
        promotionRepo.save(promo);

        notificationService.createNotification(promo.getCreator(), promo.getBusiness(),
                NotificationType.PROMOTION_REVOKED,
                promo.getBusiness().getName() + " revoked your promotion access", promo.getPost().getId());
    }

    // ═══════ Check if creator can promote a post ═══════
    public boolean canCreatorPromote(Long creatorId, Long postId) {
        return promotionRepo.existsByPostIdAndCreatorIdAndStatus(postId, creatorId, PromotionStatus.APPROVED);
    }

    // ═══════ Track CTA click ═══════
    @Transactional
    public void trackCtaClick(Long promotionId, boolean fromCreator) {
        PostPromotion promo = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        if (fromCreator) {
            promo.setCreatorClicks(promo.getCreatorClicks() + 1);
        } else {
            promo.setOrganicClicks(promo.getOrganicClicks() + 1);
        }
        promotionRepo.save(promo);
    }

    // ═══════ Get collaboration for a user ═══════
    public Page<CollaborationResponse> getMyCollaborations(Long userId, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Collaboration> collabs;
        if (status != null && !status.isEmpty()) {
            CollaborationStatus s = CollaborationStatus.valueOf(status.toUpperCase());
            if (user.getUserType() == UserType.BUSINESS) {
                collabs = collaborationRepo.findByBusinessIdAndStatus(userId, s, pageRequest);
            } else {
                collabs = collaborationRepo.findByCreatorIdAndStatus(userId, s, pageRequest);
            }
        } else {
            if (user.getUserType() == UserType.BUSINESS) {
                collabs = collaborationRepo.findByBusinessId(userId, pageRequest);
            } else {
                collabs = collaborationRepo.findByCreatorId(userId, pageRequest);
            }
        }

        return collabs.map(c -> {
            CollaborationContract contract = contractRepo.findTopByCollaborationIdOrderByCreatedAtDesc(c.getId()).orElse(null);
            return toCollaborationResponse(c, contract);
        });
    }

    // ═══════ Get single collaboration ═══════
    public CollaborationResponse getCollaboration(Long collaborationId, Long userId) {
        Collaboration collab = collaborationRepo.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        if (!collab.getBusiness().getId().equals(userId) && !collab.getCreator().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this collaboration");
        }

        CollaborationContract contract = contractRepo.findTopByCollaborationIdOrderByCreatedAtDesc(collaborationId).orElse(null);
        return toCollaborationResponse(collab, contract);
    }

    // ═══════ Get promotions for a post ═══════
    public List<PostPromotionResponse> getPostPromotions(Long postId) {
        return promotionRepo.findByPostIdAndStatus(postId, PromotionStatus.APPROVED)
                .stream()
                .map(this::toPromotionResponse)
                .toList();
    }

    // ═══════ Get promotion label for feed display ═══════
    public PostPromotionResponse getPromotionLabel(Long postId, Long creatorId) {
        return promotionRepo.findByPostIdAndCreatorId(postId, creatorId)
                .filter(p -> p.getStatus() == PromotionStatus.APPROVED)
                .map(this::toPromotionResponse)
                .orElse(null);
    }

    // ═══════ Mappers ═══════
    private CollaborationResponse toCollaborationResponse(Collaboration c, CollaborationContract contract) {
        return CollaborationResponse.builder()
                .id(c.getId())
                .status(c.getStatus().name())
                .businessId(c.getBusiness().getId())
                .businessName(c.getBusiness().getName())
                .businessUsername(c.getBusiness().getUsername())
                .businessPic(c.getBusiness().getProfilePicture())
                .creatorId(c.getCreator().getId())
                .creatorName(c.getCreator().getName())
                .creatorUsername(c.getCreator().getUsername())
                .creatorPic(c.getCreator().getProfilePicture())
                .message(c.getMessage())
                .contract(contract != null ? toContractResponse(contract) : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ContractResponse toContractResponse(CollaborationContract c) {
        return ContractResponse.builder()
                .id(c.getId())
                .promotionRules(c.getPromotionRules())
                .contentGuidelines(c.getContentGuidelines())
                .fixedFee(c.getFixedFee())
                .ratePerView(c.getRatePerView())
                .viewMilestone(c.getViewMilestone())
                .paymentSchedule(c.getPaymentSchedule())
                .brandName(c.getBrandName())
                .brandWebsite(c.getBrandWebsite())
                .durationDays(c.getDurationDays())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .acceptedByCreator(c.getAcceptedByCreator())
                .acceptedAt(c.getAcceptedAt())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private PostPromotionResponse toPromotionResponse(PostPromotion p) {
        return PostPromotionResponse.builder()
                .id(p.getId())
                .postId(p.getPost().getId())
                .postContent(p.getPost().getContent())
                .postMediaUrl(p.getPost().getMediaUrls() != null && !p.getPost().getMediaUrls().isEmpty() ? p.getPost().getMediaUrls().get(0) : null)
                .creatorId(p.getCreator().getId())
                .creatorName(p.getCreator().getName())
                .creatorUsername(p.getCreator().getUsername())
                .creatorPic(p.getCreator().getProfilePicture())
                .businessId(p.getBusiness().getId())
                .businessName(p.getBusiness().getName())
                .businessUsername(p.getBusiness().getUsername())
                .businessPic(p.getBusiness().getProfilePicture())
                .status(p.getStatus().name())
                .ctaLabel(p.getCtaLabel())
                .ctaUrl(p.getCtaUrl())
                .organicClicks(p.getOrganicClicks())
                .creatorClicks(p.getCreatorClicks())
                .promotedAt(p.getPromotedAt())
                .expiresAt(p.getExpiresAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
