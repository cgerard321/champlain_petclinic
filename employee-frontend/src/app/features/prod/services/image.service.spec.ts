import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ImageService } from './image.service';

const API_BASE_URL = '/api/gateway/images';
const MOCK_IMAGE_ID = 'image-1';

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
    const mockImageResponse = {
      imageId: MOCK_IMAGE_ID,
      imageName: 'product.png',
      imageType: 'image/png',
      imageData: 'aW1hZ2U=',
    };

    service.getImage(MOCK_IMAGE_ID).subscribe();

    const request = http.expectOne(`${API_BASE_URL}/${MOCK_IMAGE_ID}`);
    expect(request.request.method).toBe('GET');
    request.flush(mockImageResponse);
  });
});
