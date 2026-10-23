import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  Artist,
  ArtistDiscography,
  ArtistOfTheDay,
  AuthResponse,
  CreateArtistRequest,
  CreateTrackRequest,
  PageResponse,
  Track,
  UpdateArtistRequest
} from '../models/music.models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = (typeof window !== 'undefined' && window.location.hostname === 'localhost' && window.location.port === '4200')
    ? 'http://localhost:8080/api/v1'
    : '/api/v1';

  // Reactive State Signals
  readonly isEditor = signal<boolean>(false);
  readonly currentToken = signal<string | null>(null);

  constructor(private http: HttpClient) {
    const savedToken = localStorage.getItem('ice_auth_token');
    if (savedToken) {
      this.currentToken.set(savedToken);
      this.isEditor.set(true);
    }
  }

  // --- Authentication ---
  enableEditorMode(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/editor-token`, {}).pipe(
      tap((res) => {
        localStorage.setItem('ice_auth_token', res.token);
        this.currentToken.set(res.token);
        this.isEditor.set(true);
      })
    );
  }

  disableEditorMode(): void {
    localStorage.removeItem('ice_auth_token');
    this.currentToken.set(null);
    this.isEditor.set(false);
  }

  // --- Featured / Artist of the Day ---
  getArtistOfTheDay(): Observable<ArtistOfTheDay> {
    return this.http.get<ArtistOfTheDay>(`${this.baseUrl}/featured/today`);
  }

  getArtistForDate(dateStr: string): Observable<ArtistOfTheDay> {
    return this.http.get<ArtistOfTheDay>(`${this.baseUrl}/featured/date/${dateStr}`);
  }

  // --- Artists ---
  listArtists(page = 0, size = 50): Observable<PageResponse<Artist>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Artist>>(`${this.baseUrl}/artists`, { params });
  }

  listActiveArtists(): Observable<Artist[]> {
    return this.http.get<Artist[]>(`${this.baseUrl}/artists/active`);
  }

  getArtistById(id: string): Observable<Artist> {
    return this.http.get<Artist>(`${this.baseUrl}/artists/${id}`);
  }

  createArtist(request: CreateArtistRequest): Observable<Artist> {
    return this.http.post<Artist>(`${this.baseUrl}/artists`, request);
  }

  updateArtist(id: string, request: UpdateArtistRequest): Observable<Artist> {
    return this.http.patch<Artist>(`${this.baseUrl}/artists/${id}`, request);
  }

  // --- Tracks & Catalogue ---
  createTrack(request: CreateTrackRequest): Observable<Track> {
    return this.http.post<Track>(`${this.baseUrl}/tracks`, request);
  }

  getArtistTracks(artistId: string, genre?: string, page = 0, size = 50): Observable<ArtistDiscography> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (genre && genre.trim().length > 0) {
      params = params.set('genre', genre.trim());
    }
    return this.http.get<ArtistDiscography>(`${this.baseUrl}/catalogue/artists/${artistId}/tracks`, { params });
  }
}
