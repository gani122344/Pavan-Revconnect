import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Navbar } from '../../../core/components/navbar/navbar';
import { Sidebar } from '../../../core/components/sidebar/sidebar';
import { NotificationService, NotificationResponse } from '../../../core/services/notification.service';
import { SearchService } from '../../../core/services/search.service';
import { UserService, UserResponse } from '../../../core/services/user.service';
import { ConnectionService } from '../../../core/services/connection.service';
import { RouterModule, Router } from '@angular/router';
import { BottomNav } from '../../../core/components/bottom-nav/bottom-nav';
import { getRelativeTime as sharedGetRelativeTime } from '../../../shared/utils/time.utils';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [CommonModule, Navbar, Sidebar, RouterModule, BottomNav],
  providers: [DatePipe],
  templateUrl: './notifications-page.html',
  styleUrls: ['./notifications-page.css']
})
export class NotificationsPage implements OnInit, OnDestroy {
  notifications: any[] = [];
  isLoading = false;
  page = 0;
  totalPages = 1;
  private timestampRefreshSub: any;

  constructor(
    private notificationService: NotificationService,
    private searchService: SearchService,
    private userService: UserService,
    private connectionService: ConnectionService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.loadNotifications();
    // Periodically trigger change detection so relative timestamps update
    this.timestampRefreshSub = setInterval(() => {
      this.cdr.detectChanges();
    }, 30000);
  }

  ngOnDestroy() {
    if (this.timestampRefreshSub) clearInterval(this.timestampRefreshSub);
  }

  viewProfile(userId: number) {
    this.router.navigate(['/profile', userId]);
  }

  loadNotifications() {
    this.isLoading = true;
    this.notificationService.getNotifications(this.page, 20).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.notifications = res.data.content.map((n: any) => ({
            ...n,
            isRead: n.isRead === true || n.read === true
          }));
          this.totalPages = res.data.totalPages;
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  markAsRead(notification: any) {
    if (notification.isRead) return;

    // Optimistic UI update
    notification.isRead = true;
    this.notifications = [...this.notifications];
    this.cdr.detectChanges();

    this.notificationService.markAsRead(notification.id).subscribe({
      error: () => {
        // Revert on error
        notification.isRead = false;
        this.notifications = [...this.notifications];
        this.cdr.detectChanges();
      }
    });
  }

  navigateNotification(notification: any) {
    // Mark as read first
    if (!notification.isRead) {
      notification.isRead = true;
      this.notifications = [...this.notifications];
      this.cdr.detectChanges();
      this.notificationService.markAsRead(notification.id).subscribe();
    }

    // Navigate based on type
    const type: string = notification.type || '';
    switch (type) {
      case 'LIKE':
      case 'COMMENT':
      case 'SHARE':
        this.router.navigate(['/feed']);
        break;
      case 'CONNECTION_REQUEST':
        // Navigate to own profile's requests tab
        this.router.navigate(['/profile'], { queryParams: { tab: 'requests' } });
        break;
      case 'FOLLOW':
      case 'NEW_FOLLOWER':
      case 'CONNECTION_ACCEPTED':
        if (notification.actorId) {
          this.router.navigate(['/profile', notification.actorId]);
        } else if (notification.senderId) {
          this.router.navigate(['/profile', notification.senderId]);
        }
        break;
      default:
        // No specific route, just mark read
        break;
    }
  }

  markAllAsRead() {
    this.notifications = this.notifications.map(n => ({ ...n, isRead: true }));
    this.cdr.detectChanges();

    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notificationService.refreshUnreadCount();
      }
    });
  }

  deleteNotification(notification: any, event: Event) {
    event.stopPropagation();

    this.notifications = this.notifications.filter(n => n.id !== notification.id);
    this.cdr.detectChanges();

    this.notificationService.deleteNotification(notification.id).subscribe({
      error: () => {
        this.loadNotifications();
      }
    });
  }

  getIconForType(type: string): string {
    switch (type) {
      case 'LIKE': return 'fa-solid fa-heart text-danger';
      case 'COMMENT': return 'fa-solid fa-comment text-primary';
      case 'FOLLOW':
      case 'NEW_FOLLOWER': return 'fa-solid fa-user-plus text-success';
      case 'SHARE': return 'fa-solid fa-share text-warning';
      case 'CONNECTION_REQUEST': return 'fa-solid fa-user-clock text-warning';
      case 'CONNECTION_ACCEPTED': return 'fa-solid fa-check-circle text-success';
      case 'BRAND_UPDATE': return 'fa-solid fa-bullhorn text-info';
      default: return 'fa-solid fa-bell text-info';
    }
  }

  getRelativeTime(value: any): string {
    return sharedGetRelativeTime(value);
  }
}
