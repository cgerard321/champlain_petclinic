import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
type SettingsSection = 'security' | 'display';
@Component({
  selector: 'app-settings',
  imports: [MatButtonModule, MatCardModule, MatSnackBarModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly activeSection = signal<SettingsSection>('security');
  protected select(section: SettingsSection): void {
    if (section === this.activeSection()) {
      return;
    }

    this.activeSection.set(section);
  }

  protected save(): void {
    this.snackBar.open($localize`:@@settings.saved:Modifications enregistrées`, '', {
      duration: 2000,
      verticalPosition: 'top',
    });
    this.router.navigateByUrl('/home');
  }

  protected cancel(): void {
    this.router.navigateByUrl('/home');
  }
}
