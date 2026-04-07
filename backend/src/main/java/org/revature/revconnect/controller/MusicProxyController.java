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
            String url = "https://itunes.apple.com/search?term=" +
                java.net.URLEncoder.encode(query, "UTF-8") +
                "&media=music&limit=5&country=IN";
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
        } catch (Exception e) {
            log.error("Music search failed for query: {}", query, e);
            return ResponseEntity.ok("{\"resultCount\":0,\"results\":[]}");
        }
    }
}
