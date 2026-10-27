import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { Pet, PetType, petMatchesFilters } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';

@Component({
  imports: [MatProgressSpinnerModule, DatePipe],
  selector: 'app-pet-list',
  styleUrl: './pet-list.css',
  templateUrl: './pet-list.html',
})
export class PetList implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly pets = signal<Pet[]>([]);
  protected readonly petTypes = signal<PetType[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly petTypeFilter = signal('');
  protected readonly nameFilter = signal('');

  private readonly petTypeLabels: Record<string, string> = {
    cat: $localize`:@@pet.types.cat:Chat`,
    dog: $localize`:@@pet.types.dog:Chien`,
    lizard: $localize`:@@pet.types.lizard:Lézard`,
    snake: $localize`:@@pet.types.snake:Serpent`,
    hamster: $localize`:@@pet.types.hamster:Hamster`,
    bird: $localize`:@@pet.types.bird:Oiseau`,
  };
  private readonly otherLabel = $localize`:@@pet.types.others:Autres`;

  protected readonly filteredPets = computed(() => {
    const filters = { petType: this.petTypeFilter(), name: this.nameFilter() };

    return this.pets().filter((pet) =>
      petMatchesFilters(pet, this.petTypeNames(pet.petTypeId), filters),
    );
  });

  ngOnInit(): void {
    this.customerService
      .getPetTypes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (petType) => this.petTypes.update((types) => [...types, petType]),
      });

    this.customerService
      .getAllPets()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pet) => this.pets.update((pets) => [...pets, pet]),
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set(
            $localize`:@@petList.loadError:Impossible de charger les animaux. Veuillez réessayer plus tard.`,
          );
        },
        complete: () => this.isLoading.set(false),
      });
  }

  protected getPetTypeLabel(petTypeId: string): string {
    const petType = this.petTypes().find((type) => type.petTypeId === petTypeId);
    if (!petType) {
      return this.otherLabel;
    }
    return this.petTypeLabels[petType.name.toLowerCase()] ?? petType.name;
  }

  private petTypeNames(petTypeId: string): string[] {
    const petType = this.petTypes().find((type) => type.petTypeId === petTypeId);
    return [this.getPetTypeLabel(petTypeId), petType?.name ?? ''];
  }
}
