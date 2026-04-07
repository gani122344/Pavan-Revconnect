import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './landing-page.html',
  styleUrls: ['./landing-page.css']
})
export class LandingPage implements OnInit, OnDestroy {
  mobileMenuOpen = false;
  private deferredPrompt: any = null;
  private promptHandler: any;

  constructor(private router: Router) {}

  ngOnInit(): void {
    const token = localStorage.getItem('revconnect_token');
    if (token) {
      this.router.navigate(['/feed']);
    }

    // Capture PWA install prompt
    this.promptHandler = (e: any) => {
      e.preventDefault();
      this.deferredPrompt = e;
    };
    window.addEventListener('beforeinstallprompt', this.promptHandler);
  }

  ngOnDestroy(): void {
    if (this.promptHandler) {
      window.removeEventListener('beforeinstallprompt', this.promptHandler);
    }
  }

  getStarted() {
    this.router.navigate(['/register']);
  }

  scrollToSection(sectionId: string) {
    this.mobileMenuOpen = false;
    const el = document.getElementById(sectionId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  scrollToDownload() {
    this.scrollToSection('download');
  }

  async installPWA() {
    if (this.deferredPrompt) {
      // Chrome/Edge native install prompt available
      try {
        this.deferredPrompt.prompt();
        const result = await this.deferredPrompt.userChoice;
        if (result.outcome === 'accepted') {
          this.deferredPrompt = null;
        }
      } catch (e) {
        // If prompt fails, scroll to download section for manual instructions
        this.scrollToDownload();
      }
      this.deferredPrompt = null;
    } else {
      // No native prompt — scroll to download section with manual instructions
      this.scrollToDownload();
    }
  }
}
