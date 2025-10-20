import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../features/auth/models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <a class="navbar-brand" href="#">PetClinic</a>
      <button class="navbar-toggler" type="button" (click)="toggleNavbar()">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div
        class="collapse navbar-collapse justify-content-between"
        [class.show]="isNavbarOpen"
        id="navbarSupportedContent"
      >
        <ul class="navbar-nav mr-auto" *ngIf="currentUser">
          <li class="nav-item active">
            <a class="nav-link" routerLink="/">Home <span class="sr-only">(current)</span></a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/vets">Veterinarians</a>
          </li>
          <li
            class="nav-item dropdown"
            (mouseenter)="isDropdownOpen = true"
            (mouseleave)="isDropdownOpen = false"
          >
            <a
              class="nav-link dropdown-toggle"
              href="#"
              id="navbarDropdown"
              role="button"
              [attr.data-toggle]="'dropdown'"
              [attr.aria-haspopup]="true"
              [attr.aria-expanded]="isDropdownOpen"
            >
              Owners
            </a>
            <div
              class="dropdown-menu"
              [class.show]="isDropdownOpen"
              aria-labelledby="navbarDropdown"
            >
              <a class="nav-link" routerLink="/owners">Owners</a>
              <a class="nav-link" [routerLink]="['/owners', currentUser?.userId]">Edit Account</a>
              <a class="nav-link" routerLink="/pet-types">Pet Types</a>
            </div>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/bills">Bills</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/visit-list">Visits</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/inventories">Inventory</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/products">Products</a>
          </li>
        </ul>
        <ul class="navbar-nav mr-auto justify-content-end" *ngIf="currentUser">
          <li class="nav-item">
            <a class="nav-link" routerLink="/">Welcome back {{ currentUser?.username }}!</a>
          </li>
          <li class="nav-item" *ngIf="isAdmin">
            <a class="nav-link" routerLink="/admin-panel">Admin-Panel</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" (click)="logout()" style="cursor:pointer">Logout</a>
          </li>
        </ul>
        <ul class="navbar-nav mr-auto justify-content-end" *ngIf="!currentUser">
          <li class="nav-item">
            <a class="nav-link" routerLink="/signup">Signup</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" routerLink="/login">Login</a>
          </li>
        </ul>
      </div>
    </nav>
  `,
  styles: [],
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  isAdmin = false;
  isNavbarOpen = false;
  isDropdownOpen = false;

  public authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAdmin = this.authService.isAdmin();
    });
  }

  toggleNavbar(): void {
    this.isNavbarOpen = !this.isNavbarOpen;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
