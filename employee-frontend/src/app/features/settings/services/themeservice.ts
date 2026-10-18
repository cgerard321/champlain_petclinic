import { DOCUMENT } from '@angular/common';
import { Injectable, signal, effect, inject } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private document = inject(DOCUMENT);
  private currentUsername: string | null = localStorage.getItem('active_user') || null;

  readonly isDarkMode = signal<boolean>(this.getInitialTheme());
  constructor() {
    this.syncTheme(false);
    effect(() => {
      this.syncTheme(this.isDarkMode());
    });
  }

  private getInitialTheme(): boolean {
    if (this.currentUsername) {
      return localStorage.getItem(`theme_${this.currentUsername}`) === 'dark';
    }
    return false;
  }
  toggleTheme(): void {
    this.isDarkMode.update((dark) => !dark);
  }

  public setCurrentUser(username: string | null): void {
    this.currentUsername = username;
    if (username) {
      localStorage.setItem('active_user', username);
      const savedTheme = localStorage.getItem(`theme_${username}`);
      this.isDarkMode.set(savedTheme === 'dark');
    } else {
      this.resetToDefault();
    }
  }

  public saveThemePreference(): void {
    if (this.currentUsername) {
      const themeStr = this.isDarkMode() ? 'dark' : 'light';
      localStorage.setItem(`theme_${this.currentUsername}`, themeStr);
    }
  }

  public resetToDefault(): void {
    this.currentUsername = null;
    localStorage.removeItem('active_user');
    this.isDarkMode.set(false);
  }
  private syncTheme(isDark: boolean): void {
    {
      const root = this.document.documentElement;
      if (isDark) {
        root.classList.add('dark-mode');
        root.classList.remove('light-mode');
      } else {
        root.classList.add('light-mode');
        root.classList.remove('dark-mode');
      }
    }
  }
}
