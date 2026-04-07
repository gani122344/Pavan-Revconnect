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
  isSpeakerOn: boolean;
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
  private remoteAudioEl: HTMLAudioElement | null = null;
  private ringtoneCtx: AudioContext | null = null;
  private ringtoneOsc: OscillatorNode | null = null;
  private ringtoneGain: GainNode | null = null;
  private ringtoneInterval: any = null;

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
    { urls: 'turn:a.relay.metered.ca:80', username: 'e8dd65e92f6b4ae04a6ab508', credential: 'uWdWNmkhvyqTEswO' },
    { urls: 'turn:a.relay.metered.ca:80?transport=tcp', username: 'e8dd65e92f6b4ae04a6ab508', credential: 'uWdWNmkhvyqTEswO' },
    { urls: 'turn:a.relay.metered.ca:443', username: 'e8dd65e92f6b4ae04a6ab508', credential: 'uWdWNmkhvyqTEswO' },
    { urls: 'turns:a.relay.metered.ca:443?transport=tcp', username: 'e8dd65e92f6b4ae04a6ab508', credential: 'uWdWNmkhvyqTEswO' },
  ];

  constructor(private http: HttpClient, private zone: NgZone) {}

  // ─── Polling ───
  startPolling(): void {
    if (this.pollInterval) return;
    console.log('[Call] Signal polling started');
    this.pollInterval = setInterval(() => {
      this.http.get<ApiResponse<any[]>>(`${this.api}/signals`).subscribe({
        next: (res) => {
          if (res.success && res.data && res.data.length > 0) {
            console.log('[Call] Received signals:', res.data.map((s: any) => s.type));
            this.zone.run(() => {
              for (const sig of res.data) this.handleSignal(sig);
            });
          }
        },
        error: (err) => {
          console.warn('[Call] Signal poll failed:', err.status, err.message);
        }
      });
    }, 800);
  }

  stopPolling(): void {
    if (this.pollInterval) { clearInterval(this.pollInterval); this.pollInterval = null; }
  }

  forcePoll(): void {
    this.http.get<ApiResponse<any[]>>(`${this.api}/signals`).subscribe({
      next: (res) => {
        if (res.success && res.data && res.data.length > 0) {
          console.log('[Call] forcePoll received signals:', res.data.map((s: any) => s.type));
          this.zone.run(() => {
            for (const sig of res.data) this.handleSignal(sig);
          });
        }
      },
      error: () => {}
    });
  }

  getCallState(): CallState | null {
    return this.callStateSubject.getValue();
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
    console.log('[Call] initiateCall — callType:', callType, ', recipientId:', recipientId);
    const cur = this.callStateSubject.getValue();
    if (cur?.active) return;

    this.callStateSubject.next({
      active: true, callId: '', callType,
      direction: 'outgoing', status: 'ringing',
      remoteUserId: recipientId, remoteName: name,
      remoteUsername: username, remotePic: pic,
      isMuted: false, isCameraOff: callType === 'audio', isSpeakerOn: true,
    });

    // Get media ready while ringing
    await this.setupMedia(callType);

    this.startRingtone('outgoing');

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

    this.stopRingtone();
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
    this.stopRingtone();
    this.http.post<ApiResponse<any>>(`${this.api}/${state.callId}/reject`, {}).subscribe();
    this.cleanup();
  }

  endCall(): void {
    const state = this.callStateSubject.getValue();
    if (!state) return;
    if (state.callId) {
      this.http.post<ApiResponse<any>>(`${this.api}/${state.callId}/end`, {}).subscribe();
    }
    // Also send end signal directly so the other side gets it quickly
    if (state.remoteUserId) {
      this.sendSignal({ type: 'call-ended', recipientId: state.remoteUserId, callId: state.callId });
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

  toggleSpeaker(): void {
    const state = this.callStateSubject.getValue();
    if (!state) return;
    state.isSpeakerOn = !state.isSpeakerOn;
    if (this.remoteAudioEl) {
      this.remoteAudioEl.volume = state.isSpeakerOn ? 1.0 : 0.0;
    }
    this.callStateSubject.next({ ...state });
  }

  private attachRemoteAudio(stream: MediaStream): void {
    if (!this.remoteAudioEl) {
      this.remoteAudioEl = new Audio();
      this.remoteAudioEl.autoplay = true;
    }
    this.remoteAudioEl.srcObject = stream;
    this.remoteAudioEl.play().catch(err => console.warn('Remote audio play failed:', err));
  }

  // ─── Video Bitrate ───
  private async setVideoBitrate(pc: RTCPeerConnection, maxBitrate: number): Promise<void> {
    try {
      const senders = pc.getSenders();
      for (const sender of senders) {
        if (sender.track?.kind === 'video') {
          const params = sender.getParameters();
          if (!params.encodings || params.encodings.length === 0) {
            params.encodings = [{}];
          }
          params.encodings[0].maxBitrate = maxBitrate;
          params.encodings[0].scaleResolutionDownBy = 1.0;
          await sender.setParameters(params);
          console.log('[Call] Video bitrate set to', maxBitrate / 1000, 'kbps');
        }
      }
    } catch (err) {
      console.warn('[Call] Failed to set video bitrate:', err);
    }
  }

  // ─── Media Setup ───
  private async setupMedia(callType: 'audio' | 'video'): Promise<void> {
    console.log('[Call] setupMedia called with callType:', callType);
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: callType === 'video' ? {
          width: { ideal: 1920, min: 1280 },
          height: { ideal: 1080, min: 720 },
          frameRate: { ideal: 30, min: 24 },
          facingMode: 'user'
        } : false,
      });
      console.log('[Call] getUserMedia success — video tracks:', this.localStream.getVideoTracks().length, ', audio tracks:', this.localStream.getAudioTracks().length);
      this.localStreamSubject.next(this.localStream);
    } catch (err) {
      console.error('[Call] getUserMedia failed:', err);
      // If video fails, fall back to audio-only but keep callType as video for UI
      if (callType === 'video') {
        console.warn('[Call] Falling back to audio-only stream');
        try {
          this.localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
          this.localStreamSubject.next(this.localStream);
          return;
        } catch (err2) {
          console.error('[Call] Audio-only fallback also failed:', err2);
        }
      }
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
        console.log('[Call] ontrack — remote video tracks:', this.remoteStream.getVideoTracks().length, ', audio tracks:', this.remoteStream.getAudioTracks().length);
        // For audio calls, use hidden audio element; for video calls, the <video> element handles audio
        const curState = this.callStateSubject.getValue();
        if (curState?.callType !== 'video') {
          this.attachRemoteAudio(this.remoteStream);
        }
        const s = this.callStateSubject.getValue();
        if (s && s.status !== 'connected') {
          s.status = 'connected';
          s.startTime = Date.now();
          this.callStateSubject.next({ ...s });
          this.startDurationTimer();
        }
      });
    };

    pc.oniceconnectionstatechange = () => {
      console.log('[Call] ICE connection state:', pc.iceConnectionState);
      if (pc.iceConnectionState === 'connected' || pc.iceConnectionState === 'completed') {
        this.zone.run(() => {
          const s = this.callStateSubject.getValue();
          if (s && s.status === 'connecting') {
            s.status = 'connected';
            s.startTime = s.startTime || Date.now();
            this.callStateSubject.next({ ...s });
            this.startDurationTimer();
          }
        });
        // Boost video bitrate for better quality
        this.setVideoBitrate(pc, 4000000);
      }
    };

    pc.onconnectionstatechange = () => {
      console.log('[Call] Connection state:', pc.connectionState);
      if (pc.connectionState === 'disconnected' || pc.connectionState === 'failed' || pc.connectionState === 'closed') {
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
    console.log('[Call] onIncomingCall — sig.callType:', sig.callType, ', callId:', sig.callId);
    this.callStateSubject.next({
      active: true, callId: sig.callId,
      callType: sig.callType || 'audio',
      direction: 'incoming', status: 'ringing',
      remoteUserId: sig.callerId,
      remoteName: sig.callerName || 'User',
      remoteUsername: sig.callerUsername || '',
      remotePic: sig.callerPic || '',
      isMuted: false, isCameraOff: sig.callType === 'audio', isSpeakerOn: true,
    });
    this.startRingtone('incoming');
  }

  private async onCallAccepted(sig: any): Promise<void> {
    const state = this.callStateSubject.getValue();
    if (!state) return;
    // Fix race condition: if our callId hasn't been set yet from HTTP response, accept by direction
    if (state.callId && state.callId !== sig.callId) return;
    if (!state.callId && sig.callId) {
      state.callId = sig.callId;
    }
    console.log('[Call] onCallAccepted — callType:', state.callType, ', callId:', state.callId);
    this.stopRingtone();
    if (this.ringTimeout) { clearTimeout(this.ringTimeout); this.ringTimeout = null; }

    state.status = 'connecting';
    this.callStateSubject.next({ ...state });

    // Caller: now create PC and send offer
    const pc = this.buildPC(state.remoteUserId);
    const offer = await pc.createOffer();
    offer.sdp = this.boostSdpBandwidth(offer.sdp || '');
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
    this.stopRingtone();
    if (this.ringTimeout) { clearTimeout(this.ringTimeout); this.ringTimeout = null; }
    if (this.durationInterval) { clearInterval(this.durationInterval); this.durationInterval = null; }
    if (this.localStream) {
      this.localStream.getTracks().forEach(t => t.stop());
      this.localStream = null;
      this.localStreamSubject.next(null);
    }
    if (this.pc) { try { this.pc.close(); } catch {} this.pc = null; }
    if (this.remoteAudioEl) {
      this.remoteAudioEl.srcObject = null;
      this.remoteAudioEl = null;
    }
    this.remoteStream = null;
    this.remoteStreamSubject.next(null);
    this.pendingIceCandidates = [];
    this.callStateSubject.next(null);
  }

  // ─── Ringtone ───
  private startRingtone(type: 'incoming' | 'outgoing'): void {
    this.stopRingtone();
    try {
      this.ringtoneCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      this.ringtoneGain = this.ringtoneCtx.createGain();
      this.ringtoneGain.connect(this.ringtoneCtx.destination);
      this.ringtoneGain.gain.value = 0;

      const playTone = () => {
        if (!this.ringtoneCtx || !this.ringtoneGain) return;
        const now = this.ringtoneCtx.currentTime;
        // Incoming: classic phone ring (440+480 Hz dual-tone)
        // Outgoing: ringback tone (440+480 Hz, different cadence)
        const osc1 = this.ringtoneCtx.createOscillator();
        const osc2 = this.ringtoneCtx.createOscillator();
        const gain = this.ringtoneCtx.createGain();
        osc1.frequency.value = 440;
        osc2.frequency.value = 480;
        osc1.connect(gain);
        osc2.connect(gain);
        gain.connect(this.ringtoneCtx.destination);

        if (type === 'incoming') {
          // Ring: 1s on, 0.2s off, 1s on — then 3s silence
          gain.gain.setValueAtTime(0.3, now);
          gain.gain.setValueAtTime(0.3, now + 1.0);
          gain.gain.setValueAtTime(0, now + 1.0);
          gain.gain.setValueAtTime(0.3, now + 1.2);
          gain.gain.setValueAtTime(0, now + 2.2);
          osc1.start(now);
          osc2.start(now);
          osc1.stop(now + 2.2);
          osc2.stop(now + 2.2);
        } else {
          // Ringback: 2s on, 4s off
          gain.gain.setValueAtTime(0.15, now);
          gain.gain.setValueAtTime(0, now + 2.0);
          osc1.start(now);
          osc2.start(now);
          osc1.stop(now + 2.0);
          osc2.stop(now + 2.0);
        }
      };

      playTone();
      const interval = type === 'incoming' ? 4000 : 6000;
      this.ringtoneInterval = setInterval(playTone, interval);
    } catch (e) {
      console.warn('[Call] Ringtone failed:', e);
    }
  }

  private stopRingtone(): void {
    if (this.ringtoneInterval) { clearInterval(this.ringtoneInterval); this.ringtoneInterval = null; }
    if (this.ringtoneCtx) {
      try { this.ringtoneCtx.close(); } catch {}
      this.ringtoneCtx = null;
    }
    this.ringtoneOsc = null;
    this.ringtoneGain = null;
  }

  // ─── SDP Bandwidth Boost ───
  private boostSdpBandwidth(sdp: string): string {
    // Set high bandwidth for video in SDP
    let lines = sdp.split('\r\n');
    const result: string[] = [];
    for (let i = 0; i < lines.length; i++) {
      result.push(lines[i]);
      if (lines[i].startsWith('m=video')) {
        // Remove any existing b= line
        if (i + 1 < lines.length && lines[i + 1].startsWith('b=')) {
          i++; // skip old b= line
        }
        result.push('b=AS:4000');
      } else if (lines[i].startsWith('m=audio')) {
        if (i + 1 < lines.length && lines[i + 1].startsWith('b=')) {
          i++;
        }
        result.push('b=AS:128');
      }
    }
    return result.join('\r\n');
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }
}
