import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { catchError, of } from 'rxjs';

// Core Model Import
import { CurrentUserResponse } from '@core/models/current-user-response';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  private readonly http = inject(HttpClient);

  /**
   * Reactively fetches current user credentials from gateway API.
   */
  private readonly currentUser = toSignal(
    this.http.get<CurrentUserResponse>('/api/gateway/users/jwt').pipe(catchError(() => of(null))),
    { initialValue: null },
  );

  /**
   * Derives employee display name, falling back to null for generic greeting.
   */
  protected readonly employeeName = computed(() => {
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
