import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, ElementRef, signal } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { CallService, CallState } from './core/services/call.service';
import { AuthService } from './core/services/auth.service';
import { PushNotificationService } from './core/services/push-notification.service';
import { InstallPrompt } from './core/components/install-prompt/install-prompt';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, InstallPrompt],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('revconnect-ui');

  callState: CallState | null = null;
  private callSub?: Subscription;
  private localStreamSub?: Subscription;
  private remoteStreamSub?: Subscription;
  private authCheckInterval: any = null;
  private pollingStarted = false;

  // Video call support
  @ViewChild('gLocalVideo') localVideoRef?: ElementRef<HTMLVideoElement>;
  @ViewChild('gRemoteVideo') remoteVideoRef?: ElementRef<HTMLVideoElement>;
  localStream: MediaStream | null = null;
  remoteStream: MediaStream | null = null;
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
    public callService: CallService,
    private authService: AuthService,
    private pushService: PushNotificationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
      document.body.classList.add('light-theme');
    }

    // Request browser notification permission early
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }

    // Subscribe to call state globally
    this.callSub = this.callService.callState$.subscribe(state => {
      const wasRinging = this.callState?.status === 'ringing' && this.callState?.direction === 'incoming';
      this.callState = state;
      this.cdr.detectChanges();

      // Show browser notification for incoming calls
      if (state?.status === 'ringing' && state?.direction === 'incoming' && !wasRinging) {
        this.showBrowserNotification(state);
      }

      // Attach video elements when video call connects
      if (state?.callType === 'video' && (state.status === 'connecting' || state.status === 'connected')) {
        setTimeout(() => {
          this.attachLocalVideo();
          this.attachRemoteVideo();
        }, 300);
      }
    });

    // Subscribe to local/remote streams for video
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

    // Start polling if user is logged in; check periodically
    this.startPollingIfLoggedIn();
    this.authCheckInterval = setInterval(() => {
      this.startPollingIfLoggedIn();
    }, 3000);

    // Listen for push messages from service worker (incoming call when app is open)
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('message', (event) => {
        const msg = event.data;
        if (!msg) return;
        if (msg.type === 'incoming-call-push' || msg.type === 'open-call-push') {
          // Force an immediate poll to pick up the incoming call signal
          this.callService.forcePoll();
        }
        if (msg.type === 'answer-call-push') {
          // Force poll and then auto-accept when call state arrives
          this.callService.forcePoll();
          setTimeout(() => {
            const state = this.callService.getCallState();
            if (state?.status === 'ringing' && state?.direction === 'incoming') {
              this.callService.acceptCall();
            }
          }, 2000);
        }
      });
    }

    // When app regains focus, force-poll for any pending signals
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        this.startPollingIfLoggedIn();
        this.callService.forcePoll();
      }
    });
  }

  ngOnDestroy() {
    this.callSub?.unsubscribe();
    this.localStreamSub?.unsubscribe();
    this.remoteStreamSub?.unsubscribe();
    if (this.authCheckInterval) clearInterval(this.authCheckInterval);
    this.callService.stopPolling();
  }

  // ─── Video helpers ───
  private attachLocalVideo(retries = 20): void {
    const el = this.localVideoRef?.nativeElement;
    if (el && this.localStream) {
      if (el.srcObject !== this.localStream) {
        el.srcObject = this.localStream;
        el.play().catch(() => {});
      }
    } else if (retries > 0 && this.localStream) {
      setTimeout(() => this.attachLocalVideo(retries - 1), 150);
    }
  }

  private attachRemoteVideo(retries = 20): void {
    const el = this.remoteVideoRef?.nativeElement;
    if (el && this.remoteStream) {
      if (el.srcObject !== this.remoteStream) {
        el.srcObject = this.remoteStream;
        el.play().catch(() => {});
      }
    } else if (retries > 0 && this.remoteStream) {
      setTimeout(() => this.attachRemoteVideo(retries - 1), 150);
    }
  }

  toggleVideoSwap(): void {
    this.videoSwapped = !this.videoSwapped;
    this.pipX = 20;
    this.pipY = 20;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.attachLocalVideo();
      this.attachRemoteVideo();
    }, 100);
  }

  onPipPointerDown(event: PointerEvent): void {
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
      if (!this.pipMoved) this.toggleVideoSwap();
    };

    document.addEventListener('pointermove', onMove);
    document.addEventListener('pointerup', onUp);
    event.preventDefault();
  }

  private startPollingIfLoggedIn() {
    if (this.pollingStarted) return;
    const token = localStorage.getItem('revconnect_token');
    if (token) {
      this.callService.startPolling();
      this.pollingStarted = true;
      // Subscribe to push notifications for background call alerts
      this.pushService.subscribeToPush();
    }
  }

  private showBrowserNotification(state: CallState) {
    if (!('Notification' in window) || Notification.permission !== 'granted') return;
    try {
      const notif = new Notification(`Incoming ${state.callType} call`, {
        body: `${state.remoteName} is calling you`,
        icon: state.remotePic || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(state.remoteName) + '&background=random&size=128',
        tag: 'incoming-call',
        requireInteraction: true,
      });
      notif.onclick = () => {
        window.focus();
        notif.close();
      };
      // Auto-close after 30s
      setTimeout(() => notif.close(), 30000);
    } catch (e) {
      console.warn('Browser notification failed:', e);
    }
  }

  /** Whether the current route is messages page (to avoid duplicate overlay) */
  get isOnMessagesPage(): boolean {
    return this.router.url.startsWith('/messages');
  }
}
