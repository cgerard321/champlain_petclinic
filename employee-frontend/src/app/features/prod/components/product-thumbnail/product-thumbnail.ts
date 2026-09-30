import { Component, effect, inject, input, signal } from '@angular/core';

import { ImageService } from '@features/prod/services/image.service';

@Component({
  selector: 'app-product-thumbnail',
  templateUrl: './product-thumbnail.html',
  styleUrl: './product-thumbnail.css',
})
export class ProductThumbnail {
  private readonly imageService = inject(ImageService);

  readonly imageId = input<string | undefined>();
  readonly alt = input('Product image');
  protected readonly imageSrc = signal<string | null>(null);

  constructor() {
    effect(() => {
      const imageId = this.imageId();
      this.imageSrc.set(null);

      if (!imageId) {
        return;
      }

      this.imageService.getImage(imageId).subscribe({
        next: (image) => this.imageSrc.set(`data:${image.imageType};base64,${image.imageData}`),
        error: () => this.imageSrc.set(null),
      });
    });
  }
}
