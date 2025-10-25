import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [],
  template: `
    <div class="app-container">
      <h1>Employee Frontend</h1>
      <p>Coming Soon</p>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      background-color: #f8f9fa;
    }
    h1 {
      color: #007bff;
      margin-bottom: 1rem;
    }
    p {
      color: #6c757d;
      font-size: 1.2rem;
    }
  `]
})
export class AppComponent {
  title = 'employee-frontend';
}