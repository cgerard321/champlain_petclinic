import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-auth',

  standalone: true,
  // This component manages its own dependencies, no NgModule required

  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
  ],
  templateUrl: './auth.component.html',
  // Path to the HTML file defining this component's markup

  styleUrl: './auth.component.scss',
  // Path to the SCSS file defining this component's styles
})
export class AuthComponent {
  private fb = new FormBuilder();
  // (note: normally injected via inject(FormBuilder) or the constructor,
  // but instantiating it directly like this also works)

  loginForm = this.fb.group({
    username: ['', Validators.required],
    // 'username' field: starts empty (''), must not be left empty (required)
    password: ['', Validators.required],
    // 'password' field: same rule
  });

  onSubmit(): void {
    // Called when the form is submitted (ngSubmit)
    if (this.loginForm.invalid) {
      return;
      // Stop here if required fields are empty — prevents submitting invalid data
    }

    // TODO: connect to AuthService once its implementation has been migrated
    console.log('Login form submitted', this.loginForm.value);
    // Temporary placeholder: logs the form values instead of actually authenticating
  }
}