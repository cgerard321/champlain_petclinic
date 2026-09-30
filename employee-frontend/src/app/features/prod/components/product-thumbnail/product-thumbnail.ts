import { Component, effect, inject, input, signal } from '@angular/core';

import { FileDetails } from '@features/prod/models/image.model';
import { ImageService } from '@features/prod/services/image.service';

@Component({
  selector: 'app-product-thumbnail',
  templateUrl: './product-thumbnail.html',
  styleUrl: './product-thumbnail.css',
})
export class ProductThumbnail {
  private readonly imageService = inject(ImageService);

  readonly imageId = input<string | undefined>();
  readonly image = input<FileDetails | null | undefined>();
  readonly alt = input('Product image');
  protected readonly imageSrc = signal<string | null>(null);

  constructor() {
    effect(() => {
      const imageId = this.imageId();
      const image = this.image();
      this.imageSrc.set(null);

      if (image?.fileData) {
        this.imageSrc.set(`data:${image.fileType};base64,${image.fileData}`);
        return;
      }

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
