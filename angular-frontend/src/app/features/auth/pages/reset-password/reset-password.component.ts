import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { ResetPasswordRequest } from '../../models/user.model';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="display: flex; justify-content: center; padding: 40px 20px">
      <div style="width: 100%; max-width: 25em">
        <h2 class="text-center" style="margin-bottom: 30px">Reset Your Password</h2>
        
        <form #resetForm="ngForm" (ngSubmit)="resetPassword()" style="max-width: 25em; margin: 0 auto">
          <div class="alert alert-danger alert-dismissible text-center" role="alert" *ngFor="let error of errorMessages">
            <a class="close" aria-label="close" (click)="clearErrorMessages()" style="cursor: pointer">&times;</a>
            <strong>Error:</strong> {{ error }}
          </div>

          <div class="group-form" style="margin-bottom: 20px">
            <label for="pwd">New Password</label>
            <input 
              id="pwd" 
              [type]="showPassword ? 'text' : 'password'" 
              class="form-control" 
              [(ngModel)]="resetPasswordData.newPassword" 
              (ngModelChange)="updatePasswordStrength()"
              name="password" 
              [ngModelOptions]="{standalone: true}"
              required/>
            <i 
              (click)="togglePasswordVisibility()" 
              style="margin-left: 180px; margin-top: -700px; cursor: pointer" 
              [class]="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'">
            </i>
            <div [class]="'password-strength strength-' + passwordStrength">
              {{ strengthText }}
            </div>
            <span class="help-block">Password is required.</span>
          </div>

          <div class="group-form d-flex justify-content-center" style="margin-top: 30px">
            <button id="button" class="btn btn-default" type="submit" [disabled]="resetForm.invalid">
              Reset Password
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    label {
      font-weight: bold;
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
  `]
})
export class ResetPasswordComponent implements OnInit {
  private authApi = inject(AuthApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  resetPasswordData: ResetPasswordRequest = {
    token: '',
    newPassword: ''
  };

  errorMessages: string[] = [];
  showPassword = false;
  passwordStrength = 0;
  strengthText = '';

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.resetPasswordData.token = params['token'] || '';
    });
  }

  resetPassword(): void {
    this.errorMessages = [];

    this.authApi.resetPassword(this.resetPasswordData).subscribe({
      next: () => {
        alert('Password was reset !');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.error?.error || 'Failed to reset password. Please try again.';
        this.errorMessages = errorMsg.split('\n');
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  updatePasswordStrength(): void {
    const password = this.resetPasswordData.newPassword;
    this.passwordStrength = this.calculatePasswordStrength(password);
    this.strengthText = this.getStrengthText(this.passwordStrength);
  }

  calculatePasswordStrength(password: string): number {
    const pattern = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*!()_.<>,{}])(?=\S+$).{8,}$/;

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
        return "Weak";
      case 2:
        return "Medium";
      case 3:
        return "Strong";
      default:
        return "";
    }
  }

  clearErrorMessages(): void {
    this.errorMessages = [];
  }
}

