import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  imports: [],
  selector: 'app-forbidden',
  styleUrl: './forbidden.css',
  templateUrl: './forbidden.html',
})
export class Forbidden {
  private readonly router = inject(Router);

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token') || !!sessionStorage.getItem('token');
  }

  handleAction(event: Event) {
    event.preventDefault();

    if (this.isLoggedIn) {
      this.router.navigate(['/home']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
