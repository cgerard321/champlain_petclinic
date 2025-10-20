import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { LoginRequest } from '../../models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div style="display: flex; justify-content: center">
      <div>
        <h2 class="text-center">Login</h2>

        <form
          #loginForm="ngForm"
          (ngSubmit)="login()"
          style="max-width: 25em; justify-content: center"
        >
          <div class="alert alert-danger text-center" *ngFor="let error of errorMessages">
            <a class="close" aria-label="close" (click)="clearErrorMessages()">&times;</a>
            <strong>Error:</strong> {{ error }}
          </div>

          <div class="group-form">
            <label>Email</label>
            <input
              id="email"
              class="form-control"
              [(ngModel)]="credentials.email"
              name="email"
              [ngModelOptions]="{ standalone: true }"
              required
            />
            <span *ngIf="!credentials.email" class="help-block"> Email is required. </span>
          </div>

          <div class="group-form">
            <label>Password</label>
            <input
              id="pwd"
              [type]="showPassword ? 'text' : 'password'"
              class="form-control"
              [(ngModel)]="credentials.password"
              name="password"
              [ngModelOptions]="{ standalone: true }"
              required
            />
            <i
              id="eye"
              (click)="togglePasswordVisibility()"
              style="margin-left: 180px; margin-top: -700px; cursor: pointer"
              [class]="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"
            >
            </i>
            <span *ngIf="!credentials.password" class="help-block"> Password is required. </span>
          </div>

          <div class="group-form d-flex justify-content-center">
            <button id="button" class="btn btn-default" type="submit">Login</button>
          </div>
        </form>

        <tbody>
          <tr>
            <td>
              <a class="btn p-0 responsive-text" style="color: blue" routerLink="/forgot-password">
                Forgot Password ?
              </a>
            </td>
          </tr>
        </tbody>
      </div>
    </div>
  `,
  styles: [
    `
      label {
        font-weight: bold;
      }
    `,
  ],
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  credentials: LoginRequest = {
    email: '',
    password: '',
  };

  errorMessages: string[] = [];
  showPassword = false;

  login(): void {
    this.errorMessages = [];

    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/welcome']);
      },
      error: error => {
        const errorMsg =
          error.error?.message ||
          error.error?.error ||
          'Login failed. Please check your credentials.';
        this.errorMessages = errorMsg.split('\n');
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  clearErrorMessages(): void {
    this.errorMessages = [];
  }
}
