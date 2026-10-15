import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';

import { Customer } from '@features/cust/models/customer.model';
import { visitStatusLabel } from '@features/vist/models/status-label';
import { VisitResponseModel } from '@features/vist/models/VisitResponseModel';
import { VisitService } from '@features/vist/services/visit-service';

@Component({
  selector: 'app-visit-details',
  imports: [DatePipe, RouterLink],
  templateUrl: './visit-details.html',
  styleUrl: './visit-details.css',
})
export class VisitDetails implements OnInit {
  readonly visitStatusLabel = visitStatusLabel;
  protected readonly labels = {
    back: $localize`:@@visits.details.back:Retour aux visites`,
    title: $localize`:@@visits.details.title:Détails de la visite`,
    loading: $localize`:@@visits.details.loading:Chargement des détails de la visite...`,
    visitInfo: $localize`:@@visits.details.visitInfo:Informations sur la visite`,
    visitId: $localize`:@@visits.details.visitId:ID de la visite`,
    description: $localize`:@@visits.details.description:Description`,
    start: $localize`:@@visits.details.start:Début`,
    end: $localize`:@@visits.details.end:Fin`,
    status: $localize`:@@visits.details.status:Statut`,
    petInfo: $localize`:@@visits.details.petInfo:Informations sur l'animal`,
    name: $localize`:@@visits.details.name:Nom`,
    birthDate: $localize`:@@visits.details.birthDate:Date de naissance`,
    vetInfo: $localize`:@@visits.details.vetInfo:Informations sur le vétérinaire`,
    email: $localize`:@@visits.details.email:Courriel`,
    phone: $localize`:@@visits.details.phone:Téléphone`,
    customerInfo: $localize`:@@visits.details.customerInfo:Informations sur le client`,
    notAvailable: $localize`:@@visits.details.notAvailable:Non disponible`,
  };

  private readonly route = inject(ActivatedRoute);
  private readonly visitService = inject(VisitService);

  protected readonly visit = signal<VisitResponseModel | null>(null);
  protected readonly customer = signal<Customer | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const visitId = this.route.snapshot.paramMap.get('visitId');

    if (!visitId) {
      this.errorMessage.set(
        $localize`:@@visits.details.noVisitId:Aucun identifiant de visite n'a été fourni.`,
      );
      this.isLoading.set(false);
      return;
    }

    this.visitService.getVisitById(visitId).subscribe({
      next: (visit: VisitResponseModel) => {
        this.visit.set(visit);
        this.isLoading.set(false);

        this.visitService
          .getPetById(visit.petId)
          .pipe(
            switchMap((pet) => this.visitService.getCustomerById(pet.customerId)),
            catchError(() => of(null as Customer | null)),
          )
          .subscribe((customer) => {
            this.customer.set(customer);
          });
      },
      error: (error: unknown) => {
        this.isLoading.set(false);

        this.errorMessage.set(
          error instanceof HttpErrorResponse && error.status === 404
            ? $localize`:@@visits.details.notFound:Visite introuvable.`
            : $localize`:@@visits.details.loadError:Impossible de charger les détails de la visite.`,
        );
      },
    });
  }
}
