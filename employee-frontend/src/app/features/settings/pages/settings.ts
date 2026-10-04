import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
type SettingsSection = 'security' | 'display';
@Component({
  selector: 'app-settings',
  imports: [MatButtonModule, MatCardModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
  private readonly router = inject(Router);

  protected readonly activeSection = signal<SettingsSection>('security');
  protected readonly isSaved = signal(false);
  protected select(section: SettingsSection): void {
    if (section === this.activeSection()) {
      return;
    }

    this.isSaved.set(false);
    this.activeSection.set(section);
  }

  protected save(): void {
    this.isSaved.set(true);
  }

  protected cancel(): void {
    this.router.navigateByUrl('/home');
  }
}
