import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WalletResponse {
  id: number;
  userId: number;
  username: string;
  balance: number;
  currency: string;
  isActive: boolean;
  createdAt: string;
}

export interface TransactionResponse {
  id: number;
  transactionRef: string;
  senderId: number | null;
  senderName: string;
  senderUsername: string | null;
  senderPic: string | null;
  receiverId: number | null;
  receiverName: string;
  receiverUsername: string | null;
  receiverPic: string | null;
  amount: number;
  currency: string;
  type: string;
  status: string;
  paymentMethod: string;
  note: string | null;
  failureReason: string | null;
  createdAt: string;
  completedAt: string | null;
}

export interface PaymentRequestResponse {
  id: number;
  requesterId: number;
  requesterName: string;
  requesterUsername: string;
  requesterPic: string | null;
  payerId: number;
  payerName: string;
  payerUsername: string;
  payerPic: string | null;
  amount: number;
  currency: string;
  note: string | null;
  status: string;
  createdAt: string;
  respondedAt: string | null;
}

export interface RazorpayOrderResponse {
  orderId: string;
  amount: number;
  currency: string;
  transactionRef: string;
  keyId: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
}

@Injectable({ providedIn: 'root' })
export class WalletService {
  private api = '/api/wallet';
  private payApi = '/api/payment';

  constructor(private http: HttpClient) {}

  // Wallet
  createWallet(): Observable<ApiResponse<WalletResponse>> {
    return this.http.post<ApiResponse<WalletResponse>>(`${this.api}/create`, {});
  }

  getBalance(): Observable<ApiResponse<WalletResponse>> {
    return this.http.get<ApiResponse<WalletResponse>>(`${this.api}/balance`);
  }

  // Send Money
  sendMoney(recipientId: number, amount: number, note?: string): Observable<ApiResponse<TransactionResponse>> {
    return this.http.post<ApiResponse<TransactionResponse>>(`${this.api}/send`, { recipientId, amount, note });
  }

  sendMoneyByUsername(recipientUsername: string, amount: number, note?: string): Observable<ApiResponse<TransactionResponse>> {
    return this.http.post<ApiResponse<TransactionResponse>>(`${this.api}/send`, { recipientUsername, amount, note });
  }

  // Request Money
  requestMoney(payerId: number, amount: number, note?: string): Observable<ApiResponse<PaymentRequestResponse>> {
    return this.http.post<ApiResponse<PaymentRequestResponse>>(`${this.api}/request`, { payerId, amount, note });
  }

  acceptRequest(id: number): Observable<ApiResponse<TransactionResponse>> {
    return this.http.post<ApiResponse<TransactionResponse>>(`${this.api}/request/${id}/accept`, {});
  }

  rejectRequest(id: number): Observable<ApiResponse<PaymentRequestResponse>> {
    return this.http.post<ApiResponse<PaymentRequestResponse>>(`${this.api}/request/${id}/reject`, {});
  }

  getPaymentRequests(page = 0, size = 20): Observable<ApiResponse<PageResponse<PaymentRequestResponse>>> {
    return this.http.get<ApiResponse<PageResponse<PaymentRequestResponse>>>(`${this.api}/requests?page=${page}&size=${size}`);
  }

  getPendingRequests(page = 0, size = 20): Observable<ApiResponse<PageResponse<PaymentRequestResponse>>> {
    return this.http.get<ApiResponse<PageResponse<PaymentRequestResponse>>>(`${this.api}/requests/pending?page=${page}&size=${size}`);
  }

  getPendingRequestCount(): Observable<ApiResponse<{ count: number }>> {
    return this.http.get<ApiResponse<{ count: number }>>(`${this.api}/requests/pending/count`);
  }

  // Transactions
  getTransactions(page = 0, size = 20, filter?: string): Observable<ApiResponse<PageResponse<TransactionResponse>>> {
    let url = `${this.api}/transactions?page=${page}&size=${size}`;
    if (filter) url += `&filter=${filter}`;
    return this.http.get<ApiResponse<PageResponse<TransactionResponse>>>(url);
  }

  // Bank Account
  addBankAccount(req: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.api}/bank`, req);
  }

  getBankAccounts(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.api}/bank`);
  }

  deleteBankAccount(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.api}/bank/${id}`);
  }

  // UPI
  addUpi(req: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.api}/upi`, req);
  }

  getUpis(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.api}/upi`);
  }

  deleteUpi(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.api}/upi/${id}`);
  }

  // Razorpay
  getPaymentConfig(): Observable<ApiResponse<{ keyId: string; configured: boolean }>> {
    return this.http.get<ApiResponse<{ keyId: string; configured: boolean }>>(`${this.payApi}/config`);
  }

  createRazorpayOrder(amount: number): Observable<ApiResponse<RazorpayOrderResponse>> {
    return this.http.post<ApiResponse<RazorpayOrderResponse>>(`${this.payApi}/create-order`, { amount });
  }

  verifyPayment(razorpayOrderId: string, razorpayPaymentId: string, razorpaySignature: string): Observable<ApiResponse<TransactionResponse>> {
    return this.http.post<ApiResponse<TransactionResponse>>(`${this.payApi}/verify`, {
      razorpayOrderId, razorpayPaymentId, razorpaySignature
    });
  }
}
