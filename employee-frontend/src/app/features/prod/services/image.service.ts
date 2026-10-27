import { Injectable } from '@angular/core';

import { FileDetails } from '@features/prod/models/image.model';

@Injectable({ providedIn: 'root' })
export class ImageService {
  toFileDetails(file: File): Promise<FileDetails> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const dataUrl = reader.result;
        if (typeof dataUrl !== 'string' || !dataUrl.includes(',')) {
          reject(new Error('Could not read the selected image.'));
          return;
        }

        resolve({
          fileName: file.name,
          fileType: file.type,
          fileData: dataUrl.slice(dataUrl.indexOf(',') + 1),
        });
      };
      reader.onerror = () => reject(new Error('Could not read the selected image.'));
      reader.readAsDataURL(file);
    });
  }
}
