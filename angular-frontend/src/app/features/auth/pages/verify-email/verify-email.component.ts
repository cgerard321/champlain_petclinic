import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="d-flex justify-content-center">
      <div>
        <h2 class="text-center">E-mail Verification</h2>
        <h3 class="text-danger text-center" *ngFor="let error of errorMessages">{{ error }}</h3>
      </div>
    </div>
  `,
  styles: []
})
export class VerifyEmailComponent implements OnInit {
  private authApi = inject(AuthApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  errorMessages: string[] = [];

  ngOnInit(): void {
    this.verifyEmail();
  }

  verifyEmail(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'] || '';

      if (!token) {
        this.errorMessages = ['Invalid verification link'];
        return;
      }

      this.authApi.verifyEmail(token).subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: (error) => {
          const errorMsg = error.error?.message || error.error?.error || 'Email verification failed. The link may be invalid or expired.';
          this.errorMessages = errorMsg.split('\n');
        }
      });
    });
  }
}


