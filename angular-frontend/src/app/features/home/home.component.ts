import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [],
  template: `
    <h1>Welcome to Petclinic</h1>

    <div class="row">
      <div class="col-md-12">
        <img class="img-responsive" src="/images/pets.png" alt="pets logo" />
      </div>
    </div>
  `,
  styles: [],
})
export class HomeComponent {}
