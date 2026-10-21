import { Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PetType } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';

interface AddPetDialogData {
  customerId: string;
}

@Component({
  selector: 'app-add-pet',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './customer-pet-add.html',
  styleUrl: './customer-pet-add.css',
})
export class AddPet implements OnInit {
  private readonly data = inject<AddPetDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<AddPet>);
  private readonly service = inject(CustomerService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly customerId = this.data.customerId;

  petTypeOptions: PetType[] = [];

  petForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    petTypeId: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    birthDate: new FormControl<Date>(new Date(), {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{4}-\d{2}-\d{2}$/)],
    }),

    weight: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(?:[.,]\d{1,2})?$/)],
    }),

    isActive: new FormControl(true, {
      nonNullable: true,
    }),
  });

  apiError = '';

  ngOnInit(): void {
    this.loadPetTypes();
  }

  private loadPetTypes(): void {
    this.service.getPetTypes().subscribe({
      next: (petType) => {
        this.petTypeOptions.push(petType);
        this.petTypeOptions.sort((a, b) => {
          if (a.name === 'Others') return 1;
          if (b.name === 'Others') return -1;

          return a.name.localeCompare(b.name);
        });
      },
      error: (error) => {
        console.error('Error loading pet types:', error);
        this.apiError = 'Error loading pet types.';
      },
    });
  }
  protected getPetTypeName(petType: PetType): string {
    return (
      this.petTypeLabels[petType?.name.toLowerCase() ?? ''] ?? $localize`:@@pet.types.others:Autres`
    );
  }
  protected readonly petTypeLabels: Record<string, string> = {
    cat: $localize`:@@pet.types.cat:Chat`,
    dog: $localize`:@@pet.types.dog:Chien`,
    lizard: $localize`:@@pet.types.lizard:Lézard`,
    snake: $localize`:@@pet.types.snake:Serpent`,
    hamster: $localize`:@@pet.types.hamster:Hamster`,
    bird: $localize`:@@pet.types.bird:Oiseau`,
  };

  onSubmit(): void {
    if (!this.customerId) {
      this.apiError = 'Customer ID is missing.';
      return;
    }

    if (this.petForm.invalid) {
      this.petForm.markAllAsTouched();
      return;
    }

    const pet = this.petForm.getRawValue();

    this.service.addPet(this.customerId, pet).subscribe({
      next: () => {
        this.dialogRef.close(true);

        this.snackBar.open('Pet added successfully.', 'Close', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
      },

      error: (error) => {
        console.error('Error adding pet:', error);
        this.apiError = 'Error adding pet.';
      },
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
