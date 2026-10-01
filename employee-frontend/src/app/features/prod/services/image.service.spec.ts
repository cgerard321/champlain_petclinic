import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ImageService } from './image.service';

describe('ImageService', () => {
  let service: ImageService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ImageService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ImageService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('converts an image to Files Service details', async () => {
    const file = new File(['image'], 'product.png', { type: 'image/png' });

    const result = await service.toFileDetails(file);

    expect(result).toEqual({
      fileName: 'product.png',
      fileType: 'image/png',
      fileData: 'aW1hZ2U=',
    });
  });

  it('retrieves an image by id', () => {
    service.getImage('image-1').subscribe();

    const request = http.expectOne('/api/gateway/images/image-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      imageId: 'image-1',
      imageName: 'product.png',
      imageType: 'image/png',
      imageData: 'aW1hZ2U=',
    });
  });
});
