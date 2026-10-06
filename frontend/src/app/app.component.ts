import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from './core/services/api.service';
import { Artist, ArtistDiscography, ArtistOfTheDay, Track } from './core/models/music.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  // State Signals
  readonly artistOfTheDay = signal<ArtistOfTheDay | null>(null);
  readonly artists = signal<Artist[]>([]);
  readonly selectedArtistId = signal<string | null>(null);
  readonly discography = signal<ArtistDiscography | null>(null);
  readonly selectedGenre = signal<string | null>(null);
  readonly loadingDiscography = signal<boolean>(false);
  readonly toastMessage = signal<{ text: string; type: 'success' | 'error' } | null>(null);

  // Countdown timer state
  remainingSeconds = signal<number>(0);
  private countdownInterval: any;

  // Modals state
  showAddTrackModal = false;
  showEditArtistModal = false;
  showNewArtistModal = false;

  // Add Track Form
  newTrack = {
    artistId: '',
    title: '',
    genre: '',
    durationMinutes: 3,
    durationSeconds: 30,
    isrc: ''
  };

  // Edit Artist Form
  editArtistData = {
    id: '',
    canonicalName: '',
    currentName: '',
    aliases: [] as string[],
    newAlias: ''
  };

  // New Artist Form
  newArtistData = {
    canonicalName: '',
    aliasesText: ''
  };

  // Computed Values
  readonly countdownDisplay = computed(() => {
    const s = this.remainingSeconds();
    if (s <= 0) return '00:00:00';
    const hrs = Math.floor(s / 3600);
    const mins = Math.floor((s % 3600) / 60);
    const secs = s % 60;
    return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  });

  constructor(public api: ApiService) {}

  ngOnInit(): void {
    // Automatically acquire Editor token on startup if not present
    if (!this.api.isEditor()) {
      this.api.enableEditorMode().subscribe({
        next: () => console.log('Editor session initialized with JWT token'),
        error: (err) => console.warn('Could not auto-enable editor session', err)
      });
    }

    this.loadFeaturedArtist();
    this.loadArtists();

    // UTC Midnight countdown ticker
    this.countdownInterval = setInterval(() => {
      this.remainingSeconds.update(s => Math.max(0, s - 1));
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  loadFeaturedArtist(): void {
    this.api.getArtistOfTheDay().subscribe({
      next: (data) => {
        this.artistOfTheDay.set(data);
        if (data) {
          this.remainingSeconds.set(data.nextRotationSecondsRemaining || 3600);
        }
      },
      error: (err) => console.error('Failed to load Artist of the Day', err)
    });
  }

  loadArtists(): void {
    this.api.listArtists(0, 100).subscribe({
      next: (page) => {
        this.artists.set(page.content);
        if (page.content.length > 0 && !this.selectedArtistId()) {
          this.selectArtist(page.content[0].id);
        }
      },
      error: (err) => console.error('Failed to load artists roster', err)
    });
  }

  selectArtist(artistId: string): void {
    this.selectedArtistId.set(artistId);
    this.selectedGenre.set(null);
    this.loadDiscography(artistId, null);
  }

  filterByGenre(genre: string | null): void {
    this.selectedGenre.set(genre);
    const id = this.selectedArtistId();
    if (id) {
      this.loadDiscography(id, genre);
    }
  }

  loadDiscography(artistId: string, genre: string | null): void {
    this.loadingDiscography.set(true);
    this.api.getArtistTracks(artistId, genre || undefined).subscribe({
      next: (disco) => {
        this.discography.set(disco);
        this.loadingDiscography.set(false);
      },
      error: (err) => {
        console.error('Failed to load discography', err);
        this.loadingDiscography.set(false);
      }
    });
  }

  // --- Auth & Role Controls ---
  toggleEditorMode(): void {
    if (this.api.isEditor()) {
      this.api.disableEditorMode();
      this.showToast('Viewer Mode Enabled (Read-Only)', 'success');
    } else {
      this.api.enableEditorMode().subscribe({
        next: () => this.showToast('Editor Mode Enabled (JWT Token Active)', 'success'),
        error: () => this.showToast('Failed to acquire editor token', 'error')
      });
    }
  }

  // --- Track Actions ---
  openAddTrackModal(): void {
    if (!this.api.isEditor()) {
      this.api.enableEditorMode().subscribe({
        next: () => this.doOpenAddTrackModal(),
        error: () => this.showToast('Failed to acquire editor token', 'error')
      });
      return;
    }
    this.doOpenAddTrackModal();
  }

  private doOpenAddTrackModal(): void {
    this.newTrack = {
      artistId: this.selectedArtistId() || (this.artists()[0]?.id ?? ''),
      title: '',
      genre: 'Electropop',
      durationMinutes: 3,
      durationSeconds: 45,
      isrc: ''
    };
    this.showAddTrackModal = true;
  }

  submitAddTrack(): void {
    const artistId = this.selectedArtistId();
    if (!artistId || !this.newTrack.title.trim()) {
      this.showToast('Please enter a valid track title.', 'error');
      return;
    }

    const durationMs = (this.newTrack.durationMinutes * 60 + this.newTrack.durationSeconds) * 1000;

    this.api.createTrack({
      artistId: artistId,
      title: this.newTrack.title.trim(),
      genre: this.newTrack.genre.trim(),
      durationMs: durationMs,
      isrc: this.newTrack.isrc.trim() || undefined
    }).subscribe({
      next: () => {
        this.showToast(`Track '${this.newTrack.title}' added successfully!`, 'success');
        this.showAddTrackModal = false;
        this.loadDiscography(artistId, this.selectedGenre());
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to add track';
        this.showToast(msg, 'error');
      }
    });
  }

  // --- Artist Edit Actions ---
  openEditArtistModal(artist: Artist): void {
    if (!this.api.isEditor()) {
      this.api.enableEditorMode().subscribe({
        next: () => this.doOpenEditArtistModal(artist),
        error: () => this.showToast('Failed to acquire editor token', 'error')
      });
      return;
    }
    this.doOpenEditArtistModal(artist);
  }

  private doOpenEditArtistModal(artist: Artist): void {
    this.editArtistData = {
      id: artist.id,
      canonicalName: artist.canonicalName,
      currentName: artist.canonicalName,
      aliases: [...(artist.aliases || [])],
      newAlias: ''
    };
    this.showEditArtistModal = true;
  }

  addAliasToEdit(): void {
    const val = this.editArtistData.newAlias.trim();
    if (val && !this.editArtistData.aliases.includes(val)) {
      this.editArtistData.aliases.push(val);
      this.editArtistData.newAlias = '';
    }
  }

  removeAliasFromEdit(alias: string): void {
    this.editArtistData.aliases = this.editArtistData.aliases.filter(a => a !== alias);
  }

  submitEditArtist(): void {
    if (!this.editArtistData.canonicalName.trim()) {
      this.showToast('Canonical name cannot be blank', 'error');
      return;
    }

    this.api.updateArtist(this.editArtistData.id, {
      canonicalName: this.editArtistData.canonicalName.trim(),
      aliases: this.editArtistData.aliases
    }).subscribe({
      next: (updated) => {
        this.showToast(`Artist '${updated.canonicalName}' updated! Previous name archived to aliases.`, 'success');
        this.showEditArtistModal = false;
        this.loadArtists();
        this.loadFeaturedArtist();
        if (this.selectedArtistId() === updated.id) {
          this.loadDiscography(updated.id, this.selectedGenre());
        }
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to update artist', 'error');
      }
    });
  }

  // --- New Artist Actions ---
  openNewArtistModal(): void {
    if (!this.api.isEditor()) {
      this.api.enableEditorMode().subscribe({
        next: () => this.doOpenNewArtistModal(),
        error: () => this.showToast('Failed to acquire editor token', 'error')
      });
      return;
    }
    this.doOpenNewArtistModal();
  }

  private doOpenNewArtistModal(): void {
    this.newArtistData = {
      canonicalName: '',
      aliasesText: ''
    };
    this.showNewArtistModal = true;
  }

  submitNewArtist(): void {
    if (!this.newArtistData.canonicalName.trim()) {
      this.showToast('Canonical name is required', 'error');
      return;
    }

    const aliases = this.newArtistData.aliasesText
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);

    this.api.createArtist({
      canonicalName: this.newArtistData.canonicalName.trim(),
      aliases: aliases
    }).subscribe({
      next: (created) => {
        this.showToast(`Artist '${created.canonicalName}' created successfully!`, 'success');
        this.showNewArtistModal = false;
        this.loadArtists();
        this.selectArtist(created.id);
        this.loadFeaturedArtist();
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to create artist', 'error');
      }
    });
  }

  showToast(text: string, type: 'success' | 'error'): void {
    this.toastMessage.set({ text, type });
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }
}
