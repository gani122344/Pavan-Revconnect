import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { ApiResponse } from './auth.service';

export interface CallState {
  active: boolean;
  callId: string;
  callType: 'audio' | 'video';
  direction: 'outgoing' | 'incoming';
  status: 'ringing' | 'connecting' | 'connected' | 'ended';
  remoteUserId: number;
  remoteName: string;
  remoteUsername: string;
  remotePic: string;
  startTime?: number;
  duration?: number;
  isMuted: boolean;
  isCameraOff: boolean;
}

@Injectable({ providedIn: 'root' })
export class CallService {
  private api = '/api/calls';
  private pc: RTCPeerConnection | null = null;
  private localStream: MediaStream | null = null;
  private remoteStream: MediaStream | null = null;
  private pollInterval: any = null;
  private durationInterval: any = null;
  private ringTimeout: any = null;
  private pendingIceCandidates: RTCIceCandidateInit[] = [];

  private callStateSubject = new BehaviorSubject<CallState | null>(null);
  callState$ = this.callStateSubject.asObservable();

  private localStreamSubject = new BehaviorSubject<MediaStream | null>(null);
  localStream$ = this.localStreamSubject.asObservable();

  private remoteStreamSubject = new BehaviorSubject<MediaStream | null>(null);
  remoteStream$ = this.remoteStreamSubject.asObservable();

  private readonly ICE_SERVERS: RTCIceServer[] = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' },
  ];

  constructor(private http: HttpClient, private zone: NgZone) {}

  // ─── Polling ───
  startPolling(): void {
    if (this.pollInterval) return;
    this.pollInterval = setInterval(() => {
      this.http.get<ApiResponse<any[]>>(`${this.api}/signals`).subscribe({
        next: (res) => {
          if (res.success && res.data && res.data.length > 0) {
            this.zone.run(() => {
              for (const sig of res.data) this.handleSignal(sig);
            });
          }
        }
      });
    }, 800);
  }

  stopPolling(): void {
    if (this.pollInterval) { clearInterval(this.pollInterval); this.pollInterval = null; }
  }

  // ─── Signal Router ───
  private handleSignal(sig: any): void {
    switch (sig.type) {
      case 'incoming-call': this.onIncomingCall(sig); break;
      case 'call-accepted': this.onCallAccepted(sig); break;
      case 'call-rejected': this.onCallRejected(sig); break;
      case 'call-ended':    this.cleanup(); break;
      case 'offer':         this.onOffer(sig); break;
      case 'answer':        this.onAnswer(sig); break;
      case 'ice-candidate': this.onIceCandidate(sig); break;
    }
  }

  // ─── Initiate (Caller) ───
  async initiateCall(recipientId: number, name: string, username: string, pic: string, callType: 'audio' | 'video'): Promise<void> {
    const cur = this.callStateSubject.getValue();
    if (cur?.active) return;

    this.callStateSubject.next({
      active: true, callId: '', callType,
      direction: 'outgoing', status: 'ringing',
      remoteUserId: recipientId, remoteName: name,
      remoteUsername: username, remotePic: pic,
      isMuted: false, isCameraOff: callType === 'audio',
    });

    // Get media ready while ringing
    await this.setupMedia(callType);

    this.http.post<ApiResponse<any>>(`${this.api}/initiate?recipientId=${recipientId}&callType=${callType}`, {}).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const s = this.callStateSubject.getValue();
          if (s) { s.callId = res.data.callId; this.callStateSubject.next({ ...s }); }
        }
      },
      error: () => this.cleanup()
    });

    // Auto-cancel after 45s if no answer
    this.ringTimeout = setTimeout(() => {
      const s = this.callStateSubject.getValue();
      if (s?.status === 'ringing') this.endCall();
    }, 45000);
  }

  // ─── Accept (Callee) ───
  async acceptCall(): Promise<void> {
    const state = this.callStateSubject.getValue();
    if (!state || state.direction !== 'incoming') return;

    state.status = 'connecting';
    this.callStateSubject.next({ ...state });

    // Get media ready
    await this.setupMedia(state.callType);

    // Tell the backend (which notifies the caller)
    this.http.post<ApiResponse<any>>(`${this.api}/${state.callId}/accept`, {}).subscribe();

    // Callee does NOT create offer — waits for caller's offer via signal
  }

  rejectCall(): void {
    const state = this.callStateSubject.getValue();
    if (!state) return;
    this.http.post<ApiResponse<any>>(`${this.api}/${state.callId}/reject`, {}).subscribe();
    this.cleanup();
  }

  endCall(): void {
    const state = this.callStateSubject.getValue();
    if (!state) return;
    if (state.callId) {
      this.http.post<ApiResponse<any>>(`${this.api}/${state.callId}/end`, {}).subscribe();
    }
    this.cleanup();
  }

  toggleMute(): void {
    const state = this.callStateSubject.getValue();
    if (!state || !this.localStream) return;
    this.localStream.getAudioTracks().forEach(t => t.enabled = !t.enabled);
    state.isMuted = !state.isMuted;
    this.callStateSubject.next({ ...state });
  }

  toggleCamera(): void {
    const state = this.callStateSubject.getValue();
    if (!state || !this.localStream) return;
    this.localStream.getVideoTracks().forEach(t => t.enabled = !t.enabled);
    state.isCameraOff = !state.isCameraOff;
    this.callStateSubject.next({ ...state });
  }

  // ─── Media Setup ───
  private async setupMedia(callType: 'audio' | 'video'): Promise<void> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: callType === 'video' ? { width: 640, height: 480, facingMode: 'user' } : false,
      });
      this.localStreamSubject.next(this.localStream);
    } catch (err) {
      console.error('getUserMedia failed:', err);
      alert('Could not access microphone/camera. Please allow permissions.');
      this.cleanup();
    }
  }

  // ─── Peer Connection ───
  private buildPC(remoteUserId: number): RTCPeerConnection {
    if (this.pc) { try { this.pc.close(); } catch {} }
    this.pendingIceCandidates = [];

    const pc = new RTCPeerConnection({ iceServers: this.ICE_SERVERS });

    pc.onicecandidate = (ev) => {
      if (ev.candidate) {
        this.sendSignal({ type: 'ice-candidate', recipientId: remoteUserId, candidate: ev.candidate.toJSON() });
      }
    };

    pc.ontrack = (ev) => {
      this.zone.run(() => {
        this.remoteStream = ev.streams[0];
        this.remoteStreamSubject.next(this.remoteStream);
        const s = this.callStateSubject.getValue();
        if (s && s.status !== 'connected') {
          s.status = 'connected';
          s.startTime = Date.now();
          this.callStateSubject.next({ ...s });
          this.startDurationTimer();
        }
      });
    };

    pc.onconnectionstatechange = () => {
      if (pc.connectionState === 'disconnected' || pc.connectionState === 'failed') {
        this.zone.run(() => this.cleanup());
      }
    };

    // Add local tracks
    if (this.localStream) {
      this.localStream.getTracks().forEach(t => pc.addTrack(t, this.localStream!));
    }

    this.pc = pc;
    return pc;
  }

  // ─── Signal Handlers ───

  private onIncomingCall(sig: any): void {
    if (this.callStateSubject.getValue()?.active) return;
    this.callStateSubject.next({
      active: true, callId: sig.callId,
      callType: sig.callType || 'audio',
      direction: 'incoming', status: 'ringing',
      remoteUserId: sig.callerId,
      remoteName: sig.callerName || 'User',
      remoteUsername: sig.callerUsername || '',
      remotePic: sig.callerPic || '',
      isMuted: false, isCameraOff: sig.callType === 'audio',
    });
  }

  private async onCallAccepted(sig: any): Promise<void> {
    const state = this.callStateSubject.getValue();
    if (!state || state.callId !== sig.callId) return;
    if (this.ringTimeout) { clearTimeout(this.ringTimeout); this.ringTimeout = null; }

    state.status = 'connecting';
    this.callStateSubject.next({ ...state });

    // Caller: now create PC and send offer
    const pc = this.buildPC(state.remoteUserId);
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    this.sendSignal({
      type: 'offer', recipientId: state.remoteUserId,
      sdp: offer.sdp, sdpType: offer.type,
    });
  }

  private onCallRejected(sig: any): void {
    const state = this.callStateSubject.getValue();
    if (state && state.callId === sig.callId) this.cleanup();
  }

  private async onOffer(sig: any): Promise<void> {
    const state = this.callStateSubject.getValue();
    if (!state) return;

    // Callee: build PC, set remote desc, create answer
    const pc = this.buildPC(state.remoteUserId);
    await pc.setRemoteDescription(new RTCSessionDescription({ type: sig.sdpType, sdp: sig.sdp }));

    // Flush queued ICE candidates
    for (const c of this.pendingIceCandidates) {
      try { await pc.addIceCandidate(new RTCIceCandidate(c)); } catch {}
    }
    this.pendingIceCandidates = [];

    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    this.sendSignal({
      type: 'answer', recipientId: state.remoteUserId,
      sdp: answer.sdp, sdpType: answer.type,
    });
  }

  private async onAnswer(sig: any): Promise<void> {
    if (!this.pc) return;
    await this.pc.setRemoteDescription(new RTCSessionDescription({ type: sig.sdpType, sdp: sig.sdp }));

    // Flush queued ICE candidates
    for (const c of this.pendingIceCandidates) {
      try { await this.pc.addIceCandidate(new RTCIceCandidate(c)); } catch {}
    }
    this.pendingIceCandidates = [];
  }

  private async onIceCandidate(sig: any): Promise<void> {
    if (!this.pc || !this.pc.remoteDescription) {
      // Queue until remote description is set
      this.pendingIceCandidates.push(sig.candidate);
      return;
    }
    try {
      await this.pc.addIceCandidate(new RTCIceCandidate(sig.candidate));
    } catch (err) {
      console.error('ICE candidate error:', err);
    }
  }

  // ─── Helpers ───
  private sendSignal(signal: any): void {
    this.http.post<ApiResponse<any>>(`${this.api}/signal`, signal).subscribe();
  }

  private startDurationTimer(): void {
    if (this.durationInterval) clearInterval(this.durationInterval);
    this.durationInterval = setInterval(() => {
      const s = this.callStateSubject.getValue();
      if (s?.startTime) {
        s.duration = Math.floor((Date.now() - s.startTime) / 1000);
        this.callStateSubject.next({ ...s });
      }
    }, 1000);
  }

  private cleanup(): void {
    if (this.ringTimeout) { clearTimeout(this.ringTimeout); this.ringTimeout = null; }
    if (this.durationInterval) { clearInterval(this.durationInterval); this.durationInterval = null; }
    if (this.localStream) {
      this.localStream.getTracks().forEach(t => t.stop());
      this.localStream = null;
      this.localStreamSubject.next(null);
    }
    if (this.pc) { try { this.pc.close(); } catch {} this.pc = null; }
    this.remoteStream = null;
    this.remoteStreamSubject.next(null);
    this.pendingIceCandidates = [];
    this.callStateSubject.next(null);
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }
}
