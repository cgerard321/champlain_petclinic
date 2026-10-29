import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { FileDetails, ImageResponse } from '@features/prod/models/image.model';

@Injectable({ providedIn: 'root' })
export class ImageService {
  private readonly http = inject(HttpClient);

  toFileDetails(file: File): Promise<FileDetails> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = () => {
        const result = reader.result as string;
        const base64 = result.split(',')[1];

        resolve({
          fileName: file.name,
          fileType: file.type,
          fileData: base64,
        } as FileDetails);
      };

      reader.onerror = () => reject(reader.error);

      reader.readAsDataURL(file);
    });
  }

  getImage(imageId: string): Observable<ImageResponse> {
    return this.http.get<ImageResponse>(
      `/api/gateway/images/${imageId}`
    );
  }
}
