import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { UserDetails } from '../../models/user.model';

@Component({
  selector: 'app-role-update',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <h2>Update User Role</h2>
    <form style="background-color: #f5f5f5; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);">
      <div class="form-group">
        <label>Select Role:</label>
        <div *ngFor="let role of availableRoles" class="form-check">
          <input 
            type="checkbox" 
            class="form-check-input" 
            [id]="role"
            [(ngModel)]="selectedRoles[role]"
            [name]="role"
            [ngModelOptions]="{standalone: true}">
          <label class="form-check-label" [for]="role">{{ role }}</label>
        </div>
      </div>
      <button type="button" class="btn btn-warning" (click)="updateRole()">
        Update Roles
      </button>
      <a routerLink="/admin-panel" class="btn btn-secondary">Cancel</a>
    </form>
  `,
  styles: []
})
export class RoleUpdateComponent implements OnInit {
  private authApi = inject(AuthApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  userId: string = '';
  user: UserDetails | null = null;
  availableRoles: string[] = ['ADMIN', 'USER', 'VET', 'INVENTORY_MANAGER'];
  selectedRoles: { [key: string]: boolean } = {};

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = params['userId'];
      this.loadUserDetails();
    });
  }

  loadUserDetails(): void {
    this.authApi.getUserDetails(this.userId).subscribe({
      next: (user: any) => {
        this.user = user;
        this.availableRoles.forEach(role => {
          this.selectedRoles[role] = user.roles.some((r: any) => r.name === role);
        });
      },
      error: (_error: any) => {
      }
    });
  }

  updateRole(): void {
    if (!this.user) return;

    const currentRoleNames = this.user.roles.map(r => r.name);
    const selectedRoleNames = this.availableRoles.filter(role => this.selectedRoles[role]);

    const rolesToAdd = selectedRoleNames.filter(role => !currentRoleNames.includes(role));
    const rolesToRemove = currentRoleNames.filter(role => !selectedRoleNames.includes(role));

    const updates: Promise<any>[] = [];

    rolesToAdd.forEach(roleName => {
      const newRoles = [...currentRoleNames, roleName];
      updates.push(
        this.authApi.updateUserRole(this.userId, newRoles).toPromise()
      );
    });

    rolesToRemove.forEach(roleName => {
      const newRoles = currentRoleNames.filter(role => role !== roleName);
      updates.push(
        this.authApi.updateUserRole(this.userId, newRoles).toPromise()
      );
    });

    if (updates.length === 0) {
      alert('No role changes detected');
      return;
    }

    Promise.all(updates).then(
      () => {
        alert('User roles updated successfully!');
        this.router.navigate(['/admin-panel']);
      },
      (error) => {
        const errorMsg = error.error?.message || error.error?.error || 'Failed to update user roles.';
        alert('Failed to update roles. ' + errorMsg);
      }
    );
  }
}

