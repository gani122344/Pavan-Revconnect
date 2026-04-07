import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AudioPlayerService {
  private audio: HTMLAudioElement | null = null;
  private playing = false;
  private songKey = '';
  private loading = false;
  private urlCache = new Map<string, string>();

  play(songTitle: string, genre: string, loop = true): void {
    const key = `${songTitle}::${genre}`;
    if (this.playing && this.songKey === key) return;
    this.stop();

    this.playing = true;
    this.songKey = key;
    this.loading = true;

    this.fetchSongUrl(songTitle).then(url => {
      if (!url || this.songKey !== key) {
        if (this.songKey === key) { this.playing = false; this.songKey = ''; }
        this.loading = false;
        return;
      }
      this.loading = false;
      this.audio = new Audio(url);
      this.audio.loop = loop;
      this.audio.volume = 0.7;
      this.audio.crossOrigin = 'anonymous';
      this.audio.play().catch(err => {
        console.error('Audio play failed:', err);
        this.playing = false;
        this.songKey = '';
      });
      this.audio.onended = () => {
        if (!loop) { this.playing = false; this.songKey = ''; }
      };
    });
  }

  stop(): void {
    if (this.audio) {
      this.audio.pause();
      this.audio.currentTime = 0;
      this.audio.src = '';
      this.audio = null;
    }
    this.playing = false;
    this.songKey = '';
    this.loading = false;
  }

  toggle(songTitle: string, genre: string): boolean {
    if (this.playing && this.songKey === `${songTitle}::${genre}`) {
      this.stop();
      return false;
    }
    this.play(songTitle, genre);
    return true;
  }

  getIsPlaying(): boolean { return this.playing; }
  getCurrentSongKey(): string { return this.songKey; }
  getIsLoading(): boolean { return this.loading; }

  private async fetchSongUrl(title: string): Promise<string | null> {
    const cached = this.urlCache.get(title);
    if (cached) return cached;

    // Try full title first, then first 2 words as fallback
    const queries = [title];
    const words = title.split(/\s+/);
    if (words.length > 2) {
      queries.push(words.slice(0, 2).join(' '));
    }
    if (words.length > 1) {
      queries.push(words[0]);
    }

    for (const query of queries) {
      try {
        const res = await fetch(`/api/music/search?query=${encodeURIComponent(query)}`);
        const data = await res.json();
        const results = data?.results;
        if (!results || results.length === 0) {
          console.warn(`[Audio] No results for "${query}"`);
          continue;
        }

        const withPreview = results.filter((r: any) => r.previewUrl);
        if (withPreview.length === 0) {
          console.warn(`[Audio] No preview URLs for "${query}"`);
          continue;
        }

        const titleLower = title.toLowerCase();
        const titleWords = titleLower.split(/\s+/).filter((w: string) => w.length > 2);
        const match = withPreview.find((r: any) => {
          const name = (r.trackName || '').toLowerCase();
          return titleWords.some((w: string) => name.includes(w));
        }) || withPreview[0];

        const url = match.previewUrl;
        if (url) {
          console.log(`[Audio] Found: "${match.trackName}" for "${title}"`);
          this.urlCache.set(title, url);
          return url;
        }
      } catch (err) {
        console.error(`[Audio] Search failed for "${query}":`, err);
      }
    }
    console.warn(`[Audio] Song not found on iTunes: "${title}"`);
    return null;
  }
}
