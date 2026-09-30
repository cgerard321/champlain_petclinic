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
  handleAction(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/home']);
  }

}
