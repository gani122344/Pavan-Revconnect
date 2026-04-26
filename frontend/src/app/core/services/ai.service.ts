import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AiChatResponse {
  reply: string;
}

export interface AiCaptionResponse {
  caption: string;
  hashtags: string[];
  emojis: string[];
}

export interface AiModerationResponse {
  safe: boolean;
  score: number;
  flags: string[];
  reason: string;
}

export interface AiHealthResponse {
  status: string;
  baseUrl: string;
  defaultModel: string;
  availableModels: string[];
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class AiService {
  private api = '/api/ai';

  constructor(private http: HttpClient) {}

  // AI Chat Assistant
  chat(message: string, history?: { role: string; content: string }[]): Observable<{ success: boolean; data: AiChatResponse }> {
    return this.http.post<any>(`${this.api}/chat`, { message, history });
  }

  // Smart Reply Suggestions
  getSmartReplies(message: string, context?: string): Observable<{ success: boolean; data: string[] }> {
    return this.http.post<any>(`${this.api}/smart-replies`, { message, context });
  }

  // Caption Generator
  generateCaption(context: string, mood?: string, platform?: string): Observable<{ success: boolean; data: AiCaptionResponse }> {
    return this.http.post<any>(`${this.api}/caption`, { context, mood, platform });
  }

  // Hashtag Suggestions
  suggestHashtags(content: string, count?: number): Observable<{ success: boolean; data: string[] }> {
    return this.http.post<any>(`${this.api}/hashtags`, { content, count: count || 5 });
  }

  // Bio Generator
  generateBio(name: string, userType?: string, category?: string, interests?: string): Observable<{ success: boolean; data: string[] }> {
    return this.http.post<any>(`${this.api}/bio`, { name, userType, category, interests });
  }

  // Analytics Insights
  getInsights(analyticsData: any): Observable<{ success: boolean; data: { insights: string } }> {
    return this.http.post<any>(`${this.api}/insights`, analyticsData);
  }

  // Content Moderation
  moderateContent(content: string): Observable<{ success: boolean; data: AiModerationResponse }> {
    return this.http.post<any>(`${this.api}/moderate`, { content });
  }

  // Collaboration Matchmaking
  getMatchAnalysis(businessInfo: string, creatorInfo: string): Observable<{ success: boolean; data: { analysis: string } }> {
    return this.http.post<any>(`${this.api}/match`, { businessInfo, creatorInfo });
  }

  // PDF Summarization
  summarizeText(text: string): Observable<{ success: boolean; data: { summary: string } }> {
    return this.http.post<any>(`${this.api}/summarize`, { text });
  }

  // Health Check
  healthCheck(): Observable<{ success: boolean; data: AiHealthResponse }> {
    return this.http.get<any>(`${this.api}/health`);
  }
}
