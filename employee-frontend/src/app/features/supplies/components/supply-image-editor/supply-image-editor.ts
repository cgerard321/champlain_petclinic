import { Component, computed, input, OnDestroy, output, signal } from '@angular/core';

@Component({
  selector: 'app-supply-image-editor',
  standalone: true,
  templateUrl: './supply-image-editor.html',
  styleUrl: './supply-image-editor.css',
})
export class SupplyImageEditor implements OnDestroy {
  readonly currentImage = input<string | null>(null);
  readonly disabled = input(false);
  readonly imageChange = output<File | null>();

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

    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      this.error.set('Choose a JPG, PNG, or WebP image.');
      return;
    }

    // A provisional frontend limit; align it with the backend later.
    if (file.size > 2 * 1024 * 1024) {
      this.error.set('Choose an image smaller than 2 MB.');
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
