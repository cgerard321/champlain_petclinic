import { Component, computed, input, OnDestroy, output, signal } from '@angular/core';

export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'] as const;
const DEFAULT_MAX_IMAGE_BYTES = 2 * 1024 * 1024;

@Component({
  selector: 'app-supply-image-editor',
  standalone: true,
  templateUrl: './image-editor.html',
  styleUrl: './image-editor.css',
})
export class ImageEditor implements OnDestroy {
  readonly label = input('Photo');
  readonly maxSizeBytes = input<number | undefined>(undefined);

  readonly currentImage = input<string | null>(null);
  readonly disabled = input(false);
  readonly imageChange = output<File | null>();

  private readonly limit = computed(() => this.maxSizeBytes() ?? DEFAULT_MAX_IMAGE_BYTES);
  protected readonly limitLabel = computed(() => `${this.limit() / (1024 * 1024)} MB`);

  protected readonly selectedPreview = signal<string | null>(null);
  protected readonly removed = signal(false);
  protected readonly error = signal('');

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
      this.error.set('Choose a JPG, PNG, or WebP image.');
      return;
    }

    // A provisional frontend limit; align it with the backend later.
    if (file.size > this.limit()) {
      this.error.set(`Choose an image smaller than ${this.limitLabel()}.`);
      return;
    }

    this.clearPreview();
    this.selectedPreview.set(URL.createObjectURL(file));
    this.removed.set(false);
    this.imageChange.emit(file);
  }

  protected removeImage(): void {
    if (this.disabled()) return;

    this.clearPreview();
    this.removed.set(true);
    this.error.set('');
    this.imageChange.emit(null);
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
