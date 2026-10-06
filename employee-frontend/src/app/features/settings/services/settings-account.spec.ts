import { provideHttpClient } from '@angular/common/http';
//simulate http call
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { environment } from '@environments/environment';

import { SettingsAccount } from './settings-account';

const API_BASE_URL = '/api/gateway/users';
//fake id
const USER_ID = '69f852ca-625b-11ee-8c99-0242ac120002';

const CURRENT_USER: CurrentUserResponse = {
  userId: USER_ID,
  email: 'Vet1',
  username: 'Vet1',
  roles: ['VET'],
};

describe('SettingsAccount (VETS-CPC-2090)', () => {
  let service: SettingsAccount;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SettingsAccount, provideHttpClient(), provideHttpClientTesting()],
    });

    http = TestBed.inject(HttpTestingController);
    // Injecting the service fires the /jwt read straight away, so every test has to answer it.
    service = TestBed.inject(SettingsAccount);
  });

  afterEach(() => http.verify());

  function flushCurrentUser(user: CurrentUserResponse | null = CURRENT_USER): void {
    const request = http.expectOne(`${API_BASE_URL}/jwt`);
    expect(request.request.method).toBe('GET');

    if (user) {
      request.flush(user);
    } else {
      request.flush('no token', { status: 401, statusText: 'Unauthorized' });
    }
  }

  describe('currentUser', () => {
    // 1. NEGATIVE - Nothing is known before the server answers.
    it('starts empty', () => {
      // Assert
      expect(service.currentUser()).toBeNull();
      flushCurrentUser();
    });

    // 2. POSITIVE - Reads the signed-in employee from the existing v1 endpoint.
    it('exposes the signed-in employee once the token endpoint answers', () => {
      // Act
      flushCurrentUser();

      // Assert
      expect(service.currentUser()).toEqual(CURRENT_USER);
    });

    // 3. POSITIVE - One read per visit, shared by the page and its sections.
    it('reads the token endpoint only once', () => {
      // Act
      flushCurrentUser();
      service.currentUser();
      service.currentUser();

      // Assert
      http.expectNone(`${API_BASE_URL}/jwt`);
    });

    // 4. NEGATIVE - A failed read leaves the page usable instead of throwing.
    it('stays empty when the token endpoint fails', () => {
      // Act
      flushCurrentUser(null);

      // Assert
      expect(service.currentUser()).toBeNull();
    });
  });

  describe('updateUsername', () => {
    beforeEach(() => flushCurrentUser());

    // 5. POSITIVE - Hits the existing v1 endpoint with the user id in the path.
    it('patches the username of the given user', () => {
      // Act
      service.updateUsername(USER_ID, 'jean_dupont').subscribe((saved) => {
        // Assert
        expect(saved).toBe('jean_dupont');
      });

      const request = http.expectOne(`${API_BASE_URL}/${USER_ID}/username`);
      expect(request.request.method).toBe('PATCH');
      request.flush('jean_dupont');
    });

    // 6. POSITIVE - The body is a bare string, not JSON.
    // The gateway validates the body against a regex, so braces and quotes from a JSON object
    // would make it answer 400. This assertion is what keeps that from regressing.
    it('sends the new name as a bare string', () => {
      // Act
      service.updateUsername(USER_ID, 'jean_dupont').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/${USER_ID}/username`);
      expect(request.request.body).toBe('jean_dupont');
      expect(typeof request.request.body).toBe('string');
      request.flush('jean_dupont');
    });

    // 7. POSITIVE - Plain text in, plain text out.
    it('declares a plain text body and reads a plain text answer', () => {
      // Act
      service.updateUsername(USER_ID, 'jean_dupont').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/${USER_ID}/username`);
      expect(request.request.headers.get('Content-Type')).toBe('text/plain');
      expect(request.request.responseType).toBe('text');
      request.flush('jean_dupont');
    });

    // 8. NEGATIVE - A refused name surfaces as an error the caller can handle.
    // A duplicate lands here as a 500, because of the UNIQUE constraint in schema-mysql.sql.
    it('fails when the server refuses the name', () => {
      // Arrange
      let status = 0;

      // Act
      service.updateUsername(USER_ID, 'jean_dupont').subscribe({
        error: (error: { status: number }) => {
          status = error.status;
        },
      });

      const request = http.expectOne(`${API_BASE_URL}/${USER_ID}/username`);
      request.flush('Duplicate entry', { status: 500, statusText: 'Server Error' });

      // Assert
      expect(status).toBe(500);
    });
  });

  describe('sendPasswordResetLink', () => {
    beforeEach(() => flushCurrentUser());

    // 9. POSITIVE - Hits the existing v1 forgot-password endpoint.
    it('posts to the forgot password endpoint', () => {
      // Act
      service.sendPasswordResetLink('jean@example.com').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/forgot_password`);
      expect(request.request.method).toBe('POST');
      request.flush(null);
    });

    // 10. POSITIVE - Carries the email and the customer portal reset link.
    // The link is built from `environment.customerPortal` rather than hard-coded, so it follows
    // the build (localhost in development, the real host in production).
    it('sends the email with the customer portal reset link', () => {
      // Act
      service.sendPasswordResetLink('jean@example.com').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/forgot_password`);
      expect(request.request.body).toEqual({
        email: 'jean@example.com',
        url: `${environment.customerPortal}/users/reset-password/`,
      });
      request.flush(null);
    });
  });
});
