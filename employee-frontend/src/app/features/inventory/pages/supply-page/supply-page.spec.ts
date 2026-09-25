import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { SupplyPage } from './supply-page';

describe('SupplyPage', () => {
  let component: SupplyPage;
  let fixture: ComponentFixture<SupplyPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupplyPage],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '123',
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SupplyPage);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    // Assert
    expect(component).toBeTruthy();
  });

  it('should display the inventory ID from the route', () => {
    // Act
    fixture.detectChanges();

    // Assert
    expect(fixture.nativeElement.textContent).toContain('123');
  });
});
