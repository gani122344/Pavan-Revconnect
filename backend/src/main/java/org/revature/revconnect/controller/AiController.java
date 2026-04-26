package org.revature.revconnect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.dto.response.ApiResponse;
import org.revature.revconnect.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI", description = "AI-powered features using Ollama LLM")
public class AiController {

    private final AiService aiService;

    // ══════════════ AI Chat Assistant ══════════════
    @PostMapping("/chat")
    @Operation(summary = "Chat with RevConnect AI assistant")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message is required"));
        }

        log.info("AI chat request: {}", message.substring(0, Math.min(50, message.length())));
        String response = aiService.chatWithAssistant(message, history);

        return ResponseEntity.ok(ApiResponse.success(Map.of("reply", response)));
    }

    // ══════════════ Smart Reply Suggestions ══════════════
    @PostMapping("/smart-replies")
    @Operation(summary = "Get smart reply suggestions for a chat message")
    public ResponseEntity<ApiResponse<List<String>>> smartReplies(@RequestBody Map<String, String> request) {
        String lastMessage = request.get("message");
        String context = request.get("context");

        if (lastMessage == null || lastMessage.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message is required"));
        }

        List<String> replies = aiService.generateSmartReplies(lastMessage, context);
        return ResponseEntity.ok(ApiResponse.success(replies));
    }

    // ══════════════ Caption Generator ══════════════
    @PostMapping("/caption")
    @Operation(summary = "Generate a post caption with hashtags")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCaption(@RequestBody Map<String, String> request) {
        String context = request.get("context");
        String mood = request.get("mood");
        String platform = request.get("platform");

        if (context == null || context.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Context/description is required"));
        }

        Map<String, Object> result = aiService.generateCaption(context, mood, platform);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ══════════════ Hashtag Suggestions ══════════════
    @PostMapping("/hashtags")
    @Operation(summary = "Suggest hashtags for post content")
    public ResponseEntity<ApiResponse<List<String>>> suggestHashtags(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        int count = request.containsKey("count") ? ((Number) request.get("count")).intValue() : 5;

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Content is required"));
        }

        List<String> hashtags = aiService.suggestHashtags(content, count);
        return ResponseEntity.ok(ApiResponse.success(hashtags));
    }

    // ══════════════ Bio Generator ══════════════
    @PostMapping("/bio")
    @Operation(summary = "Generate profile bio suggestions")
    public ResponseEntity<ApiResponse<List<String>>> generateBio(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String userType = request.get("userType");
        String category = request.get("category");
        String interests = request.get("interests");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
        }

        List<String> bios = aiService.generateBio(name, userType, category, interests);
        return ResponseEntity.ok(ApiResponse.success(bios));
    }

    // ══════════════ Analytics Insights ══════════════
    @PostMapping("/insights")
    @Operation(summary = "Get AI-generated analytics insights")
    public ResponseEntity<ApiResponse<Map<String, String>>> analyticsInsights(@RequestBody Map<String, Object> request) {
        String insights = aiService.generateAnalyticsInsights(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("insights", insights)));
    }

    // ══════════════ Content Moderation ══════════════
    @PostMapping("/moderate")
    @Operation(summary = "Check content for moderation issues")
    public ResponseEntity<ApiResponse<Map<String, Object>>> moderateContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Content is required"));
        }

        Map<String, Object> result = aiService.moderateContent(content);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ══════════════ Collaboration Matchmaking ══════════════
    @PostMapping("/match")
    @Operation(summary = "AI-powered collaboration matchmaking")
    public ResponseEntity<ApiResponse<Map<String, String>>> matchmaking(@RequestBody Map<String, String> request) {
        String businessInfo = request.get("businessInfo");
        String creatorInfo = request.get("creatorInfo");

        String result = aiService.suggestCollaborationMatch(businessInfo, creatorInfo);
        return ResponseEntity.ok(ApiResponse.success(Map.of("analysis", result)));
    }

    // ══════════════ PDF Summarization ══════════════
    @PostMapping("/summarize")
    @Operation(summary = "Summarize extracted text (from PDF/document)")
    public ResponseEntity<ApiResponse<Map<String, String>>> summarize(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Text is required"));
        }

        String summary = aiService.summarizePdf(text);
        return ResponseEntity.ok(ApiResponse.success(Map.of("summary", summary)));
    }

    // ══════════════ Health Check ══════════════
    @GetMapping("/health")
    @Operation(summary = "Check AI/Ollama connection status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(aiService.healthCheck()));
    }
}
