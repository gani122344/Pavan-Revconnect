package org.revature.revconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.config.AiConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final WebClient ollamaWebClient;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ══════════════════════════════════════════════════════
    //  CORE: Call Ollama /api/generate
    // ══════════════════════════════════════════════════════
    private String callOllama(String prompt, String systemPrompt) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiConfig.getDefaultModel());
            body.put("prompt", prompt);
            body.put("stream", false);

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                body.put("system", systemPrompt);
            }

            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.7);
            options.put("top_p", 0.9);
            options.put("num_predict", 512);
            body.put("options", options);

            String response = ollamaWebClient.post()
                    .uri("/api/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                    .block();

            if (response != null) {
                JsonNode node = objectMapper.readTree(response);
                return node.has("response") ? node.get("response").asText().trim() : "";
            }
            return "";
        } catch (Exception e) {
            log.error("Ollama call failed: {}", e.getMessage());
            return "[AI unavailable] " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════════════
    //  CORE: Call Ollama /api/chat (multi-turn)
    // ══════════════════════════════════════════════════════
    private String callOllamaChat(List<Map<String, String>> messages) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiConfig.getDefaultModel());
            body.put("messages", messages);
            body.put("stream", false);

            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.7);
            options.put("num_predict", 512);
            body.put("options", options);

            String response = ollamaWebClient.post()
                    .uri("/api/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(aiConfig.getTimeoutSeconds()))
                    .block();

            if (response != null) {
                JsonNode node = objectMapper.readTree(response);
                if (node.has("message") && node.get("message").has("content")) {
                    return node.get("message").get("content").asText().trim();
                }
            }
            return "";
        } catch (Exception e) {
            log.error("Ollama chat failed: {}", e.getMessage());
            return "[AI unavailable] " + e.getMessage();
        }
    }

    // ══════════════════════════════════════════════════════
    //  1. AI CHAT ASSISTANT (DM bot)
    // ══════════════════════════════════════════════════════
    public String chatWithAssistant(String userMessage, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of("role", "system", "content",
                "You are RevConnect AI, a friendly assistant inside a social media app. " +
                "You help users with content ideas, caption writing, collaboration tips, and general questions. " +
                "Keep responses concise (2-3 sentences max). Be creative and helpful. " +
                "If asked about RevConnect features, explain them clearly."));

        // Add conversation history (last 10 messages max)
        if (history != null) {
            int start = Math.max(0, history.size() - 10);
            messages.addAll(history.subList(start, history.size()));
        }

        messages.add(Map.of("role", "user", "content", userMessage));
        return callOllamaChat(messages);
    }

    // ══════════════════════════════════════════════════════
    //  2. SMART REPLY SUGGESTIONS (DMs)
    // ══════════════════════════════════════════════════════
    public List<String> generateSmartReplies(String lastMessage, String conversationContext) {
        String prompt = String.format(
                "Given this chat message: \"%s\"\n" +
                "Context: %s\n\n" +
                "Generate exactly 3 short, natural reply suggestions (each under 15 words). " +
                "Return ONLY a JSON array of 3 strings, nothing else.\n" +
                "Example: [\"Sounds great!\", \"Let me check and get back\", \"Sure, when works for you?\"]",
                lastMessage,
                conversationContext != null ? conversationContext : "casual social media chat"
        );

        String result = callOllama(prompt, "You are a smart reply generator. Return ONLY valid JSON arrays.");

        try {
            // Parse JSON array
            String cleaned = result.trim();
            if (cleaned.contains("[")) {
                cleaned = cleaned.substring(cleaned.indexOf("["), cleaned.lastIndexOf("]") + 1);
            }
            JsonNode arr = objectMapper.readTree(cleaned);
            List<String> replies = new ArrayList<>();
            for (JsonNode item : arr) {
                replies.add(item.asText());
            }
            return replies.subList(0, Math.min(3, replies.size()));
        } catch (Exception e) {
            log.warn("Failed to parse smart replies: {}", e.getMessage());
            return List.of("Sounds good!", "Let me think about it", "Thanks!");
        }
    }

    // ══════════════════════════════════════════════════════
    //  3. CAPTION GENERATOR (Posts)
    // ══════════════════════════════════════════════════════
    public Map<String, Object> generateCaption(String context, String mood, String platform) {
        String prompt = String.format(
                "Generate a social media caption for this:\n" +
                "Topic/Description: %s\n" +
                "Mood: %s\n" +
                "Platform style: %s\n\n" +
                "Return a JSON object with:\n" +
                "- \"caption\": the main caption text (2-3 lines max)\n" +
                "- \"hashtags\": array of 5 relevant hashtags (without #)\n" +
                "- \"emojis\": array of 3 fitting emojis\n" +
                "Return ONLY valid JSON.",
                context, mood != null ? mood : "casual", platform != null ? platform : "Instagram"
        );

        String result = callOllama(prompt, "You are a social media content expert. Return ONLY valid JSON.");

        try {
            String cleaned = result.trim();
            if (cleaned.contains("{")) {
                cleaned = cleaned.substring(cleaned.indexOf("{"), cleaned.lastIndexOf("}") + 1);
            }
            JsonNode node = objectMapper.readTree(cleaned);
            Map<String, Object> response = new HashMap<>();
            response.put("caption", node.has("caption") ? node.get("caption").asText() : "");

            List<String> hashtags = new ArrayList<>();
            if (node.has("hashtags")) {
                for (JsonNode h : node.get("hashtags")) hashtags.add(h.asText());
            }
            response.put("hashtags", hashtags);

            List<String> emojis = new ArrayList<>();
            if (node.has("emojis")) {
                for (JsonNode e : node.get("emojis")) emojis.add(e.asText());
            }
            response.put("emojis", emojis);

            return response;
        } catch (Exception e) {
            log.warn("Failed to parse caption response: {}", e.getMessage());
            return Map.of("caption", result, "hashtags", List.of(), "emojis", List.of());
        }
    }

    // ══════════════════════════════════════════════════════
    //  4. HASHTAG SUGGESTIONS
    // ══════════════════════════════════════════════════════
    public List<String> suggestHashtags(String content, int count) {
        String prompt = String.format(
                "For this social media post: \"%s\"\n\n" +
                "Suggest exactly %d relevant hashtags. Mix trending and niche tags.\n" +
                "Return ONLY a JSON array of strings (without # symbol).\n" +
                "Example: [\"photography\", \"travelblogger\", \"sundayvibes\"]",
                content, count > 0 ? count : 5
        );

        String result = callOllama(prompt, "You are a hashtag expert. Return ONLY valid JSON arrays.");

        try {
            String cleaned = result.trim();
            if (cleaned.contains("[")) {
                cleaned = cleaned.substring(cleaned.indexOf("["), cleaned.lastIndexOf("]") + 1);
            }
            JsonNode arr = objectMapper.readTree(cleaned);
            List<String> tags = new ArrayList<>();
            for (JsonNode item : arr) tags.add(item.asText().replace("#", ""));
            return tags;
        } catch (Exception e) {
            return List.of("trending", "viral", "explore", "fyp", "instagood");
        }
    }

    // ══════════════════════════════════════════════════════
    //  5. BIO GENERATOR (Profile)
    // ══════════════════════════════════════════════════════
    public List<String> generateBio(String name, String userType, String category, String interests) {
        String prompt = String.format(
                "Generate 3 unique social media bios for:\n" +
                "Name: %s\nAccount type: %s\nCategory: %s\nInterests: %s\n\n" +
                "Each bio should be under 150 characters, catchy, and include 1-2 emojis.\n" +
                "Return ONLY a JSON array of 3 strings.",
                name, userType != null ? userType : "personal",
                category != null ? category : "general",
                interests != null ? interests : "social media"
        );

        String result = callOllama(prompt, "You are a branding expert. Return ONLY valid JSON arrays.");

        try {
            String cleaned = result.trim();
            if (cleaned.contains("[")) {
                cleaned = cleaned.substring(cleaned.indexOf("["), cleaned.lastIndexOf("]") + 1);
            }
            JsonNode arr = objectMapper.readTree(cleaned);
            List<String> bios = new ArrayList<>();
            for (JsonNode item : arr) bios.add(item.asText());
            return bios;
        } catch (Exception e) {
            return List.of(name + " | " + (category != null ? category : "Creator") + " ✨");
        }
    }

    // ══════════════════════════════════════════════════════
    //  6. AI ANALYTICS INSIGHTS
    // ══════════════════════════════════════════════════════
    public String generateAnalyticsInsights(Map<String, Object> analyticsData) {
        String prompt = String.format(
                "Analyze this social media analytics data and give 3-4 actionable insights:\n%s\n\n" +
                "Be specific, mention numbers. Format as bullet points. Keep it under 200 words.\n" +
                "Focus on: best posting time, content strategy, engagement tips.",
                analyticsData.toString()
        );

        return callOllama(prompt,
                "You are a social media analytics expert. Give actionable, data-driven insights. Be concise and specific.");
    }

    // ══════════════════════════════════════════════════════
    //  7. CONTENT MODERATION
    // ══════════════════════════════════════════════════════
    public Map<String, Object> moderateContent(String content) {
        String prompt = String.format(
                "Analyze this social media post for content moderation:\n\"%s\"\n\n" +
                "Return a JSON object with:\n" +
                "- \"safe\": boolean (true if the content is safe)\n" +
                "- \"score\": number 0-100 (0=very toxic, 100=perfectly safe)\n" +
                "- \"flags\": array of issue categories found (e.g. \"spam\", \"hate\", \"harassment\", \"violence\", \"nsfw\")\n" +
                "- \"reason\": brief explanation if unsafe\n" +
                "Return ONLY valid JSON.",
                content
        );

        String result = callOllama(prompt,
                "You are a content moderation system. Evaluate objectively. Return ONLY valid JSON.");

        try {
            String cleaned = result.trim();
            if (cleaned.contains("{")) {
                cleaned = cleaned.substring(cleaned.indexOf("{"), cleaned.lastIndexOf("}") + 1);
            }
            return objectMapper.readValue(cleaned, Map.class);
        } catch (Exception e) {
            return Map.of("safe", true, "score", 80, "flags", List.of(), "reason", "moderation unavailable");
        }
    }

    // ══════════════════════════════════════════════════════
    //  8. COLLABORATION MATCHMAKING
    // ══════════════════════════════════════════════════════
    public String suggestCollaborationMatch(String businessInfo, String creatorInfo) {
        String prompt = String.format(
                "As a brand partnership advisor, analyze this potential collaboration:\n\n" +
                "Business: %s\n" +
                "Creator: %s\n\n" +
                "Give a compatibility score (0-100) and explain why they'd be a good/bad match. " +
                "Suggest 2-3 campaign ideas they could do together. Keep it under 150 words.",
                businessInfo, creatorInfo
        );

        return callOllama(prompt,
                "You are a brand partnership and influencer marketing expert. Be specific and actionable.");
    }

    // ══════════════════════════════════════════════════════
    //  9. PDF SUMMARIZATION (DMs)
    // ══════════════════════════════════════════════════════
    public String summarizePdf(String extractedText) {
        String truncated = extractedText.length() > 3000 ? extractedText.substring(0, 3000) + "..." : extractedText;
        String prompt = String.format(
                "Summarize this document in 3-5 key bullet points:\n\n%s",
                truncated
        );

        return callOllama(prompt,
                "You are a document summarizer. Be concise and extract the most important information.");
    }

    // ══════════════════════════════════════════════════════
    //  10. HEALTH CHECK
    // ══════════════════════════════════════════════════════
    public Map<String, Object> healthCheck() {
        try {
            String response = ollamaWebClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            JsonNode node = objectMapper.readTree(response);
            List<String> models = new ArrayList<>();
            if (node.has("models")) {
                for (JsonNode m : node.get("models")) {
                    models.add(m.get("name").asText());
                }
            }

            return Map.of(
                    "status", "connected",
                    "baseUrl", aiConfig.getOllamaBaseUrl(),
                    "defaultModel", aiConfig.getDefaultModel(),
                    "availableModels", models
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "disconnected",
                    "error", e.getMessage(),
                    "baseUrl", aiConfig.getOllamaBaseUrl()
            );
        }
    }
}
