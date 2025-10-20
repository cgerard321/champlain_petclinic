import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [],
  template: `
    <div class="container">
      <div class="row">
        <div class="col-12 text-center">
          <img src="/images/spring-pivotal-logo.png" alt="Sponsored by Pivotal" />
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class FooterComponent {}
