import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateUserRequest } from '@features/create-users/models/create-user-request';

@Injectable({ providedIn: 'root' })
export class CreateUsers {
  private readonly http = inject(HttpClient);

  createUser(request: CreateUserRequest): Observable<void> {
    return this.http.post<void>('/users', request, { withCredentials: true });
  }
}
