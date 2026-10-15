import { Component, computed, input, OnDestroy, output, signal } from '@angular/core';

export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'] as const;
export type ImageAction =
  { action: 'keep' } | { action: 'replace'; file: File } | { action: 'remove' };

const DEFAULT_MAX_IMAGE_BYTES = 2 * 1024 * 1024;

@Component({
  selector: 'app-image-editor',
  standalone: true,
  templateUrl: './image-editor.html',
  styleUrl: './image-editor.css',
})
export class ImageEditor implements OnDestroy {
  readonly label = input('Photo');
  readonly maxSizeBytes = input<number | undefined>(undefined);

  readonly currentImage = input<string | null>(null);
  readonly disabled = input(false);
  readonly imageChange = output<ImageAction>();

  private readonly limit = computed(() => this.maxSizeBytes() ?? DEFAULT_MAX_IMAGE_BYTES);
  protected readonly limitLabel = computed(() => `${this.limit() / (1024 * 1024)} MB`);

  protected readonly selectedPreview = signal<string | null>(null);
  protected readonly removed = signal(false);
  protected readonly error = signal('');
  protected readonly changeLabel = $localize`:@@image.actions.change:Changer l'image`;
  protected readonly addLabel = $localize`:@@image.actions.add:Ajouter une image`;

  protected chooseLabel(): string {
    return $localize`:@@image.actions.choose:Choisir ${this.label()}:label:`;
  }

  protected previewAlt(): string {
    return $localize`:@@image.previewAlt:Aperçu de ${this.label()}:label:`;
  }

  protected readonly preview = computed(() =>
    this.removed() ? null : (this.selectedPreview() ?? this.currentImage()),
  );

  protected selectImage(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const file = inputElement.files?.[0];
    inputElement.value = '';

    if (!file || this.disabled()) return;

    this.error.set('');

    if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
      this.error.set($localize`:@@image.errors.type:Choisissez une image JPG, PNG ou WebP.`);
      return;
    }

    // A provisional frontend limit; align it with the backend later.
    if (file.size > this.limit()) {
      this.error.set(
        $localize`:@@image.errors.size:Choisissez une image de moins de ${this.limitLabel()}:limit:.`,
      );
      return;
    }

    this.clearPreview();
    this.selectedPreview.set(URL.createObjectURL(file));
    this.removed.set(false);
    this.imageChange.emit({ action: 'replace', file });
  }

  protected removeImage(): void {
    if (this.disabled()) return;

    this.clearPreview();
    this.removed.set(true);
    this.error.set('');

    this.imageChange.emit(this.currentImage() ? { action: 'remove' } : { action: 'keep' });
  }

  private clearPreview(): void {
    const preview = this.selectedPreview();

    if (preview) URL.revokeObjectURL(preview);

    this.selectedPreview.set(null);
  }

  ngOnDestroy(): void {
    this.clearPreview();
  }
}
