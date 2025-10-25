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

  ngOnInit() {
    // Animate progress bar from 0 to 75 over 3 seconds
    const animateProgress = () => {
      const startTime = Date.now();
      const duration = 3000; // 3 seconds
      const targetValue = 75;

      const updateProgress = () => {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // Use easeOutCubic for smooth animation
        const easeOutCubic = 1 - Math.pow(1 - progress, 3);
        this.progressValue = Math.round(easeOutCubic * targetValue);

        if (progress < 1) {
          requestAnimationFrame(updateProgress);
        }
      };

      requestAnimationFrame(updateProgress);
    };

    // Start animation after a short delay
    setTimeout(animateProgress, 500);
  }
}
