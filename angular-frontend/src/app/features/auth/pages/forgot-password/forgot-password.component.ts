import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { ForgotPasswordRequest } from '../../models/user.model';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div>
        <h2 class="text-center">Forgot Password</h2>
      </div>

      <form #forgotForm="ngForm" (ngSubmit)="sendResetEmail()" method="post" style="max-width: 420px; margin: 0 auto;">
        <div class="border border-secondary rounded p-3">
          <div>
            <p>We will be sending a reset password link to your email.</p>
          </div>
          <div class="text-center">
            <p>
              <label>
                <input 
                  id="email" 
                  type="email" 
                  class="form-control" 
                  [(ngModel)]="forgotPasswordData.email" 
                  name="email" 
                  [ngModelOptions]="{standalone: true}"
                  required/>
              </label>
            </p>
            <div style="padding-left: 40%" *ngIf="isLoading">
              <div class="loader m-2"></div>
            </div>
            <p class="text-center">
              <input 
                type="submit" 
                value="Send" 
                class="btn btn-primary" 
                [disabled]="forgotForm.invalid || isLoading" />
            </p>
          </div>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .loader {
      border: 16px solid #f3f3f3;
      border-top: 16px solid #3498db;
      border-radius: 50%;
      width: 60px;
      height: 60px;
      animation: spin 2s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class ForgotPasswordComponent {
  private authApi = inject(AuthApiService);
  private router = inject(Router);

  forgotPasswordData: ForgotPasswordRequest = {
    email: ''
  };

  isLoading = false;

  sendResetEmail(): void {
    this.isLoading = true;

    this.authApi.forgotPassword(this.forgotPasswordData).subscribe({
      next: () => {
        this.isLoading = false;
        alert('Email was sent !');
        this.router.navigate(['/welcome']);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMsg = error.error?.message || error.error?.error || 'Failed to send reset email. Please try again.';
        alert('Email was not sent !, please try again!\n' + errorMsg);
      }
    });
  }
}

