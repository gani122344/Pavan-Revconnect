import { Component, OnInit, OnDestroy, ChangeDetectorRef, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { Navbar } from '../../../core/components/navbar/navbar';
import { Sidebar } from '../../../core/components/sidebar/sidebar';
import { MessageService, ConversationPartner, MessageItem } from '../../../core/services/message.service';
import { UserService } from '../../../core/services/user.service';
import { MediaService } from '../../../core/services/media.service';
import { LinkifyPipe } from '../../../shared/pipes/linkify-pipe';
import { CallService, CallState } from '../../../core/services/call.service';
import { Subscription } from 'rxjs';
import { getRelativeTime as sharedGetRelativeTime } from '../../../shared/utils/time.utils';

/**
 * HOW MESSAGES WORK (connected to backend MessageController):
 *
 * Backend uses the OTHER USER's ID as the "conversationId":
 *   - GET  /api/messages/conversations          → list of users you've chatted with
 *   - POST /api/messages/conversations?recipientId=X  → start/open a conversation
 *   - GET  /api/messages/conversations/{userId}  → fetch messages with that user
 *   - POST /api/messages/conversations/{userId}?content=X  → send a message to that user
 *   - POST /api/messages/conversations/{userId}/read  → mark all messages as read
 *   - GET  /api/messages/unread/count            → total unread message count
 *
 * So "conversationId" = the OTHER user's userId throughout.
 */
@Component({
    selector: 'app-messages-page',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, Navbar, Sidebar, LinkifyPipe],
    templateUrl: './messages-page.html',
    styleUrls: ['./messages-page.css']
})
export class MessagesPage implements OnInit, OnDestroy {
    @ViewChild('messagesEnd') messagesEnd?: ElementRef;
    @ViewChild('localVideo') localVideoRef?: ElementRef<HTMLVideoElement>;
    @ViewChild('remoteVideo') remoteVideoRef?: ElementRef<HTMLVideoElement>;

    conversations: ConversationPartner[] = [];
    selectedConversation: ConversationPartner | null = null;
    messages: MessageItem[] = [];
    newMessage = '';

    isLoadingConversations = false;
    isLoadingMessages = false;
    isSending = false;

    currentUserId: number = 0;
    currentUserType: string = 'PERSONAL';

    // Emoji picker
    showEmojiPicker = false;
    commonEmojis = ['😀','😂','😍','🥰','😊','🤔','😎','❤️','🔥','🎉','👍','💯','😄','😢','🥳','🤝','✨','💪','🙏','👋'];

    // Photo upload
    isUploadingPhoto = false;
    photoPreviewUrl: string | null = null;
    pendingMediaUrl: string | null = null;

    // Location sharing
    isSendingLocation = false;

    // Lightbox
    lightboxVisible = false;
    lightboxImageUrl: string = '';

    // Voice recording
    isRecording = false;
    recordingTime = 0;
    private mediaRecorder: MediaRecorder | null = null;
    private recordedChunks: Blob[] = [];
    private recordingInterval: any = null;
    playingVoiceId: number | null = null;
    private voiceAudio: HTMLAudioElement | null = null;
    voiceWaveformBars: number[] = Array.from({ length: 28 }, () => Math.random() * 16 + 4);
    voiceProgressIndex = 0;
    private voiceProgressInterval: any = null;

    // Document upload
    isUploadingDoc = false;

    // Context menu (right-click)
    contextMenuVisible = false;
    contextMenuX = 0;
    contextMenuY = 0;
    contextMenuMessage: MessageItem | null = null;

    // Edit mode
    editingMessageId: number | null = null;
    editingContent = '';

    // Calls
    callState: CallState | null = null;
    localStream: MediaStream | null = null;
    remoteStream: MediaStream | null = null;
    private callSub?: Subscription;
    private localStreamSub?: Subscription;
    private remoteStreamSub?: Subscription;

    // Video PiP drag & swap
    videoSwapped = false;
    pipX = 20;
    pipY = 20;
    private pipDragging = false;
    private pipStartX = 0;
    private pipStartY = 0;
    private pipOrigX = 0;
    private pipOrigY = 0;
    private pipMoved = false;

    constructor(
        private messageService: MessageService,
        private userService: UserService,
        private mediaService: MediaService,
        public callService: CallService,
        private route: ActivatedRoute,
        public router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    @HostListener('document:click')
    onDocumentClick() {
        if (this.contextMenuVisible) {
            this.contextMenuVisible = false;
            this.cdr.detectChanges();
        }
    }

    private countPollSub: any;
    private convPollSub: any;
    private msgPollSub: any;
    private timestampRefreshSub: any;

    ngOnInit() {
        this.loadCurrentUser();

        // While user is on messages page, periodically refresh unread count so navbar badge stays in sync
        this.countPollSub = setInterval(() => {
            this.messageService.refreshUnreadCount();
        }, 5000);

        // Periodically refresh conversation list to pick up incoming messages & re-sort
        this.convPollSub = setInterval(() => {
            this.refreshConversationOrder();
        }, 4000);

        // Poll for new messages in active chat
        this.msgPollSub = setInterval(() => {
            if (this.selectedConversation && !this.isSending) {
                this.pollNewMessages(this.selectedConversation.userId);
            }
        }, 5000);

        // Periodically trigger change detection so relative timestamps update
        this.timestampRefreshSub = setInterval(() => {
            this.cdr.detectChanges();
        }, 30000);

        // Subscribe to call state (polling is managed globally by App component)
        this.callSub = this.callService.callState$.subscribe(state => {
            this.callState = state;
            this.cdr.detectChanges();
            if (state?.callType === 'video' && (state.status === 'connecting' || state.status === 'connected')) {
                setTimeout(() => {
                    this.attachLocalVideo();
                    this.attachRemoteVideo();
                }, 300);
            }
        });
        this.localStreamSub = this.callService.localStream$.subscribe(stream => {
            this.localStream = stream;
            this.cdr.detectChanges();
            setTimeout(() => this.attachLocalVideo(), 100);
        });
        this.remoteStreamSub = this.callService.remoteStream$.subscribe(stream => {
            this.remoteStream = stream;
            this.cdr.detectChanges();
            setTimeout(() => this.attachRemoteVideo(), 100);
        });

        // If navigated from profile page with a userId, open that conversation
        this.route.queryParams.subscribe(params => {
            const userId = params['userId'];
            if (userId) {
                const partnerId = +userId;
                // First create/open the conversation with this user
                this.messageService.createConversation(partnerId).subscribe({
                    next: (res) => {
                        if (res.success && res.data) {
                            // Backend returns { recipientId } - need to fetch full user profile
                            this.userService.getUserById(partnerId).subscribe({
                                next: (userRes) => {
                                    if (userRes.success && userRes.data) {
                                        const partner: ConversationPartner = {
                                            userId: userRes.data.id,
                                            username: userRes.data.username,
                                            name: userRes.data.name,
                                            profilePicture: userRes.data.profilePicture || ''
                                        };
                                        this.selectConversation(partner);
                                    }
                                    this.loadConversations();
                                },
                                error: () => this.loadConversationsAndSelect(partnerId)
                            });
                        } else {
                            this.loadConversationsAndSelect(partnerId);
                        }
                    },
                    error: () => {
                        this.loadConversationsAndSelect(partnerId);
                    }
                });
            } else {
                this.loadConversations();
            }
        });
    }

    ngOnDestroy() {
        document.body.classList.remove('messages-fullscreen');
        if (this.countPollSub) clearInterval(this.countPollSub);
        if (this.convPollSub) clearInterval(this.convPollSub);
        if (this.msgPollSub) clearInterval(this.msgPollSub);
        if (this.timestampRefreshSub) clearInterval(this.timestampRefreshSub);
        this.callSub?.unsubscribe();
        this.localStreamSub?.unsubscribe();
        this.remoteStreamSub?.unsubscribe();
    }

    private refreshConversationOrder() {
        this.messageService.getConversations().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const sorted = res.data.sort((a: any, b: any) => {
                        const ta = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
                        const tb = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
                        return tb - ta;
                    });
                    // Preserve selection, just update order
                    this.conversations = sorted;
                    this.cdr.markForCheck();
                }
            }
        });
    }

    private pollNewMessages(conversationId: number) {
        this.messageService.getMessages(conversationId, 0, 50).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    const freshMessages = [...res.data].reverse();
                    if (freshMessages.length > this.messages.length) {
                        this.messages = freshMessages;
                        this.cdr.detectChanges();
                        this.scrollToBottom();
                        // Move this conversation to top of list
                        const idx = this.conversations.findIndex(c => Number(c.userId) === conversationId);
                        if (idx > 0) {
                            const [conv] = this.conversations.splice(idx, 1);
                            (conv as any).lastMessageTime = new Date().toISOString();
                            this.conversations.unshift(conv);
                            this.cdr.markForCheck();
                        }
                    }
                }
            }
        });
    }

    loadCurrentUser() {
        this.userService.getMyProfile().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.currentUserId = res.data.id;
                    this.currentUserType = res.data.userType || 'PERSONAL';
                    this.cdr.markForCheck();
                }
            }
        });
    }

    loadConversations() {
        this.isLoadingConversations = true;
        this.cdr.markForCheck();

        // GET /api/messages/conversations
        this.messageService.getConversations().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    // Sort by lastMessageTime descending (newest first)
                    this.conversations = res.data.sort((a: any, b: any) => {
                        const ta = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
                        const tb = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
                        return tb - ta;
                    });
                    // Enrich conversations missing user data (Feign enrichment may fail)
                    this.conversations.forEach(conv => {
                        if (!conv.name || !conv.username) {
                            this.userService.getUserById(conv.userId).subscribe({
                                next: (userRes) => {
                                    if (userRes.success && userRes.data) {
                                        conv.name = userRes.data.name;
                                        conv.username = userRes.data.username;
                                        conv.profilePicture = userRes.data.profilePicture || '';
                                        this.cdr.markForCheck();
                                    }
                                }
                            });
                        }
                    });
                }
                this.isLoadingConversations = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.isLoadingConversations = false;
                this.cdr.markForCheck();
            }
        });
    }

    loadConversationsAndSelect(partnerId: number) {
        this.messageService.getConversations().subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.conversations = res.data.sort((a: any, b: any) => {
                        const ta = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
                        const tb = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
                        return tb - ta;
                    });
                    // Auto-select the conversation with this partner
                    const partner = this.conversations.find(c => Number(c.userId) === partnerId);
                    if (partner) {
                        this.selectConversation(partner);
                    } else {
                        // Partner not in list yet - load their profile and add
                        this.userService.getUserById(partnerId).subscribe({
                            next: (userRes) => {
                                if (userRes.success && userRes.data) {
                                    const newPartner: ConversationPartner = {
                                        userId: userRes.data.id,
                                        username: userRes.data.username,
                                        name: userRes.data.name,
                                        profilePicture: userRes.data.profilePicture || ''
                                    };
                                    // Make sure we don't accidentally add duplicates if it somehow resolved in the meantime
                                    if (!this.conversations.some(c => Number(c.userId) === partnerId)) {
                                        this.conversations.unshift(newPartner);
                                    }
                                    this.selectConversation(newPartner);
                                    this.cdr.markForCheck();
                                }
                            }
                        });
                    }
                }
                this.cdr.markForCheck();
            }
        });
    }

    goBackToList() {
        this.selectedConversation = null;
        document.body.classList.remove('messages-fullscreen');
        this.cdr.detectChanges();
    }

    selectConversation(partner: ConversationPartner) {
        this.selectedConversation = partner;
        document.body.classList.add('messages-fullscreen');
        this.messages = [];
        this.loadMessages(partner.userId);
        // Ensure the partner is in the conversations list (for sidebar highlighting)
        if (!this.conversations.some(c => Number(c.userId) === Number(partner.userId))) {
            this.conversations.unshift(partner);
        }
        // Clear the unread badge immediately in UI
        const conv = this.conversations.find(c => Number(c.userId) === Number(partner.userId));
        if (conv) {
            conv.unreadCount = 0;
        }
        // POST /api/messages/conversations/{userId}/read
        this.messageService.markConversationAsRead(partner.userId).subscribe({
            next: () => this.messageService.refreshUnreadCount()
        });
        this.cdr.markForCheck();
    }

    loadMessages(conversationId: number) {
        this.isLoadingMessages = true;
        this.cdr.markForCheck();

        // GET /api/messages/conversations/{userId}?page=0&size=50
        this.messageService.getMessages(conversationId, 0, 50).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    // Backend returns newest first — reverse for chronological display
                    this.messages = [...res.data].reverse();
                }
                this.isLoadingMessages = false;
                this.cdr.detectChanges();
                this.scrollToBottom(true);
            },
            error: () => {
                this.isLoadingMessages = false;
                this.cdr.markForCheck();
            }
        });
    }

    sendMessage() {
        const content = this.newMessage.trim();
        const mediaUrl = this.pendingMediaUrl;
        if ((!content && !mediaUrl) || !this.selectedConversation || this.isSending) return;

        this.isSending = true;
        this.showEmojiPicker = false;
        const conversationId = this.selectedConversation.userId;
        const tempMessage = content;
        this.newMessage = '';
        this.pendingMediaUrl = null;
        this.photoPreviewUrl = null;

        // Optimistic: add message to UI immediately
        const optimisticMsg: MessageItem = {
            id: Date.now(),
            senderId: this.currentUserId,
            receiverId: conversationId,
            content: tempMessage,
            mediaUrl: mediaUrl || null,
            timestamp: new Date().toISOString(),
            isRead: false,
            isDeleted: false
        };
        this.messages.push(optimisticMsg);
        this.cdr.markForCheck();
        setTimeout(() => this.scrollToBottom(), 50);

        this.messageService.sendMessage(conversationId, tempMessage, mediaUrl || undefined).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    optimisticMsg.id = res.data.messageId;
                }
                // Move active conversation to top of list (newest message first)
                const idx = this.conversations.findIndex(c => Number(c.userId) === conversationId);
                if (idx > 0) {
                    const [conv] = this.conversations.splice(idx, 1);
                    (conv as any).lastMessageTime = new Date().toISOString();
                    this.conversations.unshift(conv);
                }
                this.isSending = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.messages = this.messages.filter(m => m.id !== optimisticMsg.id);
                this.newMessage = tempMessage;
                this.pendingMediaUrl = mediaUrl;
                this.isSending = false;
                this.cdr.markForCheck();
            }
        });
    }

    onPhotoSelected(event: Event) {
        const input = event.target as HTMLInputElement;
        if (!input.files || !input.files[0]) return;
        const file = input.files[0];
        this.isUploadingPhoto = true;
        this.cdr.markForCheck();

        this.mediaService.uploadFile(file).subscribe({
            next: (res) => {
                if (res.success && res.data?.url) {
                    this.pendingMediaUrl = res.data.url;
                    this.photoPreviewUrl = res.data.url;
                }
                this.isUploadingPhoto = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.isUploadingPhoto = false;
                this.cdr.markForCheck();
            }
        });
        input.value = '';
    }

    removePendingPhoto() {
        this.pendingMediaUrl = null;
        this.photoPreviewUrl = null;
        this.cdr.markForCheck();
    }

    openContextMenu(event: MouseEvent, msg: MessageItem) {
        if (!this.isMine(msg)) return;
        event.preventDefault();
        event.stopPropagation();
        this.contextMenuVisible = true;
        this.contextMenuX = event.clientX;
        this.contextMenuY = event.clientY;
        this.contextMenuMessage = msg;
        this.cdr.detectChanges();
    }

    startEditMessage() {
        if (!this.contextMenuMessage) return;
        this.editingMessageId = this.contextMenuMessage.id;
        this.editingContent = this.contextMenuMessage.content;
        this.contextMenuVisible = false;
        this.cdr.detectChanges();
    }

    saveEditMessage(msg: MessageItem) {
        const content = this.editingContent.trim();
        if (!content) return;
        this.messageService.editMessage(msg.id, content).subscribe({
            next: () => {
                msg.content = content;
                this.editingMessageId = null;
                this.editingContent = '';
                this.cdr.markForCheck();
            }
        });
    }

    cancelEditMessage() {
        this.editingMessageId = null;
        this.editingContent = '';
        this.cdr.markForCheck();
    }

    deleteMessage() {
        if (!this.contextMenuMessage) return;
        const msgId = this.contextMenuMessage.id;
        this.contextMenuVisible = false;
        this.contextMenuMessage = null;
        this.messageService.deleteMessage(msgId).subscribe({
            next: () => {
                this.messages = this.messages.filter(m => m.id !== msgId);
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                console.error('Delete message failed:', err);
                this.cdr.detectChanges();
            }
        });
    }

    toggleEmojiPicker() {
        this.showEmojiPicker = !this.showEmojiPicker;
        this.cdr.markForCheck();
    }

    insertEmoji(emoji: string) {
        this.newMessage += emoji;
        this.cdr.markForCheck();
    }

    isMine(message: MessageItem): boolean {
        return message.senderId === this.currentUserId;
    }

    getAvatarUrl(partner: ConversationPartner): string {
        return partner.profilePicture ||
            `https://ui-avatars.com/api/?name=${encodeURIComponent(partner.name || partner.username)}&background=random`;
    }

    getRelativeTime(value: any): string {
        return sharedGetRelativeTime(value);
    }

    formatMessage(content: string): string {
        if (!content) return '';
        const urlRegex = /(https?:\/\/[^\s]+)/g;
        return content.replace(urlRegex, (url) => {
            return `<a href="${url}" target="_blank" rel="noopener noreferrer" class="msg-link">${url}</a>`;
        });
    }

    isCallEvent(msg: MessageItem): boolean {
        return !!msg.content && msg.content.startsWith('[[CALL|') && msg.content.endsWith(']]');
    }

    getCallEventData(msg: MessageItem): { type: string; status: string; duration: number } {
        try {
            const inner = msg.content.substring(2, msg.content.length - 2);
            const parts = inner.split('|');
            return {
                type: parts[1] || 'audio',
                status: parts[2] || 'ended',
                duration: parseInt(parts[3], 10) || 0
            };
        } catch {
            return { type: 'audio', status: 'ended', duration: 0 };
        }
    }

    formatCallDuration(seconds: number): string {
        if (seconds < 60) return seconds + 's';
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        if (m < 60) return m + 'm ' + (s > 0 ? s + 's' : '');
        const h = Math.floor(m / 60);
        return h + 'h ' + (m % 60) + 'm';
    }

    isSharedPost(msg: MessageItem): boolean {
        if (!msg.content) return false;
        return msg.content.startsWith('[[SHARED_POST:') && msg.content.endsWith(']]');
    }

    getSharedPostData(msg: MessageItem): any {
        if (!msg.content) return null;
        try {
            const json = msg.content.substring('[[SHARED_POST:'.length, msg.content.length - 2);
            return JSON.parse(json);
        } catch {
            return null;
        }
    }

    getSharedPostAuthor(msg: MessageItem): string {
        const data = this.getSharedPostData(msg);
        return data?.authorUsername || '';
    }

    getSharedPostContent(msg: MessageItem): string {
        const data = this.getSharedPostData(msg);
        return data?.content || '';
    }

    private attachLocalVideo(retries = 20) {
        const el = this.localVideoRef?.nativeElement as HTMLVideoElement;
        if (el && this.localStream) {
            if (el.srcObject !== this.localStream) {
                el.srcObject = this.localStream;
                el.play().catch(() => {});
            }
        } else if (retries > 0 && this.localStream) {
            setTimeout(() => this.attachLocalVideo(retries - 1), 150);
        }
    }

    private attachRemoteVideo(retries = 20) {
        const el = this.remoteVideoRef?.nativeElement as HTMLVideoElement;
        if (el && this.remoteStream) {
            if (el.srcObject !== this.remoteStream) {
                el.srcObject = this.remoteStream;
                el.play().catch(() => {});
            }
        } else if (retries > 0 && this.remoteStream) {
            setTimeout(() => this.attachRemoteVideo(retries - 1), 150);
        }
    }

    // Video PiP: tap to swap big/small
    toggleVideoSwap() {
        this.videoSwapped = !this.videoSwapped;
        this.pipX = 20;
        this.pipY = 20;
        this.cdr.detectChanges();
        setTimeout(() => {
            this.attachLocalVideo();
            this.attachRemoteVideo();
        }, 100);
    }

    // Video PiP: drag start
    onPipPointerDown(event: PointerEvent) {
        this.pipDragging = true;
        this.pipMoved = false;
        this.pipStartX = event.clientX;
        this.pipStartY = event.clientY;
        this.pipOrigX = this.pipX;
        this.pipOrigY = this.pipY;
        const target = event.target as HTMLElement;
        target.setPointerCapture(event.pointerId);

        const onMove = (e: PointerEvent) => {
            if (!this.pipDragging) return;
            const dx = e.clientX - this.pipStartX;
            const dy = e.clientY - this.pipStartY;
            if (Math.abs(dx) > 5 || Math.abs(dy) > 5) this.pipMoved = true;
            this.pipX = this.pipOrigX + dx;
            this.pipY = this.pipOrigY + dy;
            this.cdr.detectChanges();
        };

        const onUp = (e: PointerEvent) => {
            this.pipDragging = false;
            target.releasePointerCapture(e.pointerId);
            document.removeEventListener('pointermove', onMove);
            document.removeEventListener('pointerup', onUp);
            // If it was just a tap (no drag), swap
            if (!this.pipMoved) {
                this.toggleVideoSwap();
            }
        };

        document.addEventListener('pointermove', onMove);
        document.addEventListener('pointerup', onUp);
        event.preventDefault();
    }

    startAudioCall() {
        if (!this.selectedConversation) return;
        const p = this.selectedConversation;
        this.callService.initiateCall(p.userId, p.name || p.username, p.username, p.profilePicture || '', 'audio');
    }

    startVideoCall() {
        if (!this.selectedConversation) return;
        const p = this.selectedConversation;
        this.callService.initiateCall(p.userId, p.name || p.username, p.username, p.profilePicture || '', 'video');
    }

    // ─── Location Sharing ───
    sendCurrentLocation() {
        if (!this.selectedConversation || this.isSendingLocation) return;
        if (!navigator.geolocation) {
            alert('Geolocation is not supported by your browser.');
            return;
        }
        this.isSendingLocation = true;
        this.cdr.detectChanges();

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;
                const locationContent = `[location:${lat},${lng}]`;
                this.messageService.sendMessage(this.selectedConversation!.userId, locationContent).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.messages.push({
                                id: res.data?.messageId || Date.now(),
                                senderId: this.currentUserId,
                                receiverId: this.selectedConversation!.userId,
                                content: locationContent,
                                mediaUrl: null,
                                timestamp: new Date().toISOString(),
                                isRead: false,
                                isDeleted: false,
                            });
                            this.scrollToBottom();
                        }
                        this.isSendingLocation = false;
                        this.cdr.detectChanges();
                    },
                    error: () => {
                        alert('Failed to send location. Please try again.');
                        this.isSendingLocation = false;
                        this.cdr.detectChanges();
                    }
                });
            },
            (error) => {
                this.isSendingLocation = false;
                this.cdr.detectChanges();
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        alert('Location permission denied. Please allow location access in your browser settings.');
                        break;
                    case error.POSITION_UNAVAILABLE:
                        alert('Location information is unavailable.');
                        break;
                    case error.TIMEOUT:
                        alert('Location request timed out. Please try again.');
                        break;
                    default:
                        alert('An error occurred while getting your location.');
                }
            },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
        );
    }

    isLocationMessage(msg: MessageItem): boolean {
        return msg.content?.startsWith('[location:') && msg.content?.endsWith(']');
    }

    getLocationCoords(msg: MessageItem): { lat: number; lng: number } {
        try {
            const match = msg.content.match(/\[location:([-\d.]+),([-\d.]+)\]/);
            if (match) {
                return { lat: parseFloat(match[1]), lng: parseFloat(match[2]) };
            }
        } catch {}
        return { lat: 0, lng: 0 };
    }

    // ─── Lightbox ───
    openLightbox(url: string) {
        this.lightboxImageUrl = url;
        this.lightboxVisible = true;
        this.cdr.detectChanges();
        this._lightboxEscHandler = (e: KeyboardEvent) => { if (e.key === 'Escape') this.closeLightbox(); };
        document.addEventListener('keydown', this._lightboxEscHandler);
    }

    closeLightbox() {
        this.lightboxVisible = false;
        this.lightboxImageUrl = '';
        if (this._lightboxEscHandler) {
            document.removeEventListener('keydown', this._lightboxEscHandler);
            this._lightboxEscHandler = null;
        }
        this.cdr.detectChanges();
    }

    private _lightboxEscHandler: ((e: KeyboardEvent) => void) | null = null;

    // ─── Voice Recording ───
    async startRecording() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            alert('Voice recording is not supported in your browser.');
            return;
        }
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            this.mediaRecorder = new MediaRecorder(stream);
            this.recordedChunks = [];
            this.recordingTime = 0;
            this.isRecording = true;
            this.cdr.detectChanges();

            this.mediaRecorder.ondataavailable = (e) => {
                if (e.data.size > 0) this.recordedChunks.push(e.data);
            };

            this.mediaRecorder.start();
            this.recordingInterval = setInterval(() => {
                this.recordingTime++;
                this.cdr.detectChanges();
            }, 1000);
        } catch (err) {
            alert('Microphone access denied. Please allow microphone access.');
        }
    }

    cancelRecording() {
        if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
            this.mediaRecorder.stop();
            this.mediaRecorder.stream.getTracks().forEach(t => t.stop());
        }
        if (this.recordingInterval) clearInterval(this.recordingInterval);
        this.isRecording = false;
        this.recordedChunks = [];
        this.recordingTime = 0;
        this.cdr.detectChanges();
    }

    stopAndSendRecording() {
        if (!this.mediaRecorder || !this.selectedConversation) return;
        this.mediaRecorder.onstop = () => {
            this.mediaRecorder!.stream.getTracks().forEach(t => t.stop());
            if (this.recordingInterval) clearInterval(this.recordingInterval);
            this.isRecording = false;

            const blob = new Blob(this.recordedChunks, { type: 'audio/webm' });
            const file = new File([blob], `voice_${Date.now()}.webm`, { type: 'audio/webm' });
            const duration = this.recordingTime;
            this.recordingTime = 0;
            this.cdr.detectChanges();

            this.mediaService.uploadFile(file).subscribe({
                next: (res) => {
                    if (res.success && res.data?.url) {
                        const voiceContent = `[voice:${res.data.url}|${duration}]`;
                        this.messageService.sendMessage(this.selectedConversation!.userId, voiceContent).subscribe({
                            next: (msgRes) => {
                                if (msgRes.success) {
                                    this.messages.push({
                                        id: msgRes.data?.messageId || Date.now(),
                                        senderId: this.currentUserId,
                                        receiverId: this.selectedConversation!.userId,
                                        content: voiceContent,
                                        mediaUrl: null,
                                        timestamp: new Date().toISOString(),
                                        isRead: false,
                                        isDeleted: false,
                                    });
                                    this.scrollToBottom();
                                    this.cdr.detectChanges();
                                }
                            },
                            error: () => alert('Failed to send voice message.')
                        });
                    }
                },
                error: () => alert('Failed to upload voice recording.')
            });
        };
        this.mediaRecorder.stop();
    }

    isVoiceMessage(msg: MessageItem): boolean {
        return msg.content?.startsWith('[voice:') && msg.content?.endsWith(']');
    }

    getVoiceDuration(msg: MessageItem): string {
        try {
            const match = msg.content.match(/\[voice:.*\|(\d+)\]/);
            if (match) {
                const sec = parseInt(match[1]);
                const m = Math.floor(sec / 60);
                const s = sec % 60;
                return `${m}:${s.toString().padStart(2, '0')}`;
            }
        } catch {}
        return '0:00';
    }

    private getVoiceUrl(msg: MessageItem): string {
        try {
            const match = msg.content.match(/\[voice:(.*?)\|/);
            if (match) return match[1];
        } catch {}
        return '';
    }

    togglePlayVoice(msg: MessageItem) {
        const url = this.getVoiceUrl(msg);
        if (!url) return;

        if (this.playingVoiceId === msg.id && this.voiceAudio) {
            this.voiceAudio.pause();
            this.playingVoiceId = null;
            this.stopVoiceProgress();
            this.cdr.detectChanges();
            return;
        }

        if (this.voiceAudio) { this.voiceAudio.pause(); }
        this.stopVoiceProgress();
        this.voiceAudio = new Audio(url);
        this.playingVoiceId = msg.id;
        this.voiceProgressIndex = 0;
        this.voiceWaveformBars = Array.from({ length: 28 }, () => Math.random() * 16 + 4);
        this.cdr.detectChanges();
        this.voiceAudio.play().catch(() => {});
        this.voiceProgressInterval = setInterval(() => {
            if (this.voiceAudio && this.voiceAudio.duration) {
                const pct = this.voiceAudio.currentTime / this.voiceAudio.duration;
                this.voiceProgressIndex = Math.floor(pct * this.voiceWaveformBars.length);
                this.cdr.detectChanges();
            }
        }, 80);
        this.voiceAudio.onended = () => {
            this.playingVoiceId = null;
            this.voiceProgressIndex = 0;
            this.stopVoiceProgress();
            this.cdr.detectChanges();
        };
    }

    private stopVoiceProgress() {
        if (this.voiceProgressInterval) {
            clearInterval(this.voiceProgressInterval);
            this.voiceProgressInterval = null;
        }
    }

    // ─── Document Sharing ───
    onDocumentSelected(event: Event) {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length || !this.selectedConversation) return;
        const file = input.files[0];
        this.isUploadingDoc = true;
        this.cdr.detectChanges();

        this.mediaService.uploadFile(file).subscribe({
            next: (res) => {
                if (res.success && res.data?.url) {
                    const docContent = `[doc:${res.data.url}|${file.name}]`;
                    this.messageService.sendMessage(this.selectedConversation!.userId, docContent).subscribe({
                        next: (msgRes) => {
                            if (msgRes.success) {
                                this.messages.push({
                                    id: msgRes.data?.messageId || Date.now(),
                                    senderId: this.currentUserId,
                                    receiverId: this.selectedConversation!.userId,
                                    content: docContent,
                                    mediaUrl: null,
                                    timestamp: new Date().toISOString(),
                                    isRead: false,
                                    isDeleted: false,
                                });
                                this.scrollToBottom();
                            }
                            this.isUploadingDoc = false;
                            this.cdr.detectChanges();
                        },
                        error: () => {
                            alert('Failed to send document.');
                            this.isUploadingDoc = false;
                            this.cdr.detectChanges();
                        }
                    });
                }
            },
            error: () => {
                alert('Failed to upload document.');
                this.isUploadingDoc = false;
                this.cdr.detectChanges();
            }
        });
        input.value = '';
    }

    isDocumentMessage(msg: MessageItem): boolean {
        return msg.content?.startsWith('[doc:') && msg.content?.endsWith(']');
    }

    getDocumentUrl(msg: MessageItem): string {
        try {
            const match = msg.content.match(/\[doc:(.*?)\|/);
            if (match) return match[1];
        } catch {}
        return '';
    }

    getDocumentName(msg: MessageItem): string {
        try {
            const match = msg.content.match(/\[doc:.*?\|(.*?)\]/);
            if (match) return match[1];
        } catch {}
        return 'Document';
    }

    downloadDocument(msg: MessageItem) {
        const url = this.getDocumentUrl(msg);
        const name = this.getDocumentName(msg);
        if (!url) return;

        fetch(url)
            .then(res => res.blob())
            .then(blob => {
                const a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                a.download = name || 'document';
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(a.href);
            })
            .catch(() => {
                // Fallback: open in new tab
                window.open(url, '_blank');
            });
    }

    scrollToBottom(instant = false) {
        // Use multiple frames + timeout to ensure DOM is fully rendered
        setTimeout(() => {
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    try {
                        if (this.messagesEnd) {
                            this.messagesEnd.nativeElement.scrollIntoView({
                                behavior: instant ? 'auto' : 'smooth',
                                block: 'end'
                            });
                        }
                    } catch { }
                });
            });
        }, instant ? 100 : 50);
    }

}
