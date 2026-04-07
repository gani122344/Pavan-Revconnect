import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Navbar } from '../../../core/components/navbar/navbar';
import { Sidebar } from '../../../core/components/sidebar/sidebar';
import { CollaborationService, CollaborationResponse, PostPromotionResponse } from '../../../core/services/collaboration.service';
import { UserService, UserResponse } from '../../../core/services/user.service';

@Component({
  selector: 'app-collaborations-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Navbar, Sidebar],
  templateUrl: './collaborations-page.html',
  styleUrls: ['./collaborations-page.css']
})
export class CollaborationsPage implements OnInit {
  currentUser: UserResponse | null = null;
  isBusiness = false;
  isCreator = false;

  // Tabs
  activeTab: 'all' | 'pending' | 'active' | 'rejected' | 'revoked' = 'all';

  // Collaborations list
  collaborations: CollaborationResponse[] = [];
  isLoading = false;

  // Invite modal
  showInviteModal = false;
  searchQuery = '';
  searchResults: UserResponse[] = [];
  isSearching = false;
  selectedCreator: UserResponse | null = null;

  // Invite form
  inviteStep = 1; // 1=select creator, 2=brand info, 3=payment, 4=rules
  inviteMessage = '';
  promotionRules = 'Creator must follow brand content guidelines and disclose the partnership.';
  contentGuidelines = '';
  brandName = '';
  brandWebsite = '';
  fixedFee = 0;
  ratePerView = 2;
  viewMilestone = 10;
  paymentSchedule = 'MONTHLY';
  durationDays = 30;
  isInviting = false;

  // Contract modal (creator view)
  showContractModal = false;
  selectedCollaboration: CollaborationResponse | null = null;
  contractAgreed = false;
  isAccepting = false;

  // Grant promotion modal
  showGrantModal = false;
  grantPostId: number | null = null;
  grantCreatorId: number | null = null;
  grantCollabId: number | null = null;
  grantCtaLabel = 'Visit Website';
  grantCtaUrl = '';
  isGranting = false;
  activeCollaborators: CollaborationResponse[] = [];

  // Showcase items for active collaborations
  showcaseItems: Map<number, any[]> = new Map();
  showcaseLoading: Set<number> = new Set();

  // Creator: promote showcase item
  isPromoting = false;

  constructor(
    private collabService: CollaborationService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.isLoading = true;
    this.userService.getMyProfile().subscribe({
      next: (res: any) => {
        if (res?.success && res?.data) {
          this.currentUser = res.data;
          this.isBusiness = res.data.userType === 'BUSINESS';
          this.isCreator = res.data.userType === 'CREATOR';
          this.loadCollaborations();
        } else {
          this.isLoading = false;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadCollaborations() {
    this.isLoading = true;
    this.collaborations = [];
    const status = this.activeTab === 'all' ? undefined : this.activeTab.toUpperCase();
    this.collabService.getMyCollaborations(status).subscribe({
      next: (res: any) => {
        try {
          this.collaborations = res?.data?.content || res?.data || [];
          if (!Array.isArray(this.collaborations)) this.collaborations = [];
        } catch {
          this.collaborations = [];
        }
        this.isLoading = false;
      },
      error: () => {
        this.collaborations = [];
        this.isLoading = false;
      }
    });
  }

  switchTab(tab: 'all' | 'pending' | 'active' | 'rejected' | 'revoked') {
    this.activeTab = tab;
    this.loadCollaborations();
  }

  // ═══ Business: Search creators to invite ═══
  searchCreators() {
    if (!this.searchQuery.trim()) { this.searchResults = []; return; }
    this.isSearching = true;
    this.userService.searchUsers(this.searchQuery, 0, 10).subscribe({
      next: (res: any) => {
        if (res.success && res.data) {
          const list = res.data.content || res.data || [];
          this.searchResults = list.filter((u: any) => u.userType === 'CREATOR');
        }
        this.isSearching = false;
      },
      error: () => { this.isSearching = false; }
    });
  }

  selectCreatorForInvite(creator: UserResponse) {
    this.selectedCreator = creator;
    this.searchResults = [];
    this.searchQuery = '';
  }

  sendInvite() {
    if (!this.selectedCreator) return;
    this.isInviting = true;
    this.collabService.inviteCreator({
      creatorId: this.selectedCreator.id,
      message: this.inviteMessage || undefined,
      promotionRules: this.promotionRules,
      contentGuidelines: this.contentGuidelines || undefined,
      fixedFee: this.fixedFee,
      ratePerView: this.ratePerView,
      viewMilestone: this.viewMilestone,
      paymentSchedule: this.paymentSchedule,
      brandName: this.brandName || undefined,
      brandWebsite: this.brandWebsite || undefined,
      durationDays: this.durationDays
    }).subscribe({
      next: () => {
        this.isInviting = false;
        this.closeInviteModal();
        this.loadCollaborations();
      },
      error: (err: any) => {
        this.isInviting = false;
        alert(err.error?.message || 'Failed to send invite');
      }
    });
  }

  nextInviteStep() {
    if (this.inviteStep < 4) this.inviteStep++;
  }

  prevInviteStep() {
    if (this.inviteStep > 1) this.inviteStep--;
  }

  getPerViewRate(): string {
    if (!this.viewMilestone || this.viewMilestone === 0) return '0.00';
    return (this.ratePerView / this.viewMilestone).toFixed(2);
  }

  openInviteModal() {
    this.showInviteModal = true;
    this.inviteStep = 1;
    this.selectedCreator = null;
    this.inviteMessage = '';
    this.searchQuery = '';
    this.searchResults = [];
    this.brandName = '';
    this.brandWebsite = '';
    this.fixedFee = 0;
    this.ratePerView = 2;
    this.viewMilestone = 10;
    this.paymentSchedule = 'MONTHLY';
    this.durationDays = 30;
    this.promotionRules = 'Creator must follow brand content guidelines and disclose the partnership.';
    this.contentGuidelines = '';
  }

  closeInviteModal() {
    this.showInviteModal = false;
    this.selectedCreator = null;
  }

  // ═══ Creator: View contract ═══
  viewContract(collab: CollaborationResponse) {
    this.selectedCollaboration = collab;
    this.contractAgreed = false;
    this.showContractModal = true;
  }

  downloadContractPdf(collab: CollaborationResponse) {
    this.collabService.downloadContractPdf(collab.id);
  }

  acceptContract() {
    if (!this.selectedCollaboration || !this.contractAgreed) return;
    this.isAccepting = true;
    this.collabService.acceptCollaboration(this.selectedCollaboration.id).subscribe({
      next: () => {
        this.isAccepting = false;
        this.showContractModal = false;
        this.loadCollaborations();
      },
      error: (err: any) => {
        this.isAccepting = false;
        alert(err.error?.message || 'Failed to accept');
      }
    });
  }

  rejectInvite(collab: CollaborationResponse) {
    if (!confirm('Are you sure you want to decline this collaboration?')) return;
    this.collabService.rejectCollaboration(collab.id).subscribe({
      next: () => this.loadCollaborations()
    });
  }

  revokeCollab(collab: CollaborationResponse) {
    if (!confirm('Revoke this collaboration? All active promotions will be disabled.')) return;
    this.collabService.revokeCollaboration(collab.id).subscribe({
      next: () => this.loadCollaborations()
    });
  }

  // ═══ Load showcase items for a business ═══
  loadShowcase(userId: number, collabId: number) {
    if (this.showcaseItems.has(collabId) || this.showcaseLoading.has(collabId)) return;
    this.showcaseLoading.add(collabId);
    this.userService.getShowcaseByUserId(userId).subscribe({
      next: (res: any) => {
        const items = res?.data || res || [];
        this.showcaseItems.set(collabId, Array.isArray(items) ? items : []);
        this.showcaseLoading.delete(collabId);
      },
      error: () => {
        this.showcaseItems.set(collabId, []);
        this.showcaseLoading.delete(collabId);
      }
    });
  }

  getShowcase(collabId: number): any[] {
    return this.showcaseItems.get(collabId) || [];
  }

  // ═══ Creator: Quick promote a showcase item ═══
  quickPromote(collab: CollaborationResponse, item: any) {
    if (this.isPromoting) return;
    this.isPromoting = true;
    this.collabService.grantPromotion({
      postId: 0,
      creatorId: collab.creatorId,
      ctaLabel: item.name || 'Shop Now',
      ctaUrl: item.link || ''
    }).subscribe({
      next: () => {
        this.isPromoting = false;
        alert('Promotion shared! The business post will show your partnership.');
      },
      error: (err: any) => {
        this.isPromoting = false;
        alert(err.error?.message || 'Failed to promote');
      }
    });
  }

  // ═══ Business: Grant promotion modal ═══
  openGrantModal(collab: CollaborationResponse) {
    this.showGrantModal = true;
    this.grantCreatorId = collab.creatorId;
    this.grantCollabId = collab.id;
    this.grantPostId = null;
    this.grantCtaLabel = 'Visit Website';
    this.grantCtaUrl = '';
  }

  grantPromotion() {
    if (!this.grantPostId || !this.grantCreatorId) return;
    this.isGranting = true;
    this.collabService.grantPromotion({
      postId: this.grantPostId,
      creatorId: this.grantCreatorId,
      ctaLabel: this.grantCtaLabel || undefined,
      ctaUrl: this.grantCtaUrl || undefined
    }).subscribe({
      next: () => {
        this.isGranting = false;
        this.showGrantModal = false;
        alert('Promotion granted successfully!');
      },
      error: (err: any) => {
        this.isGranting = false;
        alert(err.error?.message || 'Failed to grant promotion');
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return '#22c55e';
      case 'PENDING': return '#eab308';
      case 'REJECTED': return '#ef4444';
      case 'REVOKED': return '#6b7280';
      case 'EXPIRED': return '#9ca3af';
      default: return '#64748b';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'fa-check-circle';
      case 'PENDING': return 'fa-clock';
      case 'REJECTED': return 'fa-times-circle';
      case 'REVOKED': return 'fa-ban';
      case 'EXPIRED': return 'fa-calendar-xmark';
      default: return 'fa-circle-question';
    }
  }

  getAvatar(pic: string | undefined, name: string): string {
    return pic || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&size=64`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
  }
}
