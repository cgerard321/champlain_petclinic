import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { SignupRequest } from '../../models/user.model';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="display: flex; justify-content: center;">
      <div style="width: 400px;">
        <h2 class="text-center">Signup</h2>

        <div class="alert alert-danger alert-dismissible text-center" role="alert" *ngFor="let error of errorMessages">
          <a class="close" aria-label="close" (click)="clearErrorMessages()">&times;</a>
          <strong>Error:</strong> {{ error }}
        </div>

        <form class="form-horizontal" #signupForm="ngForm" (ngSubmit)="signup()">
          <div class="form-group">
            <label for="firstName">First Name</label>
            <input 
              id="firstName" 
              class="form-control" 
              [(ngModel)]="signupData.firstName" 
              name="firstName" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.firstName" class="help-block">First name is required.</span>
          </div>

          <div class="form-group">
            <label for="lastName">Last Name</label>
            <input 
              id="lastName" 
              class="form-control" 
              [(ngModel)]="signupData.lastName" 
              name="lastName" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.lastName" class="help-block">Last name is required.</span>
          </div>

          <div class="form-group">
            <label for="address">Address</label>
            <input 
              id="address" 
              class="form-control" 
              [(ngModel)]="signupData.address" 
              name="address" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.address" class="help-block">Address is required.</span>
          </div>

          <div class="form-group">
            <label for="city">City</label>
            <input 
              id="city" 
              class="form-control" 
              [(ngModel)]="signupData.city" 
              name="city" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.city" class="help-block">City is required.</span>
          </div>

          <div class="form-group">
            <label for="province">Province</label>
            <input 
              id="province" 
              class="form-control" 
              [(ngModel)]="signupData.province" 
              name="province" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.province" class="help-block">Province is required.</span>
          </div>

          <div class="form-group">
            <label for="telephone">Telephone</label>
            <input 
              id="telephone" 
              class="form-control" 
              [(ngModel)]="signupData.telephone" 
              name="telephone" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.telephone" class="help-block">Telephone is required.</span>
          </div>

          <div class="form-group">
            <label for="username">Username</label>
            <input 
              id="username" 
              class="form-control" 
              [(ngModel)]="signupData.username" 
              name="username" 
              [ngModelOptions]="{standalone: true}"
              maxlength="50" 
              pattern="^[\\x20-\\x7F]+$" 
              required />
            <span *ngIf="!signupData.username" class="help-block">Username is required.</span>
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <div class="input-group">
              <input 
                id="password" 
                class="form-control" 
                [type]="showPassword ? 'text' : 'password'" 
                [(ngModel)]="signupData.password" 
                name="password" 
                [ngModelOptions]="{standalone: true}"
                (ngModelChange)="updatePasswordStrength()"
                required />
              <span class="input-group-addon" (click)="togglePasswordVisibility()" style="cursor: pointer;">
                <i [class]="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
              </span>
            </div>
            <span *ngIf="!signupData.password" class="help-block">Password is required.</span>
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
              [(ngModel)]="signupData.email" 
              name="email" 
              [ngModelOptions]="{standalone: true}"
              required />
            <span *ngIf="!signupData.email" class="help-block">Email is required.</span>
          </div>

          <div style="padding-left: 42%" *ngIf="isLoading">
            <div class="loader m-2"></div>
          </div>

          <div class="form-group text-center">
            <button class="btn btn-primary" type="submit" [disabled]="signupForm.invalid || isLoading">
              Submit
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .loader {
      border: 8px solid #f3f3f3;
      border-top: 8px solid #005d9a;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
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
  `]
})
export class SignupComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  signupData: SignupRequest & {
    firstName: string;
    lastName: string;
    address: string;
    city: string;
    province: string;
    telephone: string;
  } = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: ''
  };

  errorMessages: string[] = [];
  isLoading = false;
  showPassword = false;
  passwordStrength = 0;
  strengthText = '';

  signup(): void {
    this.errorMessages = [];
    this.isLoading = true;

    const requestData = {
      userId: '',
      email: this.signupData.email,
      username: this.signupData.username,
      password: this.signupData.password,
      defaultRole: 'OWNER',
      owner: {
        ownerId: '',
        firstName: this.signupData.firstName,
        lastName: this.signupData.lastName,
        address: this.signupData.address,
        city: this.signupData.city,
        province: this.signupData.province,
        telephone: this.signupData.telephone
      }
    };

    this.authService.signup(requestData).subscribe({
      next: () => {
        this.isLoading = false;
        alert('Signup successful! Please check your email to verify your account.');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.isLoading = false;
        const errorMsg = error.error?.message || error.error?.error || 'Signup failed. Please try again.';
        this.errorMessages = errorMsg.split('\n');
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  updatePasswordStrength(): void {
    const password = this.signupData.password;
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
