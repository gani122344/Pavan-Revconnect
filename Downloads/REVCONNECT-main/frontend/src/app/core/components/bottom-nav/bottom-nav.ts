import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { UserService, UserResponse } from '../../services/user.service';

@Component({
  selector: 'app-bottom-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="bottom-nav">
      <a routerLink="/feed" routerLinkActive="active" class="nav-item">
        <i class="fa-solid fa-house"></i>
        <span>Home</span>
      </a>
      <a routerLink="/explore" routerLinkActive="active" class="nav-item">
        <i class="fa-solid fa-magnifying-glass"></i>
        <span>Explore</span>
      </a>
      <a routerLink="/notifications" routerLinkActive="active" class="nav-item">
        <i class="fa-solid fa-bell"></i>
        <span>Alerts</span>
      </a>
      <a routerLink="/messages" routerLinkActive="active" class="nav-item">
        <i class="fa-solid fa-envelope"></i>
        <span>Chats</span>
      </a>
      <button class="nav-item more-btn" (click)="toggleMore()" [class.active]="moreOpen">
        <i class="fa-solid fa-bars"></i>
        <span>More</span>
      </button>
    </nav>

    <!-- More Menu Overlay -->
    <div class="more-overlay" *ngIf="moreOpen" (click)="moreOpen = false"></div>
    <div class="more-sheet" [class.open]="moreOpen">
      <div class="more-handle"></div>
      <a routerLink="/profile" class="more-item" (click)="moreOpen = false">
        <i class="fa-solid fa-user"></i><span>Profile</span>
      </a>
      <a routerLink="/bookmarks" class="more-item" (click)="moreOpen = false">
        <i class="fa-solid fa-bookmark"></i><span>Bookmarks</span>
      </a>
      <a routerLink="/wallet" class="more-item" (click)="moreOpen = false">
        <i class="fa-solid fa-wallet"></i><span>Wallet</span>
      </a>
      <a routerLink="/collaborations" class="more-item" *ngIf="showAnalytics" (click)="moreOpen = false">
        <i class="fa-solid fa-handshake"></i><span>Collaborations</span>
      </a>
      <a routerLink="/analytics" class="more-item" *ngIf="showAnalytics" (click)="moreOpen = false">
        <i class="fa-solid fa-chart-line"></i><span>Analytics</span>
      </a>
      <a routerLink="/settings" class="more-item" (click)="moreOpen = false">
        <i class="fa-solid fa-gear"></i><span>Settings</span>
      </a>
    </div>
  `,
  styles: [`
    .bottom-nav {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 64px;
      background: var(--bg-glass-heavy);
      backdrop-filter: blur(30px);
      -webkit-backdrop-filter: blur(30px);
      border-top: 1px solid var(--border-color);
      display: none;
      justify-content: space-around;
      align-items: center;
      z-index: 3000;
      padding-bottom: env(safe-area-inset-bottom);
      box-shadow: 0 -4px 20px rgba(0,0,0,0.08), 0 -1px 6px rgba(0,0,0,0.04);
    }

    :host-context(body:not(.light-theme)) .bottom-nav {
      box-shadow: 0 -10px 30px rgba(0,0,0,0.3);
    }

    .nav-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.7rem;
      font-weight: 700;
      transition: var(--transition-fast);
      flex: 1;
      padding: 8px 0;

      i { font-size: 1.3rem; }

      &.active {
        color: var(--accent-primary);
        transform: translateY(-4px);
        text-shadow: 0 0 10px var(--accent-glow);
      }
    }

    .more-btn {
      background: none;
      border: none;
      cursor: pointer;
    }

    /* Overlay */
    .more-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.4);
      z-index: 2999;
    }

    /* Slide-up Sheet */
    .more-sheet {
      position: fixed;
      bottom: 64px;
      left: 0;
      width: 100%;
      background: var(--bg-card, #1e293b);
      border-top: 1px solid var(--border-color, #334155);
      border-radius: 20px 20px 0 0;
      padding: 0.5rem 0 1rem;
      z-index: 3001;
      transform: translateY(110%);
      transition: transform 0.25s ease, visibility 0.25s;
      box-shadow: 0 -8px 30px rgba(0,0,0,0.3);
      visibility: hidden;
      pointer-events: none;
    }

    .more-sheet.open {
      transform: translateY(0);
      visibility: visible;
      pointer-events: auto;
    }

    .more-handle {
      width: 36px;
      height: 4px;
      border-radius: 2px;
      background: var(--text-muted, #64748b);
      margin: 0 auto 0.75rem;
      opacity: 0.4;
    }

    .more-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 0.85rem 1.5rem;
      color: var(--text-primary, #e2e8f0);
      text-decoration: none;
      font-size: 0.95rem;
      font-weight: 600;
      transition: background 0.15s;
    }

    .more-item:hover, .more-item:active {
      background: var(--bg-secondary, rgba(255,255,255,0.06));
    }

    .more-item i {
      width: 24px;
      text-align: center;
      font-size: 1.1rem;
      color: var(--text-secondary, #94a3b8);
    }

    @media (max-width: 820px) {
      .bottom-nav {
        display: flex;
      }
    }

    @media (min-width: 821px) {
      .more-overlay, .more-sheet {
        display: none !important;
      }
    }
  `]
})
export class BottomNav implements OnInit {
  moreOpen = false;
  showAnalytics = false;

  constructor(private router: Router, private userService: UserService) {}

  ngOnInit() {
    this.userService.getMyProfile().subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          this.showAnalytics = res.data.userType === 'CREATOR' || res.data.userType === 'BUSINESS';
        }
      }
    });
  }

  toggleMore() {
    this.moreOpen = !this.moreOpen;
  }
}
