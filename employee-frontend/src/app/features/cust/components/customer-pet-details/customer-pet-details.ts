import { Component, inject, OnInit, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

import { Pet, PetType } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';

interface CustomerPetDetails {
  customerId: string;
}

@Component({
  selector: 'app-customer-pet-details',
  imports: [MatDialogModule, MatProgressSpinner, MatIconModule],
  templateUrl: './customer-pet-details.html',
  styleUrl: './customer-pet-details.css',
})
export class PetDetails implements OnInit {
  private readonly data = inject<CustomerPetDetails>(MAT_DIALOG_DATA);
  private readonly customerService = inject(CustomerService);

  protected readonly petTypes = signal<PetType[]>([]);
  protected readonly pets = signal<Pet[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly year = $localize`:@@pet.petDetails.year:an`;
  protected readonly years = $localize`:@@pet.petDetails.years:ans`;

  ngOnInit(): void {
    this.customerService.getPetDetails(this.data.customerId).subscribe({
      next: (pet) => {
        this.pets.update((pets) => [...pets, pet]);
      },
      complete: () => {
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Pet details error:', error);

        this.isLoading.set(false);
        this.errorMessage.set('Could not load pet details.');
      },
    });
    this.customerService.getPetTypes().subscribe({
      next: (petType) => {
        this.petTypes.update((types) => [...types, petType]);
      },
      error: (error) => {
        console.error('Pet types error:', error);
        this.errorMessage.set('Could not load pet species.');
      },
    });
  }
  calculateAge = (birthDate: Date): number => {
    const birth = new Date(birthDate);
    const ageDiffMs = Date.now() - birth.getTime();
    const ageDate = new Date(ageDiffMs);
    return Math.abs(ageDate.getUTCFullYear() - 1970);
  };
  protected getPetTypeName(petTypeId: string): string {
    const petType = this.petTypes().find((type) => type.petTypeId === petTypeId);

    return (
      this.petTypeLabels[petType?.name.toLowerCase() ?? ''] ?? $localize`:@@pet.types.others:Autres`
    );
  }
  protected readonly petTypeLabels: Record<string, string> = {
    cat: $localize`:@@pet.types.cat:Chat`,
    dog: $localize`:@@pet.types.dog:Chien`,
    lizard: $localize`:@@pet.types.lizard:Lizard`,
    snake: $localize`:@@pet.types.snake:Serpent`,
    hamster: $localize`:@@pet.types.hamster:Hamster`,
    bird: $localize`:@@pet.types.bird:Oiseau`,
  };
}
