import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { required, form, FormField } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LoginRequest } from '@features/auth/models/loginRequest';
import { Auth } from '@features/auth/services/auth';
import { isApiError } from '@core/models/api-error';

@Component({
  selector: 'app-login',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    FormField,
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginPage {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly hidePassword = signal(true);
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly model = signal<LoginRequest>({ emailOrUsername: '', password: '' });

  protected readonly loginForm = form(this.model, (schemaPath) => {
    required(schemaPath.emailOrUsername, { message: 'Username or email is required' });
    required(schemaPath.password, { message: 'Password is required' });
  });

  protected togglePasswordVisibility(): void {
    this.hidePassword.update((hidden) => !hidden);
  }

  protected submit(event: Event): void {
    event.preventDefault();

    if (this.loginForm().invalid()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.model()).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigateByUrl('/');
      },
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(isApiError(err) ? err.message : 'Could not reach the server.');
      },
    });
  }
}
