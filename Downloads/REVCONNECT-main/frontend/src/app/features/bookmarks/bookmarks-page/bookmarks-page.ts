import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Navbar } from '../../../core/components/navbar/navbar';
import { Sidebar } from '../../../core/components/sidebar/sidebar';
import { BookmarkService, BookmarkResponse } from '../../../core/services/bookmark.service';
import { SearchService } from '../../../core/services/search.service';
import { UserService, UserResponse } from '../../../core/services/user.service';
import { ConnectionService } from '../../../core/services/connection.service';
import { HashtagTextComponent } from '../../../shared/components/hashtag-text/hashtag-text.component';
import { InteractionService } from '../../../core/services/interaction.service';
import { MessageService } from '../../../core/services/message.service';
import { AudioPlayerService } from '../../../core/services/audio-player.service';
import { FormsModule } from '@angular/forms';
import { getRelativeTime as sharedGetRelativeTime } from '../../../shared/utils/time.utils';

@Component({
    selector: 'app-bookmarks-page',
    standalone: true,
    imports: [CommonModule, RouterModule, Navbar, Sidebar, HashtagTextComponent, FormsModule],
    templateUrl: './bookmarks-page.html',
    styleUrls: ['./bookmarks-page.css']
})
export class BookmarksPage implements OnInit {
    bookmarks: BookmarkResponse[] = [];
    isLoading = false;

    // Share Modal State
    shareModalOpen = false;
    sharePostId: number | null = null;
    shareUserSearch = '';
    followingUsers: any[] = [];
    shareSuccessMap: { [userId: number]: boolean } = {};
    currentUser: UserResponse | null = null;

    constructor(
        private bookmarkService: BookmarkService,
        private searchService: SearchService,
        private userService: UserService,
        private connectionService: ConnectionService,
        private interactionService: InteractionService,
        private messageService: MessageService,
        private audioService: AudioPlayerService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.loadBookmarks();
        this.userService.getMyProfile().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.currentUser = res.data;
                }
            }
        });
    }

    loadBookmarks() {
        this.isLoading = true;
        this.cdr.markForCheck();

        this.bookmarkService.getBookmarks(0, 20).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.bookmarks = res.data.content;
                }
                this.isLoading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.isLoading = false;
                this.cdr.markForCheck();
            }
        });
    }

    // Use post.id (from embedded PostResponse) - there is no standalone postId on BookmarkResponse
    removeBookmark(postId: number) {
        this.bookmarkService.removeBookmark(postId).subscribe({
            next: () => {
                this.bookmarks = this.bookmarks.filter(b => b.post?.id !== postId);
                this.cdr.markForCheck();
            }
        });
    }

    viewProfile(userId: number) {
        this.router.navigate(['/profile', userId]);
    }

    viewFullPost(postId: number) {
        // Since there is no standalone post page yet, we navigate to the author's profile
        // where the user can find the post in their feed.
        const bookmark = this.bookmarks.find(b => b.post.id === postId);
        if (bookmark?.post.authorId) {
            this.router.navigate(['/profile', bookmark.post.authorId]);
        }
    }

    openShareModal(postId: number) {
        this.sharePostId = postId;
        this.shareUserSearch = '';
        this.shareSuccessMap = {};
        this.followingUsers = [];
        this.shareModalOpen = true;
        const userId = this.currentUser?.id;
        if (!userId) return;
        this.connectionService.getFollowing(userId, 0, 100).subscribe({
            next: (res) => {
                if (res.success && res.data?.content) {
                    this.followingUsers = res.data.content.map((c: any) => ({
                        userId: c.userId,
                        username: c.username,
                        name: c.name,
                        profilePicture: c.profilePicture
                    }));
                    this.cdr.markForCheck();
                }
            },
            error: (err) => console.error('Error fetching following users:', err)
        });
    }

    closeShareModal() {
        this.shareModalOpen = false;
        this.sharePostId = null;
    }

    get filteredShareUsers() {
        const q = this.shareUserSearch.toLowerCase();
        if (!q) return this.followingUsers;
        return this.followingUsers.filter(u =>
            u.username.toLowerCase().includes(q) || u.name.toLowerCase().includes(q)
        );
    }

    sendPostToUser(recipientId: number) {
        if (!this.sharePostId || this.shareSuccessMap[recipientId]) return;
        const postUrl = `${window.location.origin}/post/${this.sharePostId}`;
        const message = `Check out this post: ${postUrl}`;
        const currentSharePostId = this.sharePostId;

        // Optimistic UI update
        const bookmarkToUpdate = this.bookmarks.find(b => b.post.id === currentSharePostId);
        if (bookmarkToUpdate?.post) {
            bookmarkToUpdate.post.shareCount++;
            this.cdr.markForCheck();
        }

        this.messageService.createConversation(recipientId).subscribe({
            next: (res) => {
                const conversationId = res.data?.userId ?? recipientId;
                this.messageService.sendMessage(conversationId, message).subscribe({
                    next: () => {
                        this.shareSuccessMap[recipientId] = true;

                        // Tell backend to increment the counter
                        this.interactionService.incrementShareCount(currentSharePostId).subscribe({
                            next: () => {
                            },
                            error: (err) => console.error('Error incrementing share count:', err)
                        });
                    },
                    error: (err) => {
                        console.error('Error sending share message:', err);
                        if (bookmarkToUpdate?.post) {
                            bookmarkToUpdate.post.shareCount = Math.max(0, bookmarkToUpdate.post.shareCount - 1);
                            this.cdr.markForCheck();
                        }
                    }
                });
            },
            error: (err) => {
                console.error('Error creating conversation:', err);
                if (bookmarkToUpdate?.post) {
                    bookmarkToUpdate.post.shareCount = Math.max(0, bookmarkToUpdate.post.shareCount - 1);
                    this.cdr.markForCheck();
                }
            }
        });
    }

    getRelativeTime(value: any): string {
        return sharedGetRelativeTime(value);
    }

    togglePostAudio(post: any) {
        if (post.songTitle) {
            this.audioService.toggle(post.songTitle, post.songGenre || '');
            this.cdr.markForCheck();
        }
    }

    isPostPlaying(post: any): boolean {
        if (!post.songTitle) return false;
        return this.audioService.getIsPlaying() && this.audioService.getCurrentSongKey() === `${post.songTitle}::${post.songGenre || ''}`;
    }
}
