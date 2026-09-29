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

  it('uploads an image as multipart form data', () => {
    const file = new File(['image'], 'product.png', { type: 'image/png' });

    service.uploadImage(file).subscribe();

    const request = http.expectOne('/api/gateway/images');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('imageName')).toBe('product.png');
    expect((request.request.body as FormData).get('imageType')).toBe('image/png');
    request.flush({
      imageId: 'image-1',
      imageName: 'product.png',
      imageType: 'image/png',
      imageData: 'aW1hZ2U=',
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
