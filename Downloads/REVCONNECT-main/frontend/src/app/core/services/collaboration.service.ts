import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CollaborationRequest {
  creatorId: number;
  message?: string;
  promotionRules?: string;
  contentGuidelines?: string;
  fixedFee?: number;
  ratePerView?: number;
  viewMilestone?: number;
  paymentSchedule?: string;
  brandName?: string;
  brandWebsite?: string;
  durationDays?: number;
}

export interface ContractResponse {
  id: number;
  promotionRules: string;
  contentGuidelines?: string;
  fixedFee: number;
  ratePerView: number;
  viewMilestone: number;
  paymentSchedule: string;
  brandName?: string;
  brandWebsite?: string;
  durationDays: number;
  startDate?: string;
  endDate?: string;
  acceptedByCreator: boolean;
  acceptedAt?: string;
  createdAt: string;
}

export interface CollaborationResponse {
  id: number;
  status: string;
  businessId: number;
  businessName: string;
  businessUsername: string;
  businessPic?: string;
  creatorId: number;
  creatorName: string;
  creatorUsername: string;
  creatorPic?: string;
  message?: string;
  contract?: ContractResponse;
  createdAt: string;
  updatedAt: string;
}

export interface PostPromotionRequest {
  postId: number;
  creatorId: number;
  ctaLabel?: string;
  ctaUrl?: string;
}

export interface PostPromotionResponse {
  id: number;
  postId: number;
  postContent: string;
  postMediaUrl?: string;
  creatorId: number;
  creatorName: string;
  creatorUsername: string;
  creatorPic?: string;
  businessId: number;
  businessName: string;
  businessUsername: string;
  businessPic?: string;
  status: string;
  ctaLabel?: string;
  ctaUrl?: string;
  organicClicks: number;
  creatorClicks: number;
  promotedAt?: string;
  expiresAt?: string;
  createdAt: string;
}

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

interface PagedData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class CollaborationService {
  private api = '/api/collaborations';

  constructor(private http: HttpClient) {}

  // Business: Invite a creator
  inviteCreator(request: CollaborationRequest): Observable<ApiResponse<CollaborationResponse>> {
    return this.http.post<ApiResponse<CollaborationResponse>>(`${this.api}/invite`, request);
  }

  // Creator: Accept collaboration
  acceptCollaboration(id: number): Observable<ApiResponse<CollaborationResponse>> {
    return this.http.post<ApiResponse<CollaborationResponse>>(`${this.api}/${id}/accept`, {});
  }

  // Creator: Reject collaboration
  rejectCollaboration(id: number): Observable<ApiResponse<CollaborationResponse>> {
    return this.http.post<ApiResponse<CollaborationResponse>>(`${this.api}/${id}/reject`, {});
  }

  // Business: Revoke collaboration
  revokeCollaboration(id: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.api}/${id}/revoke`, {});
  }

  // Get my collaborations
  getMyCollaborations(status?: string, page = 0, size = 20): Observable<ApiResponse<PagedData<CollaborationResponse>>> {
    let url = `${this.api}?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    return this.http.get<ApiResponse<PagedData<CollaborationResponse>>>(url);
  }

  // Get single collaboration
  getCollaboration(id: number): Observable<ApiResponse<CollaborationResponse>> {
    return this.http.get<ApiResponse<CollaborationResponse>>(`${this.api}/${id}`);
  }

  // Business: Grant promotion
  grantPromotion(request: PostPromotionRequest): Observable<ApiResponse<PostPromotionResponse>> {
    return this.http.post<ApiResponse<PostPromotionResponse>>(`${this.api}/promotions/grant`, request);
  }

  // Business: Revoke promotion
  revokePromotion(id: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.api}/promotions/${id}/revoke`, {});
  }

  // Get promotions for a post
  getPostPromotions(postId: number): Observable<ApiResponse<PostPromotionResponse[]>> {
    return this.http.get<ApiResponse<PostPromotionResponse[]>>(`${this.api}/promotions/post/${postId}`);
  }

  // Check if I can promote a post
  canPromote(postId: number): Observable<ApiResponse<{ canPromote: boolean }>> {
    return this.http.get<ApiResponse<{ canPromote: boolean }>>(`${this.api}/promotions/check/${postId}`);
  }

  // Track CTA click
  trackCtaClick(promotionId: number, fromCreator = false): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.api}/promotions/${promotionId}/click?fromCreator=${fromCreator}`, {});
  }

  // Get promotion label
  getPromotionLabel(postId: number, creatorId: number): Observable<ApiResponse<PostPromotionResponse>> {
    return this.http.get<ApiResponse<PostPromotionResponse>>(`${this.api}/promotions/label?postId=${postId}&creatorId=${creatorId}`);
  }

  // Download contract PDF
  downloadContractPdf(collaborationId: number): void {
    this.http.get(`${this.api}/${collaborationId}/contract/pdf`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `RevConnect-Contract-RC-${collaborationId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to download contract PDF')
    });
  }
}
