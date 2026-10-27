import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { PetType, petTypeMatchesFilters } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';

@Component({
  imports: [MatProgressSpinnerModule],
  selector: 'app-pet-type-list',
  styleUrl: './pet-type-list.css',
  templateUrl: './pet-type-list.html',
})
export class PetTypeList implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly untilDestroyed = takeUntilDestroyed<PetType>();

  protected readonly petTypes = signal<PetType[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly idFilter = signal('');
  protected readonly nameFilter = signal('');
  protected readonly descriptionFilter = signal('');

  protected readonly filteredPetTypes = computed(() => {
    const filters = {
      id: this.idFilter(),
      name: this.nameFilter(),
      description: this.descriptionFilter(),
    };

    return this.petTypes().filter((petType) => petTypeMatchesFilters(petType, filters));
  });

  ngOnInit(): void {
    this.customerService
      .getPetTypes()
      .pipe(this.untilDestroyed)
      .subscribe({
        next: (petType) => this.petTypes.update((types) => [...types, petType]),
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set(
            $localize`:@@petTypes.list.loadError:Impossible de charger les types d'animaux. Veuillez réessayer plus tard.`,
          );
        },
        complete: () => this.isLoading.set(false),
      });
  }
}
