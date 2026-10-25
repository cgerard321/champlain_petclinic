import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable, switchMap, throwError, toArray } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import {
  User,
  UserChanges,
  UserCreateRequest,
  UserUpdateRequest,
  userRoleName,
} from '@features/users/models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);

  // The list is streamed by the v1 gateway (SSE), like the inventories.
  private readonly listUrl = '/api/gateway/users';

  // Editing and deleting use the v2 endpoints, because v1 has no way to edit an email.
  private readonly baseUrl = '/api/v2/gateway/users';

  // Creating users uses the v1 gateway endpoints, one per role.
  private readonly createUrls: Record<string, string> = {
    INVENTORY_MANAGER: '/api/gateway/users/inventoryManager',
    RECEPTIONIST: '/api/gateway/users/receptionist',
    VET: '/api/gateway/users/vets',
    CUST: '/api/gateway/users',
  };

  // A new vet is available from Monday to Friday. An administrator can change this
  // later on the vets page.
  private readonly defaultVetWorkdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

  getUsers(): Observable<User[]> {
    return this.sse
      .stream(this.listUrl, { keepAlive: false, responseType: 'event' }, {}, 'GET')
      .pipe(
        filter((event): event is MessageEvent => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as User),
        toArray(),
      );
  }

  // The backend saves username/email with PUT and the role with a separate PATCH.
  updateUser(user: User, changes: UserChanges): Observable<unknown> {
    const request: UserUpdateRequest = {
      userId: user.userId,
      username: changes.username,
      email: changes.email,
      roles: user.roles,
    };
    const update$ = this.http.put<UserUpdateRequest>(`${this.baseUrl}/${user.userId}`, request);

    if (changes.role === userRoleName(user)) {
      return update$;
    }

    return update$.pipe(
      switchMap(() => this.http.patch(`${this.baseUrl}/${user.userId}`, { roles: [changes.role] })),
    );
  }

  // Only deletes the account. The vet or customer profile of the user is not deleted.
  deleteUser(userId: string): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/${userId}`);
  }

  createUser(request: UserCreateRequest): Observable<unknown> {
    const url = this.createUrls[request.role];

    if (!url) {
      return throwError(() => ({
        code: 'NOT_SUPPORTED',
        message: 'Creating this type of user is not available.',
      }));
    }

    const account = {
      username: request.username,
      email: request.email,
      password: request.password,
    };

    if (request.role === 'VET') {
      return this.http.post(
        url,
        {
          ...account,
          vet: {
            firstName: request.firstName,
            lastName: request.lastName,
            phoneNumber: request.phoneNumber,
            resume: request.resume,
            specialties: [],
            workday: this.defaultVetWorkdays,
            active: true,
            photoDefault: true,
          },
        },
        { responseType: 'text' as 'json' },
      );
    }

    if (request.role === 'CUST') {
      // The gateway still calls the customer profile "owner"
      return this.http.post(
        url,
        {
          ...account,
          owner: {
            firstName: request.firstName,
            lastName: request.lastName,
            address: request.address,
            city: request.city,
            province: request.province,
            telephone: request.telephone,
          },
        },
        { responseType: 'text' as 'json' },
      );
    }

    return this.http.post(url, account, { responseType: 'text' as 'json' });
  }
}
