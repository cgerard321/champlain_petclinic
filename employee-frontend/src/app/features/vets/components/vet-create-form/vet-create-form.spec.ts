import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthState } from '@core/services/auth-state';
import { VetCreateForm } from '@features/vets/components/vet-create-form/vet-create-form';

describe('Admin veterinarian creation', () => {
  let admin: boolean;

  beforeEach(async () => {
    admin = true;

    const template = readFileSync(
      resolve(
        process.cwd(),
        'src/app/features/vets/components/vet-create-form/vet-create-form.html',
      ),
      'utf8',
    );

    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthState,
          useValue: {
            hasRole: () => admin,
          },
        },
      ],
    })
      .overrideComponent(VetCreateForm, {
        set: {
          templateUrl: undefined,
          styleUrl: undefined,
          template,
          styles: [],
        },
      })
      .compileComponents();
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  function setup(): ReturnType<typeof TestBed.createComponent<VetCreateForm>> {
    const fixture = TestBed.createComponent(VetCreateForm);
    fixture.detectChanges();
    return fixture;
  }

  function fill(component: VetCreateForm): void {
    component['form'].patchValue({
      firstName: 'Alex',
      lastName: 'Morin',
      email: 'alex@example.com',
      username: 'alex',
      password: 'SafePass1!',
      phoneNumber: '514-555-0123',
      resume: 'Preventive animal care.',
      specialties: ['surgery'],
    });
  }

  it('blocks missing fields and updates validation when corrected', () => {
    const fixture = setup();
    const c = fixture.componentInstance;

    c['save']();

    expect(c['form'].invalid).toBe(true);

    expect(fixture.nativeElement.querySelector('button[type=submit]').disabled).toBe(true);

    fill(c);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('button[type=submit]').disabled).toBe(false);

    c['form'].controls.email.setValue('invalid');
    c['form'].controls.email.markAsTouched();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#create-email').getAttribute('aria-invalid')).toBe(
      'true',
    );

    c['form'].controls.email.setValue('alex@example.com');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#create-email').getAttribute('aria-invalid')).toBe(
      'false',
    );
  });

  it('hides the form and prevents requests for non-admins', () => {
    admin = false;

    const fixture = setup();

    fill(fixture.componentInstance);
    fixture.componentInstance['save']();

    expect(fixture.nativeElement.querySelector('form')).toBeNull();

    TestBed.inject(HttpTestingController).expectNone('/api/v2/gateway/vets/users/vets');
  });

  it('cancels without creating a record', () => {
    const c = setup().componentInstance;

    let cancelled = false;

    c.cancelled.subscribe(() => {
      cancelled = true;
    });

    fill(c);
    c['cancel']();

    expect(cancelled).toBe(true);

    TestBed.inject(HttpTestingController).expectNone('/api/v2/gateway/vets/users/vets');
  });

  it('creates an account/profile, accepts 201 and prevents duplicate submission', () => {
    const c = setup().componentInstance;

    fill(c);

    let completed = false;

    c.completed.subscribe(() => {
      completed = true;
    });

    c['save']();
    c['save']();

    const req = TestBed.inject(HttpTestingController).expectOne('/api/v2/gateway/vets/users/vets');

    expect(req.request.method).toBe('POST');
    expect(req.request.body.vet.firstName).toBe('Alex');
    expect(req.request.body.vet.specialties[0].name).toBe('surgery');
    expect(req.request.body).not.toHaveProperty('userId');

    req.flush(
      { vetId: 'new-vet' },
      {
        status: 201,
        statusText: 'Created',
      },
    );

    expect(completed).toBe(true);
    expect(c['saving']()).toBe(false);
    expect(c['form'].controls.password.value).toBe('');
  });

  it('keeps values and displays a failed creation request', () => {
    const c = setup().componentInstance;

    fill(c);
    c['save']();

    TestBed.inject(HttpTestingController).expectOne('/api/v2/gateway/vets/users/vets').flush(
      {},
      {
        status: 400,
        statusText: 'Bad Request',
      },
    );

    expect(c['error']()).not.toBe('');
    expect(c['created']()).toBeNull();
    expect(c['form'].controls.firstName.value).toBe('Alex');
    expect(c['saving']()).toBe(false);
  });

  it('rejects incomplete education and reversed dates', () => {
    const c = setup().componentInstance;

    fill(c);

    c['education'].patchValue({
      schoolName: 'College',
    });

    expect(c['educationValid']()).toBe(false);

    c['education'].patchValue({
      degree: 'DVM',
      fieldOfStudy: 'Veterinary medicine',
      startDate: '2025-01-01',
      endDate: '2024-01-01',
    });

    expect(c['educationValid']()).toBe(false);

    c['education'].controls.endDate.setValue('2026-01-01');

    expect(c['educationValid']()).toBe(true);
  });

  it('retries education without creating the veterinarian twice', () => {
    const c = setup().componentInstance;
    const http = TestBed.inject(HttpTestingController);

    fill(c);

    c['education'].setValue({
      schoolName: 'College',
      degree: 'DVM',
      fieldOfStudy: 'Veterinary medicine',
      startDate: '2020-01-01',
      endDate: '2024-01-01',
    });

    c['save']();

    http.expectOne('/api/v2/gateway/vets/users/vets').flush({ vetId: 'new-vet' });

    http.expectOne('/api/v2/gateway/vets/new-vet/educations').flush(
      {},
      {
        status: 503,
        statusText: 'Unavailable',
      },
    );

    expect(c['created']()?.vetId).toBe('new-vet');

    c['save']();

    http.expectNone('/api/v2/gateway/vets/users/vets');

    http.expectOne('/api/v2/gateway/vets/new-vet/educations').flush({});

    expect(c['error']()).toBe('');
  });
});
