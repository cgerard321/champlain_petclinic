import { Component, computed, input } from '@angular/core';

import { FileDetails } from '@features/prod/models/image.model';

@Component({
  selector: 'app-product-thumbnail',
  templateUrl: './product-thumbnail.html',
  styleUrl: './product-thumbnail.css',
})
export class ProductThumbnail {
  readonly image = input<FileDetails | null | undefined>();
  readonly alt = input('Product image');
  protected readonly imageSrc = computed(() => {
    const image = this.image();
    return image?.fileData ? `data:${image.fileType};base64,${image.fileData}` : null;
  });
}
