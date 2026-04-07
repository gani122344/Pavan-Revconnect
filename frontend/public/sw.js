const CACHE_NAME = 'revconnect-v2';
const STATIC_ASSETS = [
  '/',
  '/manifest.webmanifest',
  '/icons/icon-192x192.png',
  '/icons/icon-512x512.png'
];

// Install: cache shell assets
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

// Activate: clean old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// Push: handle incoming push notifications (calls, messages, etc.)
self.addEventListener('push', event => {
  if (!event.data) return;

  let data;
  try {
    data = event.data.json();
  } catch (e) {
    data = { type: 'generic', title: 'RevConnect', body: event.data.text() };
  }

  let title = 'RevConnect';
  let options = {
    icon: '/icons/icon-192x192.png',
    badge: '/icons/icon-96x96.png',
    vibrate: [300, 100, 300, 100, 300],
    requireInteraction: true,
    tag: 'revconnect-notification',
    data: data
  };

  if (data.type === 'incoming-call') {
    const callType = data.callType === 'video' ? 'Video' : 'Audio';
    title = `Incoming ${callType} Call`;
    options.body = `${data.callerName || 'Someone'} is calling you`;
    options.tag = 'incoming-call-' + data.callId;
    options.renotify = true;
    options.silent = false;
    // Long vibration pattern to simulate ringtone (ring for ~30 seconds)
    options.vibrate = [
      500, 200, 500, 200, 500, 1000,
      500, 200, 500, 200, 500, 1000,
      500, 200, 500, 200, 500, 1000,
      500, 200, 500, 200, 500, 1000,
      500, 200, 500, 200, 500, 1000
    ];
    options.actions = [
      { action: 'answer', title: '📞 Answer' },
      { action: 'decline', title: '❌ Decline' }
    ];
    if (data.callerPic) {
      options.image = data.callerPic;
    }

    // Forward incoming call to any open client tabs
    event.waitUntil(
      clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
        for (const client of clientList) {
          client.postMessage({
            type: 'incoming-call-push',
            callId: data.callId,
            callType: data.callType,
            callerName: data.callerName,
            callerPic: data.callerPic
          });
        }
        return self.registration.showNotification(title, options);
      })
    );
    return;
  } else if (data.type === 'message') {
    title = data.senderName || 'New Message';
    options.body = data.content || 'You have a new message';
    options.tag = 'message-' + (data.senderId || Date.now());
  }

  event.waitUntil(self.registration.showNotification(title, options));
});

// Notification click: open or focus the app
self.addEventListener('notificationclick', event => {
  const data = event.notification.data || {};
  const action = event.action;

  event.notification.close();

  // Handle call notification actions
  if (data.type === 'incoming-call') {
    if (action === 'decline') {
      // Decline: try to call reject API, then close notification
      event.waitUntil(
        fetch(self.location.origin + '/api/calls/' + data.callId + '/reject', {
          method: 'POST',
          headers: { 'Authorization': 'Bearer ' + (data.token || '') }
        }).catch(() => {})
      );
      return;
    }

    // Answer or just tap notification: open app with call params to auto-accept
    const callUrl = '/messages?incomingCall=' + data.callId + '&callType=' + (data.callType || 'audio') + '&callerName=' + encodeURIComponent(data.callerName || '');
    event.waitUntil(
      clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
        // If app is already open, post message and focus
        for (const client of clientList) {
          if (client.url.includes(self.location.origin)) {
            client.postMessage({
              type: action === 'answer' ? 'answer-call-push' : 'open-call-push',
              callId: data.callId,
              callType: data.callType,
              callerName: data.callerName,
              callerPic: data.callerPic
            });
            client.focus();
            return;
          }
        }
        // Otherwise open new window
        return clients.openWindow(self.location.origin + callUrl);
      })
    );
    return;
  }

  // Default: open the app
  let url = '/';
  if (data.type === 'message') {
    url = '/messages?userId=' + (data.senderId || '');
  }

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
      for (const client of clientList) {
        if (client.url.includes(self.location.origin)) {
          client.focus();
          client.navigate(self.location.origin + url);
          return;
        }
      }
      return clients.openWindow(self.location.origin + url);
    })
  );
});

// Fetch: network-first for API, cache-first for static
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);

  // Skip non-GET and API/WebSocket requests
  if (event.request.method !== 'GET' || url.pathname.startsWith('/api') || url.pathname.startsWith('/ws')) {
    return;
  }

  event.respondWith(
    fetch(event.request)
      .then(response => {
        // Cache successful responses for static assets
        if (response.ok && (url.origin === self.location.origin)) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
        }
        return response;
      })
      .catch(() => caches.match(event.request).then(cached => cached || caches.match('/')))
  );
});
