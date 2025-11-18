import { Component, OnInit } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NgOptimizedImage,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatButtonModule,
    NavbarComponent,
    FooterComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  title = 'employee-frontend';
  progressValue = 25;

  ngOnInit(): void {
    const animateProgress = (): void => {
      const startTime = Date.now();
      const duration = 3000;
      const targetValue = 25;

      const updateProgress = (): void => {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);

        const easeOutCubic = 1 - Math.pow(1 - progress, 3);
        this.progressValue = Math.round(easeOutCubic * targetValue);

        if (progress < 1) {
          requestAnimationFrame(updateProgress);
        }
      };

      requestAnimationFrame(updateProgress);
    };

    setTimeout(animateProgress, 500);
  }
}
