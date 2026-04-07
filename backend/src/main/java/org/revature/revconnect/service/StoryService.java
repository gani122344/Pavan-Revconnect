package org.revature.revconnect.service;

import org.revature.revconnect.exception.ResourceNotFoundException;
import org.revature.revconnect.model.Story;
import org.revature.revconnect.model.User;
import org.revature.revconnect.repository.ConnectionRepository;
import org.revature.revconnect.model.StoryView;
import org.revature.revconnect.repository.StoryRepository;
import org.revature.revconnect.repository.StoryViewRepository;
import org.revature.revconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final AuthService authService;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private static final Map<Long, Set<Long>> STORY_VIEWERS = new ConcurrentHashMap<>();
    private static final Map<Long, Map<Long, String>> STORY_REACTIONS = new ConcurrentHashMap<>();
    private static final Map<Long, List<Map<String, Object>>> STORY_REPLIES = new ConcurrentHashMap<>();

    @Transactional
    public Story createStory(String mediaUrl, String caption) {
        return createStory(mediaUrl, caption, null, null, null);
    }

    @Transactional
    public Story createStory(String mediaUrl, String caption, String songTitle, String songArtist, String songUrl) {
        User currentUser = authService.getCurrentUser();
        log.info("Creating story for user: {}", currentUser.getUsername());

        Story story = Story.builder()
                .user(currentUser)
                .mediaUrl(mediaUrl)
                .caption(caption)
                .songTitle(songTitle)
                .songArtist(songArtist)
                .songUrl(songUrl)
                .build();

        Story savedStory = storyRepository.save(story);
        log.info("Story created with ID: {}", savedStory.getId());
        return savedStory;
    }

    public List<Story> getActiveStories(User user) {
        log.info("Fetching active stories for user: {}", user.getUsername());
        return storyRepository.findByUserAndExpiresAtAfterOrderByCreatedAtDesc(user, LocalDateTime.now());
    }

    public List<Story> getStoriesFeed(List<User> followedUsers) {
        log.info("Fetching stories feed for {} users", followedUsers.size());
        return storyRepository.findActiveStoriesByUsers(followedUsers, LocalDateTime.now());
    }

    public List<Story> getStoriesFeedForCurrentUser() {
        User currentUser = authService.getCurrentUser();
        List<Long> followingIds = connectionRepository.findFollowingUserIds(currentUser.getId());
        List<User> followedUsers = userRepository.findAllById(followingIds);
        return getStoriesFeed(followedUsers);
    }

    public Story getStory(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story", "id", storyId));
    }

    public List<Story> getHighlights(User user) {
        log.info("Fetching highlights for user: {}", user.getUsername());
        return storyRepository.findByUserAndIsHighlightTrueOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Story markAsHighlight(Long storyId) {
        Story story = getStory(storyId);

        story.setHighlight(true);
        log.info("Story {} marked as highlight", storyId);
        return storyRepository.save(story);
    }

    @Transactional
    public Story unmarkHighlight(Long storyId) {
        Story story = getStory(storyId);
        story.setHighlight(false);
        log.info("Story {} unmarked as highlight", storyId);
        return storyRepository.save(story);
    }

    @Transactional
    public void incrementViewCount(Long storyId) {
        Story story = getStory(storyId);
        User viewer = authService.getCurrentUser();

        // Skip if viewer is the story owner
        if (story.getUser().getId().equals(viewer.getId())) {
            return;
        }

        // Check DB first, then in-memory cache
        if (!storyViewRepository.existsByStoryIdAndViewerId(storyId, viewer.getId())) {
            StoryView storyView = StoryView.builder()
                    .story(story)
                    .viewer(viewer)
                    .build();
            storyViewRepository.save(storyView);
            story.setViewCount(story.getViewCount() + 1);
            storyRepository.save(story);
            log.info("Story {} viewed by user {}", storyId, viewer.getUsername());
        }

        // Also maintain in-memory for backward compat
        Set<Long> viewers = STORY_VIEWERS.computeIfAbsent(storyId, ignored -> new LinkedHashSet<>());
        viewers.add(viewer.getId());
    }

    public List<Story> getArchivedStories(User user) {
        log.info("Fetching archived stories for user: {}", user.getUsername());
        return storyRepository.findByUserAndExpiresAtBeforeOrderByCreatedAtDesc(user, LocalDateTime.now());
    }

    public List<Map<String, Object>> getStoryViewers(Long storyId) {
        Story story = getStory(storyId);
        List<StoryView> views = storyViewRepository.findByStoryIdWithViewer(storyId);

        if (views.isEmpty()) {
            // Return summary info
            Map<String, Object> summary = new HashMap<>();
            summary.put("storyId", story.getId());
            summary.put("ownerId", story.getUser().getId());
            summary.put("viewerCount", story.getViewCount());
            summary.put("viewers", List.of());
            return List.of(summary);
        }

        return views.stream().map(sv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", sv.getViewer().getId());
            map.put("username", sv.getViewer().getUsername());
            map.put("name", sv.getViewer().getName());
            map.put("profilePicture", sv.getViewer().getProfilePicture());
            map.put("viewedAt", sv.getViewedAt());
            return map;
        }).toList();
    }

    @Transactional
    public void reactToStory(Long storyId, String reaction) {
        getStory(storyId);
        User currentUser = authService.getCurrentUser();
        STORY_REACTIONS.computeIfAbsent(storyId, ignored -> new ConcurrentHashMap<>())
                .put(currentUser.getId(), reaction);
    }

    @Transactional
    public void replyToStory(Long storyId, String message) {
        getStory(storyId);
        User currentUser = authService.getCurrentUser();
        Map<String, Object> reply = new HashMap<>();
        reply.put("userId", currentUser.getId());
        reply.put("username", currentUser.getUsername());
        reply.put("message", message);
        reply.put("createdAt", LocalDateTime.now());
        STORY_REPLIES.computeIfAbsent(storyId, ignored -> new ArrayList<>()).add(reply);
    }

    @Transactional
    public void deleteStory(Long storyId) {
        User currentUser = authService.getCurrentUser();
        Story story = getStory(storyId);

        if (!story.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own stories");
        }

        storyViewRepository.deleteByStoryId(storyId);
        storyRepository.delete(story);
        log.info("Story {} deleted", storyId);
    }

    @Transactional
    public void cleanupExpiredStories() {
        List<Story> expiredStories = storyRepository.findExpiredStories(LocalDateTime.now());
        log.info("Cleaning up {} expired stories", expiredStories.size());
        storyRepository.deleteAll(expiredStories);
    }
}
