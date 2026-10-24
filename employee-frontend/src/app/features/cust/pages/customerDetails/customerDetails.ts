import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, mergeMap, of } from 'rxjs';

import { Pet, PetType } from '@features/cust/models/customer.model';
import { CustomerResponseModel } from '@features/cust/models/customerResponseModel';
import { CustomerService } from '@features/cust/services/customer-service';

@Component({
  selector: 'app-customer-details',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './customerDetails.html',
  styleUrl: './customerDetails.css',
})
export class CustomerDetails implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly customerService = inject(CustomerService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    address: ['', Validators.required],
    city: ['', Validators.required],
    province: ['', Validators.required],
    telephone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
  });

  protected readonly pets = signal<Pet[]>([]);
  protected readonly petTypes = signal<PetType[]>([]);
  protected readonly isLoadingPets = signal(true);
  protected readonly petErrorMessage = signal<string | null>(null);
  protected readonly petTypesErrorMessage = signal<string | null>(null);
  protected readonly isSaving = signal(false);
  protected readonly isSaved = signal(false);
  protected readonly isEditing = signal(false);
  protected readonly customer = signal<CustomerResponseModel | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly notAvailable = $localize`:@@cust.notAvailable:Non disponible`;
  protected readonly provinces = [
    { value: 'Alberta', label: $localize`:@@cust.profile.provinces.alberta:Alberta` },
    {
      value: 'British Columbia',
      label: $localize`:@@cust.profile.provinces.britishColumbia:Colombie-Britannique`,
    },
    { value: 'Manitoba', label: $localize`:@@cust.profile.provinces.manitoba:Manitoba` },
    {
      value: 'New Brunswick',
      label: $localize`:@@cust.profile.provinces.newBrunswick:Nouveau-Brunswick`,
    },
    {
      value: 'Newfoundland and Labrador',
      label: $localize`:@@cust.profile.provinces.newfoundlandAndLabrador:Terre-Neuve-et-Labrador`,
    },
    {
      value: 'Nova Scotia',
      label: $localize`:@@cust.profile.provinces.novaScotia:Nouvelle-Écosse`,
    },
    { value: 'Ontario', label: $localize`:@@cust.profile.provinces.ontario:Ontario` },
    {
      value: 'Prince Edward Island',
      label: $localize`:@@cust.profile.provinces.princeEdwardIsland:Île-du-Prince-Édouard`,
    },
    { value: 'Quebec', label: $localize`:@@cust.profile.provinces.quebec:Québec` },
    {
      value: 'Saskatchewan',
      label: $localize`:@@cust.profile.provinces.saskatchewan:Saskatchewan`,
    },
    {
      value: 'Northwest Territories',
      label: $localize`:@@cust.profile.provinces.northwestTerritories:Territoires du Nord-Ouest`,
    },
    { value: 'Nunavut', label: $localize`:@@cust.profile.provinces.nunavut:Nunavut` },
    { value: 'Yukon', label: $localize`:@@cust.profile.provinces.yukon:Yukon` },
  ];
  private readonly petTypeLabels: Record<string, string> = {
    cat: $localize`:@@pet.types.cat:Chat`,
    dog: $localize`:@@pet.types.dog:Chien`,
    lizard: $localize`:@@pet.types.lizard:Lézard`,
    snake: $localize`:@@pet.types.snake:Serpent`,
    hamster: $localize`:@@pet.types.hamster:Hamster`,
    bird: $localize`:@@pet.types.bird:Oiseau`,
    others: $localize`:@@pet.types.others:Autres`,
  };

  ngOnInit(): void {
    const customerId = this.route.snapshot.paramMap.get('customerId');
    if (!customerId) {
      this.isLoading.set(false);
      this.errorMessage.set(
        $localize`:@@cust.profile.errors.noId:Aucun identifiant de client fourni.`,
      );
      return;
    }

    this.customerService.getCustomer(customerId, true).subscribe({
      next: (customer) => {
        this.customer.set(customer);
      },
      complete: () => {
        this.isLoading.set(false);
      },

      error: (error) => {
        console.error('Customer details error:', error);

        this.isLoading.set(false);
        this.errorMessage.set(
          $localize`:@@cust.profile.errors.load:Impossible de charger les détails du client.`,
        );
      },
    });

    this.customerService
      .getPetDetails(customerId)
      .pipe(
        mergeMap(
          (pet) => this.customerService.getPetWithPhoto(pet.petId).pipe(catchError(() => of(pet))),
          4,
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (pet) => this.pets.update((pets) => [...pets, pet]),
        complete: () => this.isLoadingPets.set(false),
        error: () => {
          this.isLoadingPets.set(false);
          this.petErrorMessage.set(
            $localize`:@@cust.profile.errors.pets:Impossible de charger les animaux. Veuillez réessayer plus tard.`,
          );
        },
      });

    this.customerService
      .getPetTypes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (petType) => this.petTypes.update((types) => [...types, petType]),
        error: () =>
          this.petTypesErrorMessage.set(
            $localize`:@@cust.profile.errors.petTypes:Impossible de charger les types d'animaux.`,
          ),
      });
  }

  protected getPetTypeName(petTypeId: string): string {
    const name = this.petTypes().find((type) => type.petTypeId === petTypeId)?.name;
    return name ? (this.petTypeLabels[name.toLowerCase()] ?? name) : this.notAvailable;
  }
  protected getProvinceLabel(value: string): string {
    return (
      this.provinces.find(
        (province) => province.value.localeCompare(value, undefined, { sensitivity: 'base' }) === 0,
      )?.label ?? value
    );
  }
  protected handleEditCustomer(): void {
    const customer = this.customer();
    if (!customer) return;
    this.isSaved.set(false);
    this.form.reset({
      firstName: customer.firstName,
      lastName: customer.lastName,
      address: customer.address,
      city: customer.city,
      province: customer.province,
      telephone: customer.telephone,
    });
    this.errorMessage.set(null);
    this.isEditing.set(true);
  }
  protected handleCustomerUpdated(): void {
    const customer = this.customer();
    if (!customer || this.isSaving()) return;
    const firstName = this.form.getRawValue().firstName.trim();
    const lastName = this.form.getRawValue().lastName.trim();
    const address = this.form.getRawValue().address.trim();
    const city = this.form.getRawValue().city.trim();
    const province = this.form.getRawValue().province.trim();
    const telephone = this.form.getRawValue().telephone.trim();
    this.form.controls.address.setValue(address);
    this.form.controls.city.setValue(city);
    this.form.controls.province.setValue(province);
    this.form.controls.telephone.setValue(telephone);
    this.form.controls.firstName.setValue(firstName);
    this.form.controls.lastName.setValue(lastName);
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    if (
      !window.confirm(
        $localize`:@@cust.profile.confirmSave:Voulez-vous vraiment enregistrer ces modifications ?`,
      )
    )
      return;

    this.isSaving.set(true);
    this.isSaved.set(false);
    this.errorMessage.set(null);
    this.customerService
      .updateCustomer(customer.customerId, {
        firstName,
        lastName,
        address,
        city,
        province,
        telephone,
      })
      .subscribe({
        next: (updatedCustomer) => {
          this.customer.set({ ...updatedCustomer, photo: updatedCustomer.photo ?? customer.photo });
          this.isEditing.set(false);
          this.isSaving.set(false);
          this.isSaved.set(true);
        },
        error: () => {
          this.errorMessage.set(
            $localize`:@@cust.profile.errors.update:Impossible de mettre à jour le client. Veuillez réessayer.`,
          );
          this.isSaving.set(false);
        },
      });
  }
  protected backToList(): void {
    this.router.navigate(['/cust']);
  }
}
