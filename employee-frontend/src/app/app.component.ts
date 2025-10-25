import { Component, OnInit } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NgOptimizedImage,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  title = 'employee-frontend';
  progressValue = 0;

  ngOnInit(): void {
    const animateProgress = (): void => {
      const startTime = Date.now();
      const duration = 3000; 
      const targetValue = 75;

      const updateProgress = (): void => {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        const easeOutCubic = 1 - Math.pow(1 - progress, 3);
        this.progressValue = Math.round(easeOutCubic * targetValue);

        if (progress < 1) {
          window.requestAnimationFrame(updateProgress);
        }
      };

      window.requestAnimationFrame(updateProgress);
    };

    window.setTimeout(animateProgress, 500);
  }
}
