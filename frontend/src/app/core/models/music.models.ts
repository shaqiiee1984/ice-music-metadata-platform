export interface Artist {
  id: string;
  canonicalName: string;
  aliases: string[];
  active: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface Track {
  id: string;
  artistId: string;
  title: string;
  genre: string;
  durationMs: number;
  durationFormatted: string;
  isrc?: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ArtistDiscography {
  artistId: string;
  canonicalName: string;
  aliases: string[];
  availableGenres: string[];
  tracks: PageResponse<Track>;
}

export interface ArtistOfTheDay {
  id: string;
  canonicalName: string;
  aliases: string[];
  utcDate: string;
  cycleDayIndex: number;
  totalActiveArtists: number;
  nextRotationSecondsRemaining: number;
}

export interface CreateTrackRequest {
  artistId: string;
  title: string;
  genre: string;
  durationMs: number;
  isrc?: string;
}

export interface CreateArtistRequest {
  canonicalName: string;
  aliases: string[];
}

export interface UpdateArtistRequest {
  canonicalName?: string;
  aliases?: string[];
  active?: boolean;
}

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
  expiresIn: number;
}
