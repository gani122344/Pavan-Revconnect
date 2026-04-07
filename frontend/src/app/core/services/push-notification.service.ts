import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class PushNotificationService {

  private vapidPublicKey: string | null = null;

  constructor(private http: HttpClient) {}

  async subscribeToPush(): Promise<void> {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      console.warn('[Push] Push notifications not supported in this browser');
      return;
    }

    try {
      // Request notification permission
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') {
        console.warn('[Push] Notification permission denied');
        return;
      }

      // Get VAPID public key from backend
      if (!this.vapidPublicKey) {
        const res: any = await this.http.get('/api/push/vapid-key').toPromise();
        if (res?.success && res?.data?.publicKey) {
          this.vapidPublicKey = res.data.publicKey;
        } else {
          console.error('[Push] Failed to get VAPID key');
          return;
        }
      }

      // Get service worker registration
      const registration = await navigator.serviceWorker.ready;

      // Check for existing subscription
      let subscription = await registration.pushManager.getSubscription();

      if (!subscription) {
        // Subscribe to push
        const applicationServerKey = this.urlBase64ToUint8Array(this.vapidPublicKey!);
        subscription = await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: applicationServerKey.buffer as ArrayBuffer
        });
        console.log('[Push] New push subscription created');
      } else {
        console.log('[Push] Using existing push subscription');
      }

      // Send subscription to backend
      const subJson = subscription.toJSON();
      await this.http.post('/api/push/subscribe', {
        endpoint: subJson.endpoint,
        keys: subJson.keys
      }).toPromise();

      console.log('[Push] Subscription sent to backend');
    } catch (err) {
      console.error('[Push] Failed to subscribe:', err);
    }
  }

  async unsubscribe(): Promise<void> {
    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      if (subscription) {
        const endpoint = subscription.endpoint;
        await subscription.unsubscribe();
        await this.http.post('/api/push/unsubscribe', { endpoint }).toPromise();
        console.log('[Push] Unsubscribed');
      }
    } catch (err) {
      console.error('[Push] Failed to unsubscribe:', err);
    }
  }

  private urlBase64ToUint8Array(base64String: string): Uint8Array {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  }
}
