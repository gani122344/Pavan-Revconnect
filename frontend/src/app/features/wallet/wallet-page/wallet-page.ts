import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Navbar } from '../../../core/components/navbar/navbar';
import { Sidebar } from '../../../core/components/sidebar/sidebar';
import { WalletService, WalletResponse, TransactionResponse, PaymentRequestResponse, PageResponse } from '../../../core/services/wallet.service';
import { UserService, UserResponse } from '../../../core/services/user.service';

declare var Razorpay: any;

@Component({
  selector: 'app-wallet-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Navbar, Sidebar],
  templateUrl: './wallet-page.html',
  styleUrls: ['./wallet-page.css']
})
export class WalletPage implements OnInit {
  wallet: WalletResponse | null = null;
  transactions: TransactionResponse[] = [];
  pendingRequests: PaymentRequestResponse[] = [];
  currentUser: UserResponse | null = null;

  // UI state
  activeTab: 'dashboard' | 'send' | 'request' | 'history' | 'addmoney' | 'bank' | 'upi' = 'dashboard';
  loading = false;
  successMessage = '';
  errorMessage = '';

  // Send money
  sendRecipient = '';
  sendAmount: number | null = null;
  sendNote = '';
  searchResults: UserResponse[] = [];
  selectedRecipient: UserResponse | null = null;
  sendLoading = false;

  // Request money
  requestRecipient = '';
  requestAmount: number | null = null;
  requestNote = '';
  requestSearchResults: UserResponse[] = [];
  selectedPayer: UserResponse | null = null;
  requestLoading = false;

  // Add money
  addMoneyAmount: number | null = null;
  razorpayConfigured = false;
  razorpayKeyId = '';

  // Transaction history
  txnFilter = 'all';
  txnPage = 0;
  txnTotalPages = 0;

  // Bank
  bankAccounts: any[] = [];
  newBank = { accountHolderName: '', accountNumber: '', ifscCode: '', bankName: '', isPrimary: false };

  // UPI
  upiLinks: any[] = [];
  newUpi = { upiId: '', provider: '', isPrimary: false };

  // Success animation
  showSuccessAnim = false;
  successTxn: TransactionResponse | null = null;

  constructor(
    private walletService: WalletService,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadWallet();
    this.loadProfile();
    this.loadTransactions();
    this.loadPendingRequests();
    this.loadPaymentConfig();
  }

  private loadProfile() {
    this.userService.getMyProfile().subscribe(res => {
      if (res.success && res.data) this.currentUser = res.data;
    });
  }

  loadWallet() {
    this.walletService.createWallet().subscribe({
      next: res => { if (res.success && res.data) { this.wallet = res.data; this.cdr.detectChanges(); } },
      error: () => {}
    });
  }

  loadTransactions() {
    this.walletService.getTransactions(this.txnPage, 20, this.txnFilter).subscribe(res => {
      if (res.success && res.data) {
        this.transactions = res.data.content;
        this.txnTotalPages = res.data.totalPages;
        this.cdr.detectChanges();
      }
    });
  }

  loadPendingRequests() {
    this.walletService.getPendingRequests().subscribe(res => {
      if (res.success && res.data) {
        this.pendingRequests = res.data.content;
        this.cdr.detectChanges();
      }
    });
  }

  loadPaymentConfig() {
    this.walletService.getPaymentConfig().subscribe(res => {
      if (res.success && res.data) {
        this.razorpayConfigured = res.data.configured;
        this.razorpayKeyId = res.data.keyId;
      }
    });
  }

  // ─── Send Money ───
  searchUsers(query: string) {
    if (query.length < 2) { this.searchResults = []; return; }
    this.userService.searchUsers(query).subscribe(res => {
      if (res.success && res.data) {
        this.searchResults = (res.data as any).content || res.data;
        this.cdr.detectChanges();
      }
    });
  }

  selectRecipient(user: UserResponse) {
    this.selectedRecipient = user;
    this.sendRecipient = user.username;
    this.searchResults = [];
  }

  sendMoney() {
    if (!this.selectedRecipient || !this.sendAmount || this.sendAmount < 1) return;
    this.sendLoading = true;
    this.errorMessage = '';
    this.walletService.sendMoney(this.selectedRecipient.id, this.sendAmount, this.sendNote).subscribe({
      next: res => {
        this.sendLoading = false;
        if (res.success && res.data) {
          this.successTxn = res.data;
          this.showSuccessAnim = true;
          this.loadWallet();
          this.loadTransactions();
          this.sendRecipient = '';
          this.sendAmount = null;
          this.sendNote = '';
          this.selectedRecipient = null;
          setTimeout(() => { this.showSuccessAnim = false; this.cdr.detectChanges(); }, 3000);
        }
        this.cdr.detectChanges();
      },
      error: err => {
        this.sendLoading = false;
        this.errorMessage = err.error?.message || 'Failed to send money';
        this.cdr.detectChanges();
      }
    });
  }

  // ─── Request Money ───
  searchPayers(query: string) {
    if (query.length < 2) { this.requestSearchResults = []; return; }
    this.userService.searchUsers(query).subscribe(res => {
      if (res.success && res.data) {
        this.requestSearchResults = (res.data as any).content || res.data;
        this.cdr.detectChanges();
      }
    });
  }

  selectPayer(user: UserResponse) {
    this.selectedPayer = user;
    this.requestRecipient = user.username;
    this.requestSearchResults = [];
  }

  requestMoney() {
    if (!this.selectedPayer || !this.requestAmount || this.requestAmount < 1) return;
    this.requestLoading = true;
    this.errorMessage = '';
    this.walletService.requestMoney(this.selectedPayer.id, this.requestAmount, this.requestNote).subscribe({
      next: res => {
        this.requestLoading = false;
        if (res.success) {
          this.successMessage = 'Payment request sent!';
          this.requestRecipient = '';
          this.requestAmount = null;
          this.requestNote = '';
          this.selectedPayer = null;
          setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        }
        this.cdr.detectChanges();
      },
      error: err => {
        this.requestLoading = false;
        this.errorMessage = err.error?.message || 'Failed to send request';
        this.cdr.detectChanges();
      }
    });
  }

  acceptRequest(id: number) {
    this.walletService.acceptRequest(id).subscribe({
      next: res => {
        if (res.success) {
          this.loadWallet();
          this.loadPendingRequests();
          this.loadTransactions();
          this.successMessage = 'Payment completed!';
          setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        }
      },
      error: err => { this.errorMessage = err.error?.message || 'Failed to accept'; this.cdr.detectChanges(); }
    });
  }

  rejectRequest(id: number) {
    this.walletService.rejectRequest(id).subscribe({
      next: () => { this.loadPendingRequests(); },
      error: err => { this.errorMessage = err.error?.message || 'Failed to reject'; this.cdr.detectChanges(); }
    });
  }

  // ─── Add Money (Razorpay) ───
  addMoney() {
    if (!this.addMoneyAmount || this.addMoneyAmount < 1) return;
    if (!this.razorpayConfigured) {
      this.errorMessage = 'Payment gateway not configured. Contact admin.';
      return;
    }
    this.loading = true;
    this.walletService.createRazorpayOrder(this.addMoneyAmount).subscribe({
      next: res => {
        this.loading = false;
        if (res.success && res.data) {
          this.openRazorpayCheckout(res.data);
        }
        this.cdr.detectChanges();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to create order';
        this.cdr.detectChanges();
      }
    });
  }

  private openRazorpayCheckout(order: any) {
    const options = {
      key: order.keyId,
      amount: order.amount * 100,
      currency: order.currency,
      name: 'RevConnect',
      description: 'Add money to wallet',
      order_id: order.orderId,
      handler: (response: any) => {
        this.verifyPayment(response.razorpay_order_id, response.razorpay_payment_id, response.razorpay_signature);
      },
      prefill: {
        name: this.currentUser?.name || '',
        email: this.currentUser?.email || ''
      },
      theme: { color: '#7c3aed' }
    };
    const rzp = new Razorpay(options);
    rzp.open();
  }

  private verifyPayment(orderId: string, paymentId: string, signature: string) {
    this.walletService.verifyPayment(orderId, paymentId, signature).subscribe({
      next: res => {
        if (res.success) {
          this.successMessage = 'Money added successfully!';
          this.addMoneyAmount = null;
          this.loadWallet();
          this.loadTransactions();
          setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Payment verification failed';
        this.cdr.detectChanges();
      }
    });
  }

  // ─── Transactions ───
  filterTransactions(filter: string) {
    this.txnFilter = filter;
    this.txnPage = 0;
    this.loadTransactions();
  }

  nextPage() {
    if (this.txnPage < this.txnTotalPages - 1) { this.txnPage++; this.loadTransactions(); }
  }

  prevPage() {
    if (this.txnPage > 0) { this.txnPage--; this.loadTransactions(); }
  }

  // ─── Bank ───
  loadBankAccounts() {
    this.walletService.getBankAccounts().subscribe(res => {
      if (res.success && res.data) { this.bankAccounts = res.data; this.cdr.detectChanges(); }
    });
  }

  addBankAccount() {
    if (!this.newBank.accountHolderName || !this.newBank.accountNumber || !this.newBank.ifscCode || !this.newBank.bankName) return;
    this.walletService.addBankAccount(this.newBank).subscribe({
      next: res => {
        if (res.success) {
          this.successMessage = 'Bank account added!';
          this.loadBankAccounts();
          this.newBank = { accountHolderName: '', accountNumber: '', ifscCode: '', bankName: '', isPrimary: false };
          setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        }
      },
      error: err => { this.errorMessage = err.error?.message || 'Failed to add bank'; this.cdr.detectChanges(); }
    });
  }

  removeBankAccount(id: number) {
    this.walletService.deleteBankAccount(id).subscribe(() => this.loadBankAccounts());
  }

  // ─── UPI ───
  loadUpiLinks() {
    this.walletService.getUpis().subscribe(res => {
      if (res.success && res.data) { this.upiLinks = res.data; this.cdr.detectChanges(); }
    });
  }

  addUpiLink() {
    if (!this.newUpi.upiId) return;
    this.walletService.addUpi(this.newUpi).subscribe({
      next: res => {
        if (res.success) {
          this.successMessage = 'UPI ID linked!';
          this.loadUpiLinks();
          this.newUpi = { upiId: '', provider: '', isPrimary: false };
          setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        }
      },
      error: err => { this.errorMessage = err.error?.message || 'Failed to add UPI'; this.cdr.detectChanges(); }
    });
  }

  removeUpiLink(id: number) {
    this.walletService.deleteUpi(id).subscribe(() => this.loadUpiLinks());
  }

  switchTab(tab: typeof this.activeTab) {
    this.activeTab = tab;
    this.errorMessage = '';
    this.successMessage = '';
    if (tab === 'bank') this.loadBankAccounts();
    if (tab === 'upi') this.loadUpiLinks();
    if (tab === 'history') this.loadTransactions();
  }

  formatAmount(amount: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount);
  }

  getTimeAgo(dateStr: string): string {
    const d = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 1) return 'Just now';
    if (mins < 60) return mins + 'm ago';
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return hrs + 'h ago';
    const days = Math.floor(hrs / 24);
    if (days < 7) return days + 'd ago';
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  }

  isSent(txn: TransactionResponse): boolean {
    return txn.senderId === this.currentUser?.id;
  }
}
