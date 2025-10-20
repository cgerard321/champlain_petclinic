import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';

@Component({
  selector: 'app-manager-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="display: flex; justify-content: center;">
      <div style="width: 400px;">
        <h2 class="text-center">Create Inventory Manager</h2>

        <div
          class="alert alert-danger alert-dismissible text-center"
          role="alert"
          *ngFor="let error of errorMessages"
        >
          <a class="close" aria-label="close" (click)="clearErrorMessages()">&times;</a>
          <strong>Error:</strong> {{ error }}
        </div>

        <form class="form-horizontal" #managerForm="ngForm" (ngSubmit)="createManager()">
          <div class="form-group">
            <label for="username">Username</label>
            <input
              id="username"
              class="form-control"
              [(ngModel)]="managerData.username"
              name="username"
              [ngModelOptions]="{ standalone: true }"
              maxlength="50"
              pattern="^[\\x20-\\x7F]+$"
              required
            />
            <span *ngIf="!managerData.username" class="help-block">Username is required.</span>
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <div class="input-group">
              <input
                id="password"
                class="form-control"
                [type]="showPassword ? 'text' : 'password'"
                [(ngModel)]="managerData.password"
                name="password"
                [ngModelOptions]="{ standalone: true }"
                (ngModelChange)="updatePasswordStrength()"
                required
              />
              <span
                class="input-group-addon"
                (click)="togglePasswordVisibility()"
                style="cursor: pointer;"
              >
                <i [class]="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
              </span>
            </div>
            <span *ngIf="!managerData.password" class="help-block">Password is required.</span>
            <div [class]="'password-strength strength-' + passwordStrength">
              {{ strengthText }}
            </div>
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              class="form-control"
              type="email"
              [(ngModel)]="managerData.email"
              name="email"
              [ngModelOptions]="{ standalone: true }"
              required
            />
            <span *ngIf="!managerData.email" class="help-block">Email is required.</span>
          </div>

          <div style="padding-left: 42%" *ngIf="isLoading">
            <div class="loader m-2"></div>
          </div>

          <div class="form-group text-center">
            <button
              class="btn btn-primary"
              type="submit"
              [disabled]="managerForm.invalid || isLoading"
            >
              Submit
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [
    `
      .loader {
        border: 8px solid #f3f3f3;
        border-top: 8px solid #005d9a;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        animation: spin 1s linear infinite;
      }

      @keyframes spin {
        0% {
          transform: rotate(0deg);
        }
        100% {
          transform: rotate(360deg);
        }
      }

      .form-group {
        margin-bottom: 20px;
      }

      label {
        font-weight: bold;
      }

      .help-block {
        color: red;
      }

      .password-strength {
        font-weight: bold;
        margin-top: 5px;
      }

      .strength-1 {
        color: red;
      }

      .strength-2 {
        color: orange;
      }

      .strength-3 {
        color: green;
      }
    `,
  ],
})
export class ManagerFormComponent {
  private authApi = inject(AuthApiService);
  private router = inject(Router);

  managerData = {
    username: '',
    email: '',
    password: '',
  };

  errorMessages: string[] = [];
  isLoading = false;
  showPassword = false;
  passwordStrength = 0;
  strengthText = '';

  createManager(): void {
    this.errorMessages = [];
    this.isLoading = true;

    this.authApi.createInventoryManager(this.managerData).subscribe({
      next: () => {
        this.isLoading = false;
        alert(
          'Inventory manager created successfully! Please check your email to verify the account.'
        );
        this.router.navigate(['/admin-panel']);
      },
      error: error => {
        this.isLoading = false;
        const errorMsg =
          error.error?.message ||
          error.error?.error ||
          'Failed to create inventory manager. Please try again.';
        this.errorMessages = errorMsg.split('\n');
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  updatePasswordStrength(): void {
    const password = this.managerData.password;
    this.passwordStrength = this.calculatePasswordStrength(password);
    this.strengthText = this.getStrengthText(this.passwordStrength);
  }

  calculatePasswordStrength(password: string): number {
    const pattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/;

    if (pattern.test(password)) {
      return 3;
    } else if (password.length >= 8) {
      return 2;
    } else if (password.length > 0) {
      return 1;
    }
    return 0;
  }

  getStrengthText(strength: number): string {
    switch (strength) {
      case 1:
        return 'Weak';
      case 2:
        return 'Medium';
      case 3:
        return 'Strong';
      default:
        return '';
    }
  }

  clearErrorMessages(): void {
    this.errorMessages = [];
  }
}
