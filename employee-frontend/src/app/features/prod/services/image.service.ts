import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ImageResponse } from '@features/prod/models/image.model';

@Injectable({ providedIn: 'root' })
export class ImageService {
  private readonly http = inject(HttpClient);

  uploadImage(file: File): Observable<ImageResponse> {
    const formData = new FormData();
    formData.append('imageName', file.name);
    formData.append('imageType', file.type);
    formData.append('imageData', file, file.name);

    return this.http.post<ImageResponse>('/api/gateway/images', formData);
  }

  getImage(imageId: string): Observable<ImageResponse> {
    return this.http.get<ImageResponse>(`/api/gateway/images/${imageId}`);
  }
}
