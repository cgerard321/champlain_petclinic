import { Injectable } from '@angular/core';

import { isApiError } from '@core/models/api-error';

// Used to localize error messages from the back-end into French
@Injectable({ providedIn: 'root' })
export class AuthErrorMessages {
  forLogin(err: unknown, typedUsername: string): string {
    if (!isApiError(err)) {
      return this.unreachable();
    }

    const message = err.message.trim().toLowerCase();

    // Checks the dynamic message first so a username such as "disabled" can't be mistaken for another case
    if (message.startsWith('incorrect username or password')) {
      return $localize`:@@login.errors.incorrectCredentials:Nom d'utilisateur ou mot de passe incorrect pour l'utilisateur : ${typedUsername}:username:`;
    }
    if (message === 'user not found') {
      return $localize`:@@login.errors.userNotFound:Utilisateur introuvable.`;
    }
    if (message.includes('disabled')) {
      return $localize`:@@login.errors.accountDisabled:Votre compte a été désactivé. Veuillez contacter le soutien.`;
    }
    if (message.includes('not verified')) {
      return $localize`:@@login.errors.accountNotVerified:Votre compte n'est pas vérifié. Un lien de vérification a été envoyé à votre courriel.`;
    }

    return this.generic();
  }

  forForgotPassword(err: unknown, email: string): string {
    if (!isApiError(err)) {
      return this.unreachable();
    }

    const message = err.message.trim().toLowerCase();

    if (message.startsWith('could not find any')) {
      return $localize`:@@forgotPassword.errors.notFound:Aucun compte n'est associé au courriel ${email}:email:.`;
    }

    if (message.startsWith('unable to send')) {
      return $localize`:@@forgotPassword.errors.sendFailed:Impossible d'envoyer le courriel de réinitialisation. Veuillez réessayer plus tard.`;
    }

    return this.generic();
  }

  private unreachable(): string {
    return $localize`:@@auth.errors.unreachable:Impossible de joindre le serveur.`;
  }

  private generic(): string {
    return $localize`:@@auth.errors.generic:Une erreur est survenue. Veuillez réessayer.`;
  }
}
