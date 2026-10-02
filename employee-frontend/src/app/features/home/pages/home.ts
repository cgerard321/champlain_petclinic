import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import { CurrentUserResponse } from '@core/models/current-user-response';
import { AuthState } from '@core/services/auth-state';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, MatCardModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  private readonly http = inject(HttpClient);

  protected readonly auth = inject(AuthState);

  protected readonly errorMessage = signal<string | null>(null);

  private readonly currentUser = toSignal(
    this.http.get<CurrentUserResponse>('/api/gateway/users/jwt').pipe(
      catchError((error) => {
        console.error('Failed to fetch current user credentials:', error);
        this.errorMessage.set('Failed to load user information. Please try again later.');
        return of(null);
      }),
    ),
    { initialValue: null },
  );

  protected readonly employeeName = computed((): string | null => {
    const user = this.currentUser();
    if (!user) return null;

    if (user.username && user.username !== 'admin@admin.com' && !user.username.includes('@')) {
      return user.username;
    }

    if (user.email && user.email !== 'admin@admin.com') {
      const namePart = user.email.split('@')[0];
      if (namePart) {
        return namePart.charAt(0).toUpperCase() + namePart.slice(1);
      }
    }

    return null;
  });
}
