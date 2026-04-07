import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-install-prompt',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- Main Install Banner -->
    <div class="install-banner" *ngIf="showBanner && !showInstructions">
      <div class="install-content">
        <img src="icons/icon-96x96.png" alt="RevConnect" class="install-icon">
        <div class="install-text">
          <strong>Install RevConnect</strong>
          <span>{{ platformHint }}</span>
        </div>
      </div>
      <div class="install-actions">
        <button class="install-btn" (click)="installApp()">
          <i class="fa-solid fa-download"></i> Install
        </button>
        <button class="dismiss-btn" (click)="dismiss()">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>
    </div>

    <!-- Instructions Modal (for iOS / Firefox / manual install) -->
    <div class="instructions-overlay" *ngIf="showInstructions" (click)="showInstructions = false">
      <div class="instructions-card" (click)="$event.stopPropagation()">
        <button class="inst-close" (click)="showInstructions = false">
          <i class="fa-solid fa-xmark"></i>
        </button>
        <div class="inst-header">
          <img src="icons/icon-96x96.png" alt="RevConnect" class="inst-icon">
          <h3>Install RevConnect</h3>
          <p>Follow these steps to install the app on your device</p>
        </div>

        <!-- iOS Instructions -->
        <div class="inst-steps" *ngIf="platform === 'ios'">
          <div class="inst-step">
            <span class="step-num">1</span>
            <span>Tap the <strong>Share</strong> button <i class="fa-solid fa-arrow-up-from-bracket"></i> at the bottom of Safari</span>
          </div>
          <div class="inst-step">
            <span class="step-num">2</span>
            <span>Scroll down and tap <strong>"Add to Home Screen"</strong> <i class="fa-solid fa-plus-square"></i></span>
          </div>
          <div class="inst-step">
            <span class="step-num">3</span>
            <span>Tap <strong>"Add"</strong> in the top right corner</span>
          </div>
        </div>

        <!-- Android (non-Chrome) Instructions -->
        <div class="inst-steps" *ngIf="platform === 'android-other'">
          <div class="inst-step">
            <span class="step-num">1</span>
            <span>Open this page in <strong>Google Chrome</strong></span>
          </div>
          <div class="inst-step">
            <span class="step-num">2</span>
            <span>Tap the <strong>menu</strong> <i class="fa-solid fa-ellipsis-vertical"></i> in the top right</span>
          </div>
          <div class="inst-step">
            <span class="step-num">3</span>
            <span>Tap <strong>"Install app"</strong> or <strong>"Add to Home Screen"</strong></span>
          </div>
        </div>

        <!-- Windows / Desktop Instructions -->
        <div class="inst-steps" *ngIf="platform === 'desktop'">
          <div class="inst-step">
            <span class="step-num">1</span>
            <span>Open this page in <strong>Google Chrome</strong> or <strong>Microsoft Edge</strong></span>
          </div>
          <div class="inst-step">
            <span class="step-num">2</span>
            <span>Look for the <strong>install icon</strong> <i class="fa-solid fa-arrow-down-to-line"></i> in the address bar (right side)</span>
          </div>
          <div class="inst-step">
            <span class="step-num">3</span>
            <span>Click <strong>"Install"</strong> in the popup dialog</span>
          </div>
        </div>

        <button class="inst-got-it" (click)="showInstructions = false">Got it!</button>
      </div>
    </div>
  `,
  styles: [`
    .install-banner {
      position: fixed;
      bottom: 80px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 10000;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.75rem 1rem;
      background: linear-gradient(135deg, #7c3aed 0%, #8b5cf6 50%, #a855f7 100%);
      border-radius: 16px;
      box-shadow: 0 8px 32px rgba(139, 92, 246, 0.4), 0 0 0 1px rgba(255,255,255,0.1) inset;
      max-width: 420px;
      width: calc(100% - 2rem);
      animation: slideUp 0.4s ease-out;
      color: #fff;
    }

    @media (min-width: 821px) {
      .install-banner { bottom: 1.5rem; }
    }

    @keyframes slideUp {
      from { transform: translateX(-50%) translateY(100px); opacity: 0; }
      to { transform: translateX(-50%) translateY(0); opacity: 1; }
    }

    .install-content {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      flex: 1;
      min-width: 0;
    }

    .install-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      flex-shrink: 0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.2);
    }

    .install-text {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
    }

    .install-text strong {
      font-size: 0.9rem;
      font-weight: 700;
      letter-spacing: -0.01em;
    }

    .install-text span {
      font-size: 0.72rem;
      opacity: 0.85;
    }

    .install-actions {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      flex-shrink: 0;
    }

    .install-btn {
      background: #fff;
      color: #7c3aed;
      border: none;
      border-radius: 10px;
      padding: 0.5rem 1rem;
      font-size: 0.82rem;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.4rem;
      transition: all 0.2s;
      white-space: nowrap;
    }

    .install-btn:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .dismiss-btn {
      background: rgba(255,255,255,0.2);
      border: none;
      border-radius: 50%;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #fff;
      font-size: 0.8rem;
      transition: background 0.2s;
    }

    .dismiss-btn:hover {
      background: rgba(255,255,255,0.3);
    }

    /* Instructions Overlay */
    .instructions-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(0,0,0,0.6);
      backdrop-filter: blur(6px);
      z-index: 10001;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
      animation: fadeIn 0.25s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .instructions-card {
      background: #fff;
      border-radius: 20px;
      padding: 2rem 1.5rem;
      max-width: 380px;
      width: 100%;
      position: relative;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      animation: scaleIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    @keyframes scaleIn {
      from { transform: scale(0.9); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }

    .inst-close {
      position: absolute;
      top: 1rem;
      right: 1rem;
      background: #f1f5f9;
      border: none;
      border-radius: 50%;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #64748b;
      font-size: 0.9rem;
      transition: all 0.2s;
    }

    .inst-close:hover {
      background: #e2e8f0;
      color: #1e293b;
    }

    .inst-header {
      text-align: center;
      margin-bottom: 1.5rem;
    }

    .inst-icon {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      margin-bottom: 0.75rem;
      box-shadow: 0 4px 16px rgba(139,92,246,0.3);
    }

    .inst-header h3 {
      margin: 0 0 0.25rem;
      font-size: 1.25rem;
      font-weight: 800;
      color: #1e293b;
    }

    .inst-header p {
      margin: 0;
      font-size: 0.85rem;
      color: #64748b;
    }

    .inst-steps {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
    }

    .inst-step {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem;
      background: #f8fafc;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
    }

    .step-num {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: linear-gradient(135deg, #7c3aed, #a855f7);
      color: #fff;
      font-size: 0.8rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .inst-step span {
      font-size: 0.85rem;
      color: #334155;
      line-height: 1.4;
    }

    .inst-step i {
      color: #7c3aed;
    }

    .inst-got-it {
      width: 100%;
      padding: 0.75rem;
      background: linear-gradient(135deg, #7c3aed, #a855f7);
      color: #fff;
      border: none;
      border-radius: 12px;
      font-size: 0.95rem;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
    }

    .inst-got-it:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(139,92,246,0.4);
    }
  `]
})
export class InstallPrompt implements OnInit, OnDestroy {
  showBanner = false;
  showInstructions = false;
  platform: 'ios' | 'android' | 'android-other' | 'desktop' = 'desktop';
  platformHint = 'Get the full app experience on your device';
  private deferredPrompt: any = null;
  private boundHandler: any;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    // Don't show if already running as installed PWA
    if (window.matchMedia('(display-mode: standalone)').matches) return;
    if ((navigator as any).standalone === true) return; // iOS standalone

    // Don't show if dismissed in last 3 days
    const dismissed = localStorage.getItem('pwa-install-dismissed');
    if (dismissed) {
      const ts = parseInt(dismissed, 10);
      if (Date.now() - ts < 3 * 24 * 60 * 60 * 1000) return;
    }

    this.detectPlatform();

    // Listen for Chrome/Edge beforeinstallprompt
    this.boundHandler = (e: any) => {
      e.preventDefault();
      this.deferredPrompt = e;
      this.showBanner = true;
      this.cdr.detectChanges();
    };
    window.addEventListener('beforeinstallprompt', this.boundHandler);

    // Show banner after 3 seconds for all platforms
    // (beforeinstallprompt may fire before or after this)
    setTimeout(() => {
      if (!this.showBanner) {
        this.showBanner = true;
        this.cdr.detectChanges();
      }
    }, 3000);
  }

  ngOnDestroy() {
    if (this.boundHandler) {
      window.removeEventListener('beforeinstallprompt', this.boundHandler);
    }
  }

  private detectPlatform() {
    const ua = navigator.userAgent.toLowerCase();
    const isIOS = /iphone|ipad|ipod/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
    const isAndroid = /android/.test(ua);
    const isChrome = /chrome/.test(ua) && !/edge|edg|opr/.test(ua);

    if (isIOS) {
      this.platform = 'ios';
      this.platformHint = 'Add to your iPhone/iPad home screen';
    } else if (isAndroid && isChrome) {
      this.platform = 'android';
      this.platformHint = 'Install the app on your Android device';
    } else if (isAndroid) {
      this.platform = 'android-other';
      this.platformHint = 'Install the app on your Android device';
    } else {
      this.platform = 'desktop';
      this.platformHint = 'Install the app on your computer';
    }
  }

  async installApp() {
    // If we have the native prompt (Chrome/Edge), use it
    if (this.deferredPrompt) {
      try {
        this.deferredPrompt.prompt();
        const result = await this.deferredPrompt.userChoice;
        if (result.outcome === 'accepted') {
          this.showBanner = false;
        }
      } catch (e) {
        // Prompt failed, show manual instructions
        this.showInstructions = true;
      }
      this.deferredPrompt = null;
      this.cdr.detectChanges();
    } else {
      // Show platform-specific instructions
      this.showInstructions = true;
      this.showBanner = false;
      this.cdr.detectChanges();
    }
  }

  dismiss() {
    this.showBanner = false;
    this.showInstructions = false;
    localStorage.setItem('pwa-install-dismissed', Date.now().toString());
    this.cdr.detectChanges();
  }
}
