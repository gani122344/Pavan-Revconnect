package org.revature.revconnect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.*;
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.model.User;
import org.revature.revconnect.service.AuthService;
import org.revature.revconnect.service.CollaborationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Collaborations", description = "Business-Creator collaboration management")
public class CollaborationController {

    private final CollaborationService collaborationService;
    private final AuthService authService;
    private final org.revature.revconnect.service.ContractPdfService contractPdfService;

    // ═══════ BUSINESS: Invite a creator ═══════
    @PostMapping("/invite")
    @Operation(summary = "Business invites a creator to collaborate")
    public ResponseEntity<ApiResponse<CollaborationResponse>> inviteCreator(@RequestBody CollaborationRequest request) {
        User currentUser = authService.getCurrentUser();
        CollaborationResponse response = collaborationService.inviteCreator(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ═══════ CREATOR: Accept collaboration ═══════
    @PostMapping("/{id}/accept")
    @Operation(summary = "Creator accepts a collaboration invite")
    public ResponseEntity<ApiResponse<CollaborationResponse>> acceptCollaboration(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        CollaborationResponse response = collaborationService.acceptCollaboration(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ═══════ CREATOR: Reject collaboration ═══════
    @PostMapping("/{id}/reject")
    @Operation(summary = "Creator rejects a collaboration invite")
    public ResponseEntity<ApiResponse<CollaborationResponse>> rejectCollaboration(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        CollaborationResponse response = collaborationService.rejectCollaboration(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ═══════ BUSINESS: Revoke collaboration ═══════
    @PostMapping("/{id}/revoke")
    @Operation(summary = "Business revokes a collaboration")
    public ResponseEntity<ApiResponse<Map<String, String>>> revokeCollaboration(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        collaborationService.revokeCollaboration(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "revoked")));
    }

    // ═══════ Get my collaborations ═══════
    @GetMapping
    @Operation(summary = "Get my collaborations (business or creator)")
    public ResponseEntity<ApiResponse<Page<CollaborationResponse>>> getMyCollaborations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = authService.getCurrentUser();
        Page<CollaborationResponse> result = collaborationService.getMyCollaborations(currentUser.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ═══════ Get single collaboration ═══════
    @GetMapping("/{id}")
    @Operation(summary = "Get collaboration details")
    public ResponseEntity<ApiResponse<CollaborationResponse>> getCollaboration(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        CollaborationResponse response = collaborationService.getCollaboration(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ═══════ BUSINESS: Grant promotion on a post ═══════
    @PostMapping("/promotions/grant")
    @Operation(summary = "Business grants a creator access to promote a specific post")
    public ResponseEntity<ApiResponse<PostPromotionResponse>> grantPromotion(@RequestBody PostPromotionRequest request) {
        User currentUser = authService.getCurrentUser();
        PostPromotionResponse response = collaborationService.grantPromotion(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ═══════ BUSINESS: Revoke promotion ═══════
    @PostMapping("/promotions/{id}/revoke")
    @Operation(summary = "Business revokes a promotion")
    public ResponseEntity<ApiResponse<Map<String, String>>> revokePromotion(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        collaborationService.revokePromotion(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "revoked")));
    }

    // ═══════ Get promotions for a post ═══════
    @GetMapping("/promotions/post/{postId}")
    @Operation(summary = "Get all approved promotions for a post")
    public ResponseEntity<ApiResponse<List<PostPromotionResponse>>> getPostPromotions(@PathVariable Long postId) {
        List<PostPromotionResponse> result = collaborationService.getPostPromotions(postId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ═══════ Check if current creator can promote a post ═══════
    @GetMapping("/promotions/check/{postId}")
    @Operation(summary = "Check if current user can promote a post")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canPromote(@PathVariable Long postId) {
        User currentUser = authService.getCurrentUser();
        boolean can = collaborationService.canCreatorPromote(currentUser.getId(), postId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("canPromote", can)));
    }

    // ═══════ Track CTA click ═══════
    @PostMapping("/promotions/{id}/click")
    @Operation(summary = "Track a CTA click on a promotion")
    public ResponseEntity<ApiResponse<Map<String, String>>> trackClick(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean fromCreator) {
        collaborationService.trackCtaClick(id, fromCreator);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "tracked")));
    }

    // ═══════ Get promotion label for feed ═══════
    @GetMapping("/promotions/label")
    @Operation(summary = "Get promotion label for a post displayed by a creator")
    public ResponseEntity<ApiResponse<PostPromotionResponse>> getPromotionLabel(
            @RequestParam Long postId,
            @RequestParam Long creatorId) {
        PostPromotionResponse response = collaborationService.getPromotionLabel(postId, creatorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ═══════ Download contract PDF ═══════
    @GetMapping("/{id}/contract/pdf")
    @Operation(summary = "Download collaboration contract as PDF")
    public ResponseEntity<byte[]> downloadContractPdf(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        byte[] pdf = contractPdfService.generateContractPdf(id, currentUser.getId());
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=RevConnect-Contract-RC-" + id + ".pdf")
                .body(pdf);
    }
}
