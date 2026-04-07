package org.revature.revconnect.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/music")
@Slf4j
public class MusicProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/search")
    public ResponseEntity<String> searchSongs(@RequestParam String query) {
        try {
            String encoded = java.net.URLEncoder.encode(query, "UTF-8");
            // Try global search first (wider catalog with more preview URLs)
            String url = "https://itunes.apple.com/search?term=" + encoded + "&media=music&limit=10";
            String response = restTemplate.getForObject(url, String.class);
            // If no results, try India-specific
            if (response != null && response.contains("\"resultCount\":0")) {
                url = "https://itunes.apple.com/search?term=" + encoded + "&media=music&limit=10&country=IN";
                response = restTemplate.getForObject(url, String.class);
            }
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
        } catch (Exception e) {
            log.error("Music search failed for query: {}", query, e);
            return ResponseEntity.ok("{\"resultCount\":0,\"results\":[]}");
        }
    }
}
