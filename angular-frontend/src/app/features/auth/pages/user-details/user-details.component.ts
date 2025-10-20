import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { User, UserDetails } from '../../models/user.model';

@Component({
  selector: 'app-user-details',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="user-details">
      <h3>User Details for ID: {{ userId }}</h3>
      <p><strong>Username:</strong> {{ user ? user.username : 'N/A' }}</p>
      <p><strong>Email:</strong> {{ user ? user.email : 'N/A' }}</p>
      <p>
        <strong>Roles:</strong>
        <span *ngFor="let role of user?.roles; let last = last">
          {{ role.name }}<span *ngIf="!last">, </span>
        </span>
      </p>
      <button class="btn btn-primary" (click)="goToAdminPanel()">Back</button>
    </div>
  `,
  styles: [
    `
      .user-details {
        font-family: Arial, sans-serif;
        color: #333;
      }

      .user-details h3 {
        color: #007bff;
      }

      .user-details p {
        margin-bottom: 10px;
      }
    `,
  ],
})
export class UserDetailsComponent implements OnInit {
  private authApi = inject(AuthApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  user: UserDetails | null = null;
  userId: string = '';

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = params['userId'];
      this.loadUserDetails();
    });
  }

  loadUserDetails(): void {
    this.authApi.getUserDetails(this.userId).subscribe({
      next: (user: User) => {
        this.user = user as UserDetails;
      },
      error: () => {},
    });
  }

  goToAdminPanel(): void {
    this.router.navigate(['/admin-panel']);
  }
}
