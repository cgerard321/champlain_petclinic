import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User, LoginRequest } from '../../features/auth/models/user.model';
import { ApiConfigService } from '../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  login(credentials: LoginRequest): Observable<{
    token: string;
    username: string;
    email: string;
    userId: string;
    roles: string[];
  }> {
    return this.http
      .post<{
        token: string;
        username: string;
        email: string;
        userId: string;
        roles: string[];
      }>(this.apiConfig.getFullUrl('/users/login'), credentials)
      .pipe(
        tap(response => {
          const userData = {
            username: response.username,
            email: response.email,
            userId: response.userId,
            roles: response.roles,
          };
          this.setUserDataFromResponse(userData);
        })
      );
  }

  signup(userData: Record<string, unknown>): Observable<unknown> {
    return this.http.post(this.apiConfig.getFullUrl('/users'), userData);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!this.getUserFromStorage();
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.roles?.some(role => role.name === 'ADMIN') || false;
  }

  getToken(): string | null {
    return null;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private setUserDataFromResponse(userData: User): void {
    localStorage.setItem('user', JSON.stringify(userData));
    this.currentUserSubject.next(userData);
  }

  private getUserFromStorage(): User | null {
    try {
      const userStr = localStorage.getItem('user');
      if (!userStr || userStr === 'undefined' || userStr === 'null') {
        return null;
      }
      return JSON.parse(userStr);
    } catch (error) {
      console.warn('Error parsing user from localStorage:', error);
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      return null;
    }
  }
}
