import { HttpErrorResponse, HttpEventType, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { User } from '@features/users/models/user.model';

import { UserService } from './user-service';

const API_BASE_URL = '/api/v2/gateway/users';
const LIST_URL = '/api/gateway/users';

describe('UserService', () => {
  let service: UserService;
  let http: HttpTestingController;

  const vetUser: User = {
    userId: 'user-1',
    username: 'dr_vet',
    email: 'vet@petclinic.com',
    roles: [{ id: 2, name: 'VET' }],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(UserService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('getUsers', () => {
    const managerUser: User = {
      userId: 'user-2',
      username: 'stock_boss',
      email: 'stock@petclinic.com',
      roles: [{ id: 3, name: 'INVENTORY_MANAGER' }],
    };

    it('should collect the streamed users once the SSE response completes', () => {
      let result: User[] | undefined;

      service.getUsers().subscribe((users) => {
        result = users;
      });

      const request = http.expectOne(LIST_URL);

      expect(request.request.method).toBe('GET');
      expect(request.request.responseType).toBe('text');

      const firstEvent = `data:${JSON.stringify(vetUser)}\n\n`;

      request.event({
        type: HttpEventType.DownloadProgress,
        loaded: firstEvent.length,
        partialText: firstEvent,
      });

      expect(result).toBeUndefined();
      // The final response repeats the progress text; the first user must not be counted twice.
      request.flush(`${firstEvent}data:${JSON.stringify(managerUser)}\n\n`);
      expect(result).toEqual([vetUser, managerUser]);
    });

    it('should return an empty array when there are no users', () => {
      let result: User[] | undefined;

      service.getUsers().subscribe((users) => {
        result = users;
      });

      http.expectOne(LIST_URL).flush('');

      expect(result).toEqual([]);
    });

    it('should ignore SSE lines that are not data events', () => {
      let result: User[] | undefined;

      service.getUsers().subscribe((users) => {
        result = users;
      });

      http
        .expectOne(LIST_URL)
        .flush(`: heartbeat\n\nevent:message\ndata:${JSON.stringify(vetUser)}\nretry:1000\n\n`);

      expect(result).toEqual([vetUser]);
    });

    it('should propagate an HTTP error when loading the users fails', () => {
      let receivedError: HttpErrorResponse | undefined;

      service.getUsers().subscribe({
        error: (error: HttpErrorResponse) => {
          receivedError = error;
        },
      });

      http.expectOne(LIST_URL).flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError?.status).toBe(500);
    });
  });

  it('should only send a PUT when the role did not change', () => {
    service
      .updateUser(vetUser, { username: 'new_name', email: 'new@petclinic.com', role: 'VET' })
      .subscribe();

    const request = http.expectOne(`${API_BASE_URL}/user-1`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      userId: 'user-1',
      username: 'new_name',
      email: 'new@petclinic.com',
      roles: vetUser.roles,
    });
    request.flush({});
  });

  it('should send a PATCH with the new role after the PUT when the role changed', () => {
    service
      .updateUser(vetUser, { username: 'dr_vet', email: 'vet@petclinic.com', role: 'RECEPTIONIST' })
      .subscribe();

    http.expectOne(`${API_BASE_URL}/user-1`).flush({});

    const patch = http.expectOne(`${API_BASE_URL}/user-1`);
    expect(patch.request.method).toBe('PATCH');
    expect(patch.request.body).toEqual({ roles: ['RECEPTIONIST'] });
    patch.flush({});
  });

  it('should not send the PATCH when the PUT fails', () => {
    let failed = false;

    service
      .updateUser(vetUser, { username: 'dr_vet', email: 'vet@petclinic.com', role: 'RECEPTIONIST' })
      .subscribe({ error: () => (failed = true) });

    http
      .expectOne(`${API_BASE_URL}/user-1`)
      .flush({ message: 'Nope' }, { status: 400, statusText: 'Bad Request' });

    expect(failed).toBe(true);
  });

  it('should delete a user with the v2 endpoint', () => {
    service.deleteUser('user-1').subscribe();

    const request = http.expectOne(`${API_BASE_URL}/user-1`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('should report an error when the user cannot be deleted', () => {
    let failed = false;

    service.deleteUser('user-1').subscribe({ error: () => (failed = true) });

    http
      .expectOne(`${API_BASE_URL}/user-1`)
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(failed).toBe(true);
  });

  it('should create an inventory manager with the v1 admin endpoint', () => {
    service
      .createUser({
        username: 'stock_boss',
        email: 'stock@petclinic.com',
        password: 'Passw0rd!',
        role: 'INVENTORY_MANAGER',
      })
      .subscribe();

    const request = http.expectOne('/api/gateway/users/inventoryManager');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      username: 'stock_boss',
      email: 'stock@petclinic.com',
      password: 'Passw0rd!',
    });
    request.flush({});
  });

  it('should create a receptionist with the v1 admin endpoint', () => {
    service
      .createUser({
        username: 'front_desk',
        email: 'desk@petclinic.com',
        password: 'Passw0rd!',
        role: 'RECEPTIONIST',
      })
      .subscribe();

    const request = http.expectOne('/api/gateway/users/receptionist');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      username: 'front_desk',
      email: 'desk@petclinic.com',
      password: 'Passw0rd!',
    });
    request.flush({});
  });

  it('should create a vet with the vet profile', () => {
    service
      .createUser({
        username: 'dr_new',
        email: 'new.vet@petclinic.com',
        password: 'Passw0rd!',
        role: 'VET',
        firstName: 'Nora',
        lastName: 'Vetter',
        phoneNumber: '(514) 555-0123 #1234',
        resume: 'Ten years of experience',
      })
      .subscribe();

    const request = http.expectOne('/api/gateway/users/vets');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      username: 'dr_new',
      email: 'new.vet@petclinic.com',
      password: 'Passw0rd!',
      vet: {
        firstName: 'Nora',
        lastName: 'Vetter',
        phoneNumber: '(514) 555-0123 #1234',
        resume: 'Ten years of experience',
        specialties: [],
        workday: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'],
        active: true,
        photoDefault: true,
      },
    });
    request.flush({});
  });

  it('should create an owner with the owner profile', () => {
    service
      .createUser({
        username: 'new_owner',
        email: 'owner@petclinic.com',
        password: 'Passw0rd!',
        role: 'CUST',
        firstName: 'Olivia',
        lastName: 'Owner',
        address: '1 Main Street',
        city: 'Montreal',
        province: 'Quebec',
        telephone: '5145550123',
      })
      .subscribe();

    const request = http.expectOne('/api/gateway/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      username: 'new_owner',
      email: 'owner@petclinic.com',
      password: 'Passw0rd!',
      owner: {
        firstName: 'Olivia',
        lastName: 'Owner',
        address: '1 Main Street',
        city: 'Montreal',
        province: 'Quebec',
        telephone: '5145550123',
      },
    });
    request.flush({});
  });

  it('should refuse to create a role that does not exist', () => {
    let message = '';

    service
      .createUser({ username: 'a', email: 'a@a.com', password: 'Passw0rd!', role: 'ADMIN' })
      .subscribe({ error: (error: { message: string }) => (message = error.message) });

    expect(message).toContain('not available');
  });
});
