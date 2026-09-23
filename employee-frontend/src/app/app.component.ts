import { Component, OnInit } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
// TranslateService (ngx-translate) retire (VETS-CPC-1927) - on passe a @angular/localize

@Component({
  selector: 'app-root',
  // Nom de la balise HTML représentant ce composant (<app-root></app-root>)

  standalone: true,
  // Ce composant gère ses propres dépendances, sans passer par un NgModule

  imports: [
    NgOptimizedImage,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatButtonModule,
    NavbarComponent,
    FooterComponent,
  ],
  // Liste de tout ce que ce composant a le droit d'utiliser dans son template HTML

  templateUrl: './app.component.html',
  // Chemin vers le fichier HTML qui définit l'affichage de ce composant

  styleUrl: './app.component.scss',
  // meme chose avec css
})
export class AppComponent implements OnInit {
  title = 'employee-frontend';
  progressValue = 25;
  ngOnInit(): void {
    const animateProgress = (): void => {
      // Fonction qui gère toute la logique d'animation de la barre de progression

      const startTime = Date.now();
      // Timestamp (en ms) du moment où l'animation démarre

      const duration = 3000;
      // Durée totale voulue pour l'animation: 3000ms = 3 secondes

      const targetValue = 25;
      // Valeur finale que progressValue doit atteindre

      const updateProgress = (): void => {
        // Fonction appelée en boucle pour mettre à jour l'affichage progressivement

        const elapsed = Date.now() - startTime;
        // Temps écoulé depuis le début de l'animation

        const progress = Math.min(elapsed / duration, 1);
        // Ratio d'avancement entre 0 et 1
        // Math.min empêche de dépasser 1 (100%)

        const easeOutCubic = 1 - Math.pow(1 - progress, 3);
        // Formule d'easing: l'animation est rapide au début
        // et ralentit progressivement vers la fin (effet plus naturel)

        this.progressValue = Math.round(easeOutCubic * targetValue);
        // Calcule et arrondit la valeur actuelle de la barre de progression

        if (progress < 1) {
          requestAnimationFrame(updateProgress);
          // Si l'animation n'est pas terminée, redemande une mise à jour
          // juste avant le prochain rafraîchissement d'écran (~60fps)
        }
      };

      requestAnimationFrame(updateProgress);
    };

    setTimeout(animateProgress, 500);
    // Attend 500ms avant de démarrer l'animation pour laisser le temps a charger
  }
}
