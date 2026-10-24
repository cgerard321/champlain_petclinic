import { Component, inject, signal } from '@angular/core';
import { Validators } from '@angular/forms';
import { required, form, FormField } from '@angular/forms/signals';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { LoginRequest } from '@features/auth/models/loginRequest';
import { Auth } from '@features/auth/services/auth';
import { AuthErrorMessages } from '@features/auth/services/auth-error-messages';
import { ThemeService } from '@features/settings/services/themeservice';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import { ModalFormData, ModalFormResult } from '@shared/components/modal-form/modal-form-models';

interface ForgotPasswordFormValue {
  email: string;
}

@Component({
  selector: 'app-login',
  imports: [MatIconModule, MatProgressSpinnerModule, FormField],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginPage {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly themeService = inject(ThemeService);
  private readonly errorMessages = inject(AuthErrorMessages);

  protected readonly hidePassword = signal(true);
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly model = signal<LoginRequest>({ emailOrUsername: '', password: '' });

  protected readonly isEnglish = (localStorage.getItem('lang') ?? 'fr').startsWith('en');

  protected readonly loginForm = form(this.model, (schemaPath) => {
    required(schemaPath.emailOrUsername, {
      message: $localize`:@@login.errors.usernameRequired:Le nom d'utilisateur ou le courriel est requis.`,
    });
    required(schemaPath.password, {
      message: $localize`:@@login.errors.passwordRequired:Le mot de passe est requis.`,
    });
  });

  protected togglePasswordVisibility(): void {
    this.hidePassword.update((hidden) => !hidden);
  }

  protected switchLanguage(): void {
    localStorage.setItem('lang', this.isEnglish ? 'fr-CA' : 'en-CA');
    location.reload();
  }

  protected submit(event: Event): void {
    event.preventDefault();

    if (this.loginForm().invalid()) {
      // Reveals the validation messages for the fields
      this.loginForm.emailOrUsername().markAsTouched();
      this.loginForm.password().markAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const credentials = { ...this.model() };

    this.auth.login(this.model()).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.themeService.setCurrentUser(this.model().emailOrUsername);
        this.router.navigateByUrl('/');
      },
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.errorMessages.forLogin(err, credentials.emailOrUsername));
      },
    });
  }

  protected openForgotPassword(): void {
    const typed = this.model().emailOrUsername.trim();

    const dialogRef = this.dialog.open<
      ModalForm<ForgotPasswordFormValue>,
      ModalFormData<ForgotPasswordFormValue>,
      ModalFormResult<ForgotPasswordFormValue>
    >(ModalForm, {
      data: {
        title: $localize`:@@forgotPassword.title:Mot de passe oublié`,
        message: $localize`:@@forgotPassword.message:Entrez votre courriel et nous vous enverrons un lien de réinitialisation.`,
        submitLabel: $localize`:@@forgotPassword.submit:Envoyer le lien`,
        cancelLabel: $localize`:@@forgotPassword.cancel:Annuler`,
        fields: [
          {
            key: 'email',
            label: $localize`:@@forgotPassword.email:Courriel`,
            type: 'string',
            required: true,
            validators: [Validators.email],
            errorMessages: {
              required: $localize`:@@forgotPassword.errors.emailRequired:Le courriel est requis.`,
              email: $localize`:@@forgotPassword.errors.emailInvalid:Entrez un courriel valide.`,
            },
          },
        ],

        // Remembers the email if the user already typed it in the login field.
        initialValue: { email: typed.includes('@') ? typed : '' },
      },
      ariaLabel: $localize`:@@forgotPassword.title:Mot de passe oublié`,
      width: '440px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result) {
        return;
      }

      const email = result.value.email.trim();

      this.auth.sendPasswordResetEmail(email).subscribe({
        next: () =>
          this.notify(
            $localize`:@@forgotPassword.sent:Si un compte existe pour cette adresse, un courriel de récupération vient d'être envoyé.`,
          ),
        error: (err: unknown) =>
          this.errorMessage.set(this.errorMessages.forForgotPassword(err, email)),
      });
    });
  }

  private notify(message: string): void {
    this.snackBar.open(message, undefined, { duration: 4000, verticalPosition: 'top' });
  }
}
