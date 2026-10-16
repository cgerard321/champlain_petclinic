import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatToolbarModule } from '@angular/material/toolbar';

import { ThemeService } from '@features/settings/services/themeservice';

@Component({
  selector: 'app-display',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatSlideToggleModule],
  templateUrl: './display.html',
  styleUrl: './display.css',
})
export class Display {
  protected themeService = inject(ThemeService);
}
