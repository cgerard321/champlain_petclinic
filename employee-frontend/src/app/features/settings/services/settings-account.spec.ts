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

    service = TestBed.inject(SettingsAccount);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('getCurrentUser', () => {
    // 1. POSITIVE
    it('reads the signed-in employee from the token endpoint', () => {
      // Act
      service.getCurrentUser().subscribe((user) => {
        // Assert
        expect(user).toEqual(CURRENT_USER);
      });

      const request = http.expectOne(`${API_BASE_URL}/jwt`);
      expect(request.request.method).toBe('GET');
      request.flush(CURRENT_USER);
    });
  });

  describe('updateUsername', () => {
    // 2. POSITIVE
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

    // 3. POSITIVE - The body is a bare string, not JSON.
    it('sends the new name as a bare string', () => {
      // Act
      service.updateUsername(USER_ID, 'jean_dupont').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/${USER_ID}/username`);
      expect(request.request.body).toBe('jean_dupont');
      expect(typeof request.request.body).toBe('string');
      request.flush('jean_dupont');
    });

    // 4. POSITIVE - Plain text in, plain text out.
    it('declares a plain text body and reads a plain text answer', () => {
      // Act
      service.updateUsername(USER_ID, 'jean_dupont').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/${USER_ID}/username`);
      expect(request.request.headers.get('Content-Type')).toBe('text/plain');
      expect(request.request.responseType).toBe('text');
      request.flush('jean_dupont');
    });

    // 5. NEGATIVE - A rejected name surfaces as an error the caller can handle.
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
    // 6. POSITIVE - Hits the existing v1 forgot-password endpoint.
    it('posts to the forgot password endpoint', () => {
      // Act
      service.sendPasswordResetLink('jean@example.com').subscribe();

      // Assert
      const request = http.expectOne(`${API_BASE_URL}/forgot_password`);
      expect(request.request.method).toBe('POST');
      request.flush(null);
    });

    // 7. POSITIVE - Carries the email and the customer portal reset link.
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
