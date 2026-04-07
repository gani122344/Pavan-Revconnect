import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoryService, StoryResponse, StoryViewer } from '../../../core/services/story.service';
import { MediaService } from '../../../core/services/media.service';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UserService, UserResponse } from '../../../core/services/user.service';
import { SONG_LIBRARY, SONG_GENRES, Song } from '../../../shared/data/songs.data';
import { AudioPlayerService } from '../../../core/services/audio-player.service';

export interface StoryGroup {
  userId: number;
  username: string;
  profilePicture: string;
  stories: StoryResponse[];
  isMine: boolean;
}

@Component({
  selector: 'app-stories-feed',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './stories-feed.html',
  styleUrls: ['./stories-feed.css']
})
export class StoriesFeed implements OnInit, OnDestroy {
  stories: StoryResponse[] = [];
  myStories: StoryResponse[] = [];
  currentUser: UserResponse | null = null;
  isLoading = false;

  // Grouped stories (Instagram-style)
  otherStoryGroups: StoryGroup[] = [];
  myStoryGroup: StoryGroup | null = null;

  // Create Story Modal
  showCreateModal = false;
  selectedMediaFile: File | null = null;
  mediaPreviewUrl: string | null = null;
  newStoryCaption = '';
  isCreating = false;
  isUploadingMedia = false;

  // Music song picker
  showMusicPicker = false;
  selectedSong: Song | null = null;
  songSearch = '';
  selectedGenre = '';
  allSongs = SONG_LIBRARY;
  allGenres = SONG_GENRES;

  get filteredSongs(): Song[] {
    let songs = this.allSongs;
    if (this.selectedGenre) {
      songs = songs.filter(s => s.genre === this.selectedGenre);
    }
    if (this.songSearch.trim()) {
      const q = this.songSearch.toLowerCase();
      songs = songs.filter(s => s.title.toLowerCase().includes(q) || s.artist.toLowerCase().includes(q));
    }
    return songs;
  }

  // View Story Modal — now supports multiple stories in a group
  activeStoryToView: StoryResponse | null = null;
  activeGroup: StoryGroup | null = null;
  activeStoryIndex = 0;
  storyProgressTimer: any = null;
  storyProgress = 0;
  storyPaused = false;

  // Viewers panel
  showViewersPanel = false;
  storyViewers: StoryViewer[] = [];
  isLoadingViewers = false;

  // Audio
  isAudioPlaying = false;

  constructor(
    private storyService: StoryService,
    private mediaService: MediaService,
    private userService: UserService,
    private audioPlayer: AudioPlayerService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.userService.getMyProfile().subscribe(res => {
      if (res.success) this.currentUser = res.data;
      this.loadStories();
    });
  }

  ngOnDestroy() {
    this.clearStoryTimer();
    this.audioPlayer.stop();
  }

  loadStories() {
    this.isLoading = true;
    this.storyService.getStoriesFeed().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.stories = res.data || [];
        }
        this.fetchMyStories();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  fetchMyStories() {
    this.storyService.getMyStories().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.myStories = res.data || [];
        }
        this.buildStoryGroups();
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  buildStoryGroups() {
    // Group my stories
    if (this.myStories.length > 0) {
      this.myStoryGroup = {
        userId: this.currentUser?.id || 0,
        username: this.currentUser?.username || 'Me',
        profilePicture: this.currentUser?.profilePicture || '',
        stories: [...this.myStories],
        isMine: true,
      };
    } else {
      this.myStoryGroup = null;
    }

    // Group other users' stories by userId
    const groupMap = new Map<number, StoryGroup>();
    for (const story of this.stories) {
      const uid = story.user?.id || story.userId;
      if (!uid) continue;
      if (!groupMap.has(uid)) {
        groupMap.set(uid, {
          userId: uid,
          username: story.user?.username || 'User',
          profilePicture: story.user?.profilePicture || '',
          stories: [],
          isMine: false,
        });
      }
      groupMap.get(uid)!.stories.push(story);
    }
    this.otherStoryGroups = Array.from(groupMap.values());
  }

  getSegmentDash(group: StoryGroup): string {
    const count = group.stories.length;
    if (count <= 1) return ''; // full circle, no segmentation
    const circumference = 2 * Math.PI * 28; // r=28 in our SVG
    const gap = 4;
    const segLen = (circumference - gap * count) / count;
    return `${segLen} ${gap}`;
  }

  openCreateModal() {
    this.showCreateModal = true;
    this.selectedMediaFile = null;
    this.mediaPreviewUrl = null;
    this.newStoryCaption = '';
    this.selectedSong = null;
    this.songSearch = '';
    this.selectedGenre = '';
    this.showMusicPicker = false;
    this.cdr.markForCheck();
  }

  closeCreateModal() {
    this.showCreateModal = false;
    this.selectedMediaFile = null;
    this.mediaPreviewUrl = null;
    this.cdr.markForCheck();
  }

  triggerFileInput(fileInput: HTMLInputElement) {
    fileInput.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedMediaFile = input.files[0];

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.mediaPreviewUrl = e.target?.result as string;
        this.cdr.markForCheck();
      };
      reader.readAsDataURL(this.selectedMediaFile);
    }
  }

  removeSelectedMedia() {
    this.selectedMediaFile = null;
    this.mediaPreviewUrl = null;
    this.cdr.markForCheck();
  }

  createStory() {
    if (!this.selectedMediaFile) return;

    this.isCreating = true;
    this.isUploadingMedia = true;
    this.cdr.markForCheck();

    // Handle video or general file upload
    const uploadAction = this.selectedMediaFile.type.startsWith('video/')
      ? this.mediaService.uploadVideo(this.selectedMediaFile)
      : this.mediaService.uploadFile(this.selectedMediaFile);

    uploadAction.subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.publishStory(res.data.url);
        } else {
          this.isCreating = false;
          this.isUploadingMedia = false;
          this.cdr.markForCheck();
        }
      },
      error: (err) => {
        console.error('Error uploading story media:', err);
        this.isCreating = false;
        this.isUploadingMedia = false;
        alert('Failed to upload media. Please try again.');
        this.cdr.markForCheck();
      }
    });
  }

  toggleMusicPicker() {
    this.showMusicPicker = !this.showMusicPicker;
    this.cdr.markForCheck();
  }

  selectSong(song: Song) {
    this.selectedSong = song;
    this.showMusicPicker = false;
    this.cdr.markForCheck();
  }

  removeSong() {
    this.selectedSong = null;
    this.cdr.markForCheck();
  }

  private publishStory(mediaUrl: string) {
    const songTitle = this.selectedSong?.title || undefined;
    const songArtist = this.selectedSong?.artist || undefined;
    this.storyService.createStory(mediaUrl, this.newStoryCaption, songTitle, songArtist, undefined).subscribe({
      next: (res) => {
        this.isCreating = false;
        this.isUploadingMedia = false;
        this.closeCreateModal();
        this.loadStories(); // Refresh feed
      },
      error: (err) => {
        this.isCreating = false;
        this.isUploadingMedia = false;
        alert(err?.error?.message || 'Failed to create story');
        this.cdr.markForCheck();
      }
    });
  }

  viewGroup(group: StoryGroup) {
    this.activeGroup = group;
    this.activeStoryIndex = 0;
    this.activeStoryToView = group.stories[0];
    this.showViewersPanel = false;
    this.storyViewers = [];
    this.startStoryTimer();
    this.playSongForCurrentStory();
    this.cdr.markForCheck();
    this.storyService.viewStory(this.activeStoryToView.id).subscribe();
  }

  viewStory(story: StoryResponse) {
    // Find the group that contains this story
    if (this.myStoryGroup?.stories.some(s => s.id === story.id)) {
      this.viewGroup(this.myStoryGroup);
      return;
    }
    const group = this.otherStoryGroups.find(g => g.stories.some(s => s.id === story.id));
    if (group) {
      this.viewGroup(group);
    } else {
      // Fallback: single story
      this.activeGroup = null;
      this.activeStoryIndex = 0;
      this.activeStoryToView = story;
      this.startStoryTimer();
      this.playSongForCurrentStory();
      this.cdr.markForCheck();
      this.storyService.viewStory(story.id).subscribe();
    }
  }

  nextStory() {
    if (!this.activeGroup) { this.closeStoryView(); return; }
    if (this.activeStoryIndex < this.activeGroup.stories.length - 1) {
      this.activeStoryIndex++;
      this.activeStoryToView = this.activeGroup.stories[this.activeStoryIndex];
      this.showViewersPanel = false;
      this.storyViewers = [];
      this.startStoryTimer();
      this.playSongForCurrentStory();
      this.cdr.markForCheck();
      this.storyService.viewStory(this.activeStoryToView.id).subscribe();
    } else {
      this.closeStoryView();
    }
  }

  prevStory() {
    if (!this.activeGroup || this.activeStoryIndex <= 0) return;
    this.activeStoryIndex--;
    this.activeStoryToView = this.activeGroup.stories[this.activeStoryIndex];
    this.showViewersPanel = false;
    this.storyViewers = [];
    this.startStoryTimer();
    this.playSongForCurrentStory();
    this.cdr.markForCheck();
  }

  private startStoryTimer() {
    this.clearStoryTimer();
    this.storyProgress = 0;
    this.storyPaused = false;
    const duration = this.activeStoryToView?.songTitle ? 30000 : 10000; // 30s with music, 10s without
    const interval = 50;
    this.storyProgressTimer = setInterval(() => {
      if (this.storyPaused) return;
      this.storyProgress += (interval / duration) * 100;
      if (this.storyProgress >= 100) {
        this.nextStory();
      }
      this.cdr.markForCheck();
    }, interval);
  }

  private clearStoryTimer() {
    if (this.storyProgressTimer) {
      clearInterval(this.storyProgressTimer);
      this.storyProgressTimer = null;
    }
    this.storyProgress = 0;
    this.storyPaused = false;
  }

  pauseStory() {
    this.storyPaused = true;
  }

  resumeStory() {
    this.storyPaused = false;
  }

  toggleStoryPause() {
    this.storyPaused = !this.storyPaused;
  }

  closeStoryView() {
    this.clearStoryTimer();
    this.audioPlayer.stop();
    this.isAudioPlaying = false;
    this.activeStoryToView = null;
    this.activeGroup = null;
    this.activeStoryIndex = 0;
    this.showViewersPanel = false;
    this.storyViewers = [];
    this.cdr.markForCheck();
  }

  playSongForCurrentStory() {
    this.audioPlayer.stop();
    this.isAudioPlaying = false;
    if (this.activeStoryToView?.songTitle) {
      const song = this.allSongs.find(s => s.title === this.activeStoryToView?.songTitle);
      const genre = song?.genre || 'Love';
      this.audioPlayer.play(this.activeStoryToView.songTitle, genre, true);
      this.isAudioPlaying = true;
    }
  }

  toggleAudio() {
    if (!this.activeStoryToView?.songTitle) return;
    const song = this.allSongs.find(s => s.title === this.activeStoryToView?.songTitle);
    const genre = song?.genre || 'Love';
    this.isAudioPlaying = this.audioPlayer.toggle(this.activeStoryToView.songTitle, genre);
    this.cdr.markForCheck();
  }

  onStoryAreaClick(event: MouseEvent) {
    if (!this.activeGroup) { this.closeStoryView(); return; }
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const x = event.clientX - rect.left;
    if (x < rect.width / 3) {
      this.prevStory();
    } else if (x > rect.width * 2 / 3) {
      this.nextStory();
    }
    // Middle third does nothing (allows interaction with song badge etc.)
  }

  toggleViewersPanel() {
    this.showViewersPanel = !this.showViewersPanel;
    if (this.showViewersPanel) {
      this.pauseStory();
      if (this.activeStoryToView) {
        this.loadViewers(this.activeStoryToView.id);
      }
    } else {
      this.resumeStory();
    }
    this.cdr.markForCheck();
  }

  loadViewers(storyId: number) {
    this.isLoadingViewers = true;
    this.storyService.getStoryViewers(storyId).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.storyViewers = res.data;
        }
        this.isLoadingViewers = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoadingViewers = false;
        this.cdr.markForCheck();
      }
    });
  }

  get isMyActiveStory(): boolean {
    if (!this.activeStoryToView) return false;
    return this.myStoryGroup?.stories.some(s => s.id === this.activeStoryToView?.id) || false;
  }

  deleteStory() {
    if (!this.activeStoryToView) return;
    if (!confirm('Are you sure you want to delete this story?')) return;

    this.storyService.deleteStory(this.activeStoryToView.id).subscribe({
      next: () => {
        this.closeStoryView();
        this.loadStories();
      },
      error: (err) => {
        alert(err?.error?.message || 'Failed to delete story');
      }
    });
  }
}
