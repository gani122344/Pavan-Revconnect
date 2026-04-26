import { Component, OnDestroy, NgZone, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, LoginRequest } from '../../../core/services/auth.service';

declare var google: any;

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnDestroy {
    // Step: 'classic' | 'phone' | 'otp' | 'success'
    step: 'classic' | 'phone' | 'otp' | 'success' = 'classic';

    credentials: LoginRequest = {
        usernameOrEmail: '',
        password: ''
    };
    showPassword = false;
    formSubmitted = false;

    phone = '';
    otpDigits: string[] = ['', '', '', '', '', ''];
    otpHiddenValue = '';
    @ViewChild('otpHidden') otpHidden?: ElementRef<HTMLInputElement>;
    private otpLastAt: number[] = [0, 0, 0, 0, 0, 0];
    private otpLastVal: string[] = ['', '', '', '', '', ''];
    tempToken = '';
    oauthProvider = '';
    oauthName = '';
    oauthEmail = '';

    isLoading = false;
    isSendingOtp = false;
    isVerifyingOtp = false;
    errorMessage = '';
    successMessage = '';

    resendCooldown = 0;
    private resendTimer: any;

    private googleClientId = '584394667522-pjdgd1f8pal32l7j6n6doa4lnajhemi7.apps.googleusercontent.com';

    constructor(
        private authService: AuthService,
        private router: Router,
        private ngZone: NgZone
    ) { }

    ngOnInit() {
        this.loadGoogleScript();
    }

    ngOnDestroy() {
        if (this.resendTimer) clearInterval(this.resendTimer);
    }

    focusOtpHidden() {
        this.otpHidden?.nativeElement?.focus();
    }

    onOtpHiddenInput(event: Event) {
        const input = event.target as HTMLInputElement;
        const digits = (input.value || '').replace(/\D/g, '').substring(0, 6);
        this.otpHiddenValue = digits;

        for (let i = 0; i < 6; i++) {
            this.otpDigits[i] = digits[i] || '';
        }

        if (!this.isSendingOtp && !this.isVerifyingOtp && digits.length === 6) {
            this.verifyOtp();
        }
    }

    onOtpHiddenKeydown(event: KeyboardEvent) {
        if (event.key === 'Enter') {
            event.preventDefault();
            if (!this.isSendingOtp && !this.isVerifyingOtp && this.isOtpComplete()) {
                this.verifyOtp();
            }
            return;
        }

        if (event.key === 'Backspace') {
            return;
        }

        // Allow digits, navigation keys
        if (/^\d$/.test(event.key) || event.key === 'ArrowLeft' || event.key === 'ArrowRight' || event.key === 'Tab') {
            return;
        }

        // Block everything else
        event.preventDefault();
    }

    // ─── GOOGLE ───
    private loadGoogleScript() {
        if (document.getElementById('google-gsi')) return;
        const script = document.createElement('script');
        script.id = 'google-gsi';
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        script.onload = () => this.initGoogle();
        document.head.appendChild(script);
    }

    private initGoogle() {
        if (typeof google === 'undefined') return;
        google.accounts.id.initialize({
            client_id: this.googleClientId,
            ux_mode: 'popup',
            callback: (response: any) => {
                this.ngZone.run(() => this.handleGoogleResponse(response));
            }
        });

        const container = document.getElementById('gsi-container');
        if (container) {
            container.innerHTML = '';
            google.accounts.id.renderButton(container, {
                type: 'standard',
                theme: 'outline',
                size: 'large',
                text: 'continue_with',
                width: 320
            });
        }
    }

    continueWithGoogle() {
        this.errorMessage = '';
        if (typeof google === 'undefined') {
            this.errorMessage = 'Google Sign-In is loading. Please try again.';
            return;
        }

        const container = document.getElementById('gsi-container');
        const clickable = container?.querySelector('div[role="button"], button, iframe') as HTMLElement | null;
        if (clickable) {
            try {
                clickable.click();
                return;
            } catch {
                // fall through
            }
        }

        google.accounts.id.prompt((notification: any) => {
            if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
                this.ngZone.run(() => {
                    this.errorMessage = 'Google Sign-In was blocked by the browser. Enable popups for accounts.google.com or use Phone login.';
                });
            }
        });
    }

    private handleGoogleResponse(response: any) {
        if (!response.credential) {
            this.errorMessage = 'Google Sign-In failed';
            return;
        }
        this.isLoading = true;
        this.isSendingOtp = false;
        this.isVerifyingOtp = false;
        this.errorMessage = '';

        this.authService.googleAuth({ idToken: response.credential }).subscribe({
            next: (res: any) => {
                if (res.success && res.data) {
                    if (!res.data.requiresPhone) {
                        // Already verified user — direct login
                        this.authService.storeToken(res.data.tempToken);
                        sessionStorage.setItem('revconnect_login_time', new Date().toISOString());
                        this.router.navigate(['/feed']);
                    } else {
                        this.tempToken = res.data.tempToken;
                        this.oauthProvider = 'Google';
                        this.oauthName = res.data.name || '';
                        this.oauthEmail = res.data.email || '';
                        this.step = 'phone';
                    }
                }
                this.isLoading = false;
            },
            error: (err: any) => {
                this.errorMessage = err.error?.message || 'Google authentication failed';
                this.isLoading = false;
            }
        });
    }

    // ─── CLASSIC LOGIN ───
    onLoginSubmit() {
        this.formSubmitted = true;
        this.errorMessage = '';

        if (!this.credentials.usernameOrEmail || !this.credentials.password) {
            return;
        }

        this.isLoading = true;

        this.authService.login(this.credentials).subscribe({
            next: (response) => {
                if (response.success && response.data?.accessToken) {
                    this.authService.storeToken(response.data.accessToken);
                    sessionStorage.setItem('revconnect_login_time', new Date().toISOString());
                    this.router.navigate(['/feed']);
                }
                this.isLoading = false;
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Login failed. Please verify credentials.';
                this.isLoading = false;
            }
        });
    }

    // ─── PHONE ───
    continueWithPhone() {
        this.oauthProvider = '';
        this.tempToken = '';
        this.step = 'phone';
    }

    isOtpComplete(): boolean {
        return this.otpDigits.join('').length === 6 && this.otpDigits.every(d => d !== '');
    }

    sendOtp() {
        if (!this.phone || this.phone.length < 10) {
            this.errorMessage = 'Please enter a valid phone number';
            return;
        }

        const formattedPhone = this.phone.startsWith('+') ? this.phone : '+91' + this.phone;
        this.phone = formattedPhone;

        this.isLoading = true;
        this.isSendingOtp = true;
        this.isVerifyingOtp = false;
        this.errorMessage = '';

        // Move to OTP screen immediately for better UX, while the API call is in-flight.
        this.step = 'otp';
        this.otpDigits = ['', '', '', '', '', ''];
        this.successMessage = 'Sending OTP to ' + formattedPhone + '...';

        this.authService.sendOtp({ phone: formattedPhone, tempToken: this.tempToken || undefined }).subscribe({
            next: () => {
                this.isLoading = false;
                this.isSendingOtp = false;
                this.successMessage = 'OTP sent to ' + formattedPhone;
                this.startResendCooldown();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to send OTP';
                this.isLoading = false;
                this.isSendingOtp = false;
                this.step = 'phone';
            }
        });
    }

    verifyOtp() {
        const otp = this.otpDigits.join('');
        if (otp.length !== 6) {
            this.errorMessage = 'Please enter the complete 6-digit OTP';
            return;
        }

        if (this.isVerifyingOtp) return;

        this.isLoading = true;
        this.isVerifyingOtp = true;
        this.isSendingOtp = false;
        this.errorMessage = '';

        this.authService.verifyOtp({
            phone: this.phone,
            otp: otp,
            tempToken: this.tempToken || undefined
        }).subscribe({
            next: (res: any) => {
                if (res.success && res.data?.accessToken) {
                    this.authService.storeToken(res.data.accessToken);
                    sessionStorage.setItem('revconnect_login_time', new Date().toISOString());
                    this.step = 'success';
                    setTimeout(() => this.router.navigate(['/feed']), 1000);
                }
                this.isLoading = false;
                this.isVerifyingOtp = false;
            },
            error: (err: any) => {
                this.errorMessage = err.error?.message || 'OTP verification failed';
                this.isLoading = false;
                this.isVerifyingOtp = false;
            }
        });
    }

    resendOtp() {
        if (this.resendCooldown > 0) return;
        this.otpDigits = ['', '', '', '', '', ''];
        this.sendOtpDirect();
    }

    private sendOtpDirect() {
        this.isLoading = true;
        this.isSendingOtp = true;
        this.isVerifyingOtp = false;
        this.errorMessage = '';

        this.authService.sendOtp({ phone: this.phone, tempToken: this.tempToken || undefined }).subscribe({
            next: () => {
                this.isLoading = false;
                this.isSendingOtp = false;
                this.successMessage = 'OTP resent successfully';
                this.startResendCooldown();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to resend OTP';
                this.isLoading = false;
                this.isSendingOtp = false;
            }
        });
    }

    // ─── OTP INPUT HANDLING ───
    onOtpInput(event: Event, index: number) {
        const input = event.target as HTMLInputElement;
        const inputType = (event as InputEvent).inputType || '';
        const raw = input.value || '';
        const digits = raw.replace(/\D/g, '');

        // Typing is handled in onOtpKeydown().
        // Keep this handler ONLY for full OTP autofill/paste (6 digits in a single box).
        // Any other single-digit input here can cause focus to skip (double-advance).
        if (inputType === 'insertText' || digits.length <= 1) {
            return;
        }

        if (!digits) {
            this.otpDigits[index] = '';
            return;
        }

        // Some keyboards / OTP auto-fill may insert multiple digits into a single box.
        // Spread them across the remaining inputs starting at the current index.
        if (digits.length > 1) {
            // We only want to "spread" when we receive a full OTP (paste / auto-fill).
            // Short multi-digit values like "44" are usually composition/focus glitches and must NOT
            // populate the next box.
            if (digits.length === 6) {
                for (let i = 0; i < digits.length && (index + i) < 6; i++) {
                    this.otpDigits[index + i] = digits[i];
                }
                const host = input.parentElement;
                const nextEmptyIndex = this.otpDigits.findIndex((d) => d === '');
                if (host && nextEmptyIndex !== -1) {
                    const nextEl = host.children.item(nextEmptyIndex) as HTMLInputElement | null;
                    nextEl?.focus();
                }
            } else {
                return;
            }
        }

        // Auto-submit when all digits entered
        if (!this.isSendingOtp && !this.isVerifyingOtp && this.otpDigits.every(d => d !== '')) {
            this.verifyOtp();
        }
    }

    onOtpKeydown(event: KeyboardEvent, index: number) {
        // Handle numeric entry here (prevents Chrome/IME from generating duplicate input events)
        if (/^\d$/.test(event.key)) {
            event.preventDefault();
            this.otpDigits[index] = event.key;
            if (index < 5) {
                const next = (event.target as HTMLElement).nextElementSibling as HTMLInputElement;
                // Defer focus change to avoid the key event being applied to the newly-focused input
                // on some browsers/IME (which can cause skipping every other box).
                if (next) setTimeout(() => next.focus(), 0);
            }

            if (!this.isSendingOtp && !this.isVerifyingOtp && this.otpDigits.every(d => d !== '')) {
                this.verifyOtp();
            }
            return;
        }

        if (event.key === 'Backspace' && !this.otpDigits[index] && index > 0) {
            const prev = (event.target as HTMLElement).previousElementSibling as HTMLInputElement;
            if (prev) {
                prev.focus();
                this.otpDigits[index - 1] = '';
            }
        }
    }

    onOtpPaste(event: ClipboardEvent) {
        event.preventDefault();
        const paste = event.clipboardData?.getData('text') || '';
        const digits = paste.replace(/\D/g, '').substring(0, 6);
        for (let i = 0; i < 6; i++) {
            this.otpDigits[i] = digits[i] || '';
        }
        if (digits.length === 6) {
            this.verifyOtp();
        }
    }

    // ─── HELPERS ───
    goBack() {
        this.errorMessage = '';
        this.successMessage = '';
        if (this.step === 'otp') {
            this.step = 'phone';
            this.otpDigits = ['', '', '', '', '', ''];
        } else if (this.step === 'phone') {
            this.step = 'classic';
            this.tempToken = '';
            this.oauthProvider = '';
        }
    }

    private startResendCooldown() {
        this.resendCooldown = 30;
        if (this.resendTimer) clearInterval(this.resendTimer);
        this.resendTimer = setInterval(() => {
            this.resendCooldown--;
            if (this.resendCooldown <= 0) {
                clearInterval(this.resendTimer);
            }
        }, 1000);
    }
}
