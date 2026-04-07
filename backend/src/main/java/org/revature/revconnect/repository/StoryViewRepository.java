package org.revature.revconnect.repository;

import org.revature.revconnect.model.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, Long> {

    boolean existsByStoryIdAndViewerId(Long storyId, Long viewerId);

    @Query("SELECT sv FROM StoryView sv JOIN FETCH sv.viewer WHERE sv.story.id = :storyId ORDER BY sv.viewedAt DESC")
    List<StoryView> findByStoryIdWithViewer(@Param("storyId") Long storyId);

    long countByStoryId(Long storyId);

    void deleteByStoryId(Long storyId);
}
