import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent],
  template: `
    <app-navbar></app-navbar>
    <div class="container-fluid">
      <div class="content">
        <router-outlet></router-outlet>
      </div>
    </div>
    <app-footer></app-footer>
  `,
  styles: [],
})
export class AppComponent {
  title = 'PetClinic :: a Spring Framework demonstration';
}
