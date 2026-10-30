import { TestBed } from '@angular/core/testing';

import { ProductThumbnail } from './product-thumbnail';

describe('ProductThumbnail Files Service images', () => {
  it('renders the embedded file returned with a product', async () => {
    const fixture = TestBed.createComponent(ProductThumbnail);
    fixture.componentRef.setInput('image', {
      fileId: 'file-1',
      fileName: 'product.png',
      fileType: 'image/png',
      fileData: 'aW1hZ2U=',
    });
    fixture.componentRef.setInput('alt', 'Dog Food');
    await fixture.whenStable();
    const image = fixture.nativeElement.querySelector('img') as HTMLImageElement;
    expect(image.getAttribute('src')).toBe('data:image/png;base64,aW1hZ2U=');
    expect(image.alt).toBe('Dog Food');
  });

  it('shows the placeholder when no file is returned, without a legacy image lookup', async () => {
    const fixture = TestBed.createComponent(ProductThumbnail);
    fixture.componentRef.setInput('image', null);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('No image');
  });
});
