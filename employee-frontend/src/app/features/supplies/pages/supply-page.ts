import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  computed,
  inject,
  LOCALE_ID,
  signal,
  TemplateRef,
  viewChild,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { Supply, SupplyRequest } from '@features/supplies/models/supply';
import {
  SupplyHistoryDialog,
  SupplyHistoryDialogData,
} from '@features/supplies/pages/supply-history-dialog';
import { SupplyService } from '@features/supplies/services/supply-service';
import { ALLOWED_IMAGE_TYPES, ImageAction } from '@shared/components/image-editor/image-editor';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import {
  ModalFormData,
  ModalFormField,
  ModalFormResult,
} from '@shared/components/modal-form/modal-form-models';
import {
  SupplySummaryAction,
  SupplySummaryDialog,
  SupplySummaryDialogData,
} from '@shared/components/supply-summary-dialog/supply-summary-dialog';
import { Table, TableColumn } from '@shared/components/table/table';
import { Roles } from '@shared/models/roles';

// The shape of the modal's form: the editable Supply fields plus the photo control.
interface SupplyFormValue {
  productName: string;
  supplierName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  expirationDate: string;
  photo: ImageAction;
}

@Component({
  imports: [CurrencyPipe, MatIconModule, MatMenuModule, Table, MatTooltipModule],
  selector: 'app-supply',
  styleUrl: './supply-page.css',
  templateUrl: './supply-page.html',
})
export class SupplyPage {
  private readonly route = inject(ActivatedRoute);
  private readonly supplyService = inject(SupplyService);
  private readonly auth = inject(AuthState);
  private readonly dialog = inject(MatDialog);
  private readonly locale = inject(LOCALE_ID);

  // Translate the seeded records for display while keeping their stored API values unchanged.
  private readonly starterSupplyNames: Record<string, string> = {
    Benzodiazepines: $localize`:@@supply.data.names.benzodiazepines:Benzodiazépines`,
    Trazodone: $localize`:@@supply.data.names.trazodone:Trazodone`,
    Carprofen: $localize`:@@supply.data.names.carprofen:Carprofène`,
    'Self-adhesive Bandages': $localize`:@@supply.data.names.selfAdhesiveBandages:Bandages auto-adhésifs`,
    'Gauze Pads': $localize`:@@supply.data.names.gauzePads:Compresses de gaze`,
    'Hydrogen Peroxide': $localize`:@@supply.data.names.hydrogenPeroxide:Peroxyde d'hydrogène`,
    'MRI Machine': $localize`:@@supply.data.names.mriMachine:Appareil d'IRM`,
    'Rabies Vaccine': $localize`:@@supply.data.names.rabiesVaccine:Vaccin contre la rage`,
    Endoscope: $localize`:@@supply.data.names.endoscope:Endoscope`,
    'Ultrasound Machine': $localize`:@@supply.data.names.ultrasoundMachine:Appareil d'échographie`,
    'Elastic Bandage': $localize`:@@supply.data.names.elasticBandage:Bandage élastique`,
    'Antiseptic Wipes': $localize`:@@supply.data.names.antisepticWipes:Lingettes antiseptiques`,
    'Insulin Syringes': $localize`:@@supply.data.names.insulinSyringes:Seringues à insuline`,
    'IV Catheters': $localize`:@@supply.data.names.ivCatheters:Cathéters intraveineux`,
    Antihistamines: $localize`:@@supply.data.names.antihistamines:Antihistaminiques`,
    'Antibiotic Ointment': $localize`:@@supply.data.names.antibioticOintment:Onguent antibiotique`,
    'Blood Glucose Test Kits': $localize`:@@supply.data.names.bloodGlucoseTestKits:Trousses de mesure de la glycémie`,
    'Urinalysis Test Kits': $localize`:@@supply.data.names.urinalysisTestKits:Trousses d'analyse d'urine`,
    'Hydrogel Dressing': $localize`:@@supply.data.names.hydrogelDressing:Pansement hydrogel`,
    'Sutures Kit': $localize`:@@supply.data.names.suturesKit:Trousse de sutures`,
    'Anesthesia Machines': $localize`:@@supply.data.names.anesthesiaMachines:Appareils d'anesthésie`,
    'Anesthetic Masks': $localize`:@@supply.data.names.anestheticMasks:Masques d'anesthésie`,
    'Surgical Scissors': $localize`:@@supply.data.names.surgicalScissors:Ciseaux chirurgicaux`,
    Hemostats: $localize`:@@supply.data.names.hemostats:Pinces hémostatiques`,
    'Needle Holders': $localize`:@@supply.data.names.needleHolders:Porte-aiguilles`,
    'Pet Carrier Accessories': $localize`:@@supply.data.names.petCarrierAccessories:Accessoires pour cages de transport`,
    'Carrier Cleaning Supplies': $localize`:@@supply.data.names.carrierCleaningSupplies:Produits de nettoyage pour cages de transport`,
    'Comfort Pads': $localize`:@@supply.data.names.comfortPads:Coussinets de confort`,
    'Parvovirus Test Kits': $localize`:@@supply.data.names.parvovirusTestKits:Trousses de dépistage du parvovirus`,
    'Feline Leukemia Test Kits': $localize`:@@supply.data.names.felineLeukemiaTestKits:Trousses de dépistage de la leucémie féline`,
    'Heartworm Test Kits': $localize`:@@supply.data.names.heartwormTestKits:Trousses de dépistage du ver du cœur`,
  };
  private readonly starterSupplyDescriptions: Record<string, string> = {
    'Drugs for sleep': $localize`:@@supply.data.descriptions.benzodiazepines:Médicaments pour le sommeil`,
    'Drugs for anxiety/stress': $localize`:@@supply.data.descriptions.trazodone:Médicaments pour l'anxiété et le stress`,
    'Non-steroidal anti-inflammatory drug': $localize`:@@supply.data.descriptions.carprofen:Anti-inflammatoire non stéroïdien`,
    'Bandages for wound care': $localize`:@@supply.data.descriptions.selfAdhesiveBandages:Bandages pour le soin des plaies`,
    'Absorbent pads for wound care': $localize`:@@supply.data.descriptions.gauzePads:Compresses absorbantes pour le soin des plaies`,
    'Antiseptic for wound cleaning': $localize`:@@supply.data.descriptions.hydrogenPeroxide:Antiseptique pour nettoyer les plaies`,
    'Imaging machine for diagnostics': $localize`:@@supply.data.descriptions.mriMachine:Appareil d'imagerie pour le diagnostic`,
    'Core vaccine for rabies prevention': $localize`:@@supply.data.descriptions.rabiesVaccine:Vaccin essentiel pour prévenir la rage`,
    'Endoscope for internal examinations': $localize`:@@supply.data.descriptions.endoscope:Endoscope pour les examens internes`,
    'Ultrasound machine for diagnostic imaging': $localize`:@@supply.data.descriptions.ultrasoundMachine:Appareil d'échographie pour l'imagerie diagnostique`,
    'Elastic bandage for flexible wound support': $localize`:@@supply.data.descriptions.elasticBandage:Bandage élastique pour soutenir les plaies avec souplesse`,
    'Antiseptic wipes for cleaning wounds': $localize`:@@supply.data.descriptions.antisepticWipes:Lingettes antiseptiques pour nettoyer les plaies`,
    'Syringes for administering insulin': $localize`:@@supply.data.descriptions.insulinSyringes:Seringues pour administrer l'insuline`,
    'Intravenous catheters for fluid administration': $localize`:@@supply.data.descriptions.ivCatheters:Cathéters intraveineux pour administrer des liquides`,
    'Antihistamines for allergic reactions': $localize`:@@supply.data.descriptions.antihistamines:Antihistaminiques pour les réactions allergiques`,
    'Topical antibiotic ointment for wound care': $localize`:@@supply.data.descriptions.antibioticOintment:Onguent antibiotique topique pour le soin des plaies`,
    'Test kits for measuring blood glucose levels': $localize`:@@supply.data.descriptions.bloodGlucoseTestKits:Trousses pour mesurer la glycémie`,
    'Test kits for analyzing urine samples': $localize`:@@supply.data.descriptions.urinalysisTestKits:Trousses pour analyser des échantillons d'urine`,
    'Hydrogel dressing for moist wound healing': $localize`:@@supply.data.descriptions.hydrogelDressing:Pansement hydrogel pour la cicatrisation des plaies en milieu humide`,
    'Kit containing sutures for wound closure': $localize`:@@supply.data.descriptions.suturesKit:Trousse contenant des sutures pour fermer les plaies`,
    'Machines for administering anesthesia': $localize`:@@supply.data.descriptions.anesthesiaMachines:Appareils pour administrer l'anesthésie`,
    'Masks for delivering anesthesia': $localize`:@@supply.data.descriptions.anestheticMasks:Masques pour administrer l'anesthésie`,
    'Scissors for cutting tissues during surgery': $localize`:@@supply.data.descriptions.surgicalScissors:Ciseaux pour couper les tissus pendant une chirurgie`,
    'Clamps for controlling bleeding during surgery': $localize`:@@supply.data.descriptions.hemostats:Pinces pour maîtriser les saignements pendant une chirurgie`,
    'Hold needles securely during suturing': $localize`:@@supply.data.descriptions.needleHolders:Maintiennent les aiguilles en place pendant les sutures`,
    'Accessories like water bowls and food trays for pet carriers': $localize`:@@supply.data.descriptions.petCarrierAccessories:Accessoires comme des bols d'eau et des plateaux de nourriture pour les cages de transport`,
    'Cleaning sprays and wipes for pet carriers': $localize`:@@supply.data.descriptions.carrierCleaningSupplies:Vaporisateurs et lingettes pour nettoyer les cages de transport`,
    'Comfortable pads for lining pet carriers': $localize`:@@supply.data.descriptions.comfortPads:Coussinets confortables pour garnir les cages de transport`,
    'Test kits for detecting canine parvovirus': $localize`:@@supply.data.descriptions.parvovirusTestKits:Trousses pour détecter le parvovirus canin`,
    'Test kits for detecting feline leukemia virus': $localize`:@@supply.data.descriptions.felineLeukemiaTestKits:Trousses pour détecter le virus de la leucémie féline`,
    'Test kits for detecting heartworm infection': $localize`:@@supply.data.descriptions.heartwormTestKits:Trousses pour détecter une infection par le ver du cœur`,
  };

  protected readonly isFrench = this.locale.startsWith('fr');
  protected readonly tableLabel = $localize`:@@supply.table.label:Articles dans cet inventaire`;
  protected readonly downloadLabel = $localize`:@@supply.actions.download:Télécharger PDF`;
  protected readonly downloadingLabel = $localize`:@@supply.actions.downloading:Téléchargement…`;
  protected readonly consumeLabel = $localize`:@@supply.actions.consume:Utiliser l'article`;
  protected readonly cannotConsumeStockLabel = $localize`:@@supply.actions.cannotConsumeStock:Impossible d'utiliser l'article : rupture de stock`;
  protected readonly cannotConsumeExpiredLabel = $localize`:@@supply.actions.cannotConsumeExpired:Impossible d'utiliser l'article : produit périmé`;
  protected readonly consumingLabel = $localize`:@@supply.actions.consuming:Utilisation…`;
  protected readonly consumeButtonLabel = $localize`:@@supply.actions.consumeButton:Utiliser`;

  protected readonly inventoryId = this.route.snapshot.paramMap.get('inventoryId');

  protected readonly supplies = signal<Supply[]>([]);
  protected readonly filters = signal({
    productName: '',
    supplierName: '',
    productDescription: '',
    status: '',
  });
  protected readonly hasActiveFilters = computed(() =>
    Object.values(this.filters()).some((value) => value.trim().length > 0),
  );

  protected readonly filteredSupplies = computed(() => {
    const { productName, supplierName, productDescription, status } = this.filters();
    return this.supplies().filter(
      (supply) =>
        (this.localizedSupplyName(supply.productName)
          .toLowerCase()
          .includes(productName.trim().toLowerCase()) ||
          supply.productName.toLowerCase().includes(productName.trim().toLowerCase())) &&
        this.supplierLabel(supply).toLowerCase().includes(supplierName.trim().toLowerCase()) &&
        (this.localizedSupplyDescription(supply.productDescription)
          .toLowerCase()
          .includes(productDescription.trim().toLowerCase()) ||
          supply.productDescription
            .toLowerCase()
            .includes(productDescription.trim().toLowerCase())) &&
        (!status || this.effectiveStatus(supply) === status),
    );
  });

  protected openSupplyHistory(supply: Supply): void {
    if (!this.canManageSupplies() || !this.inventoryId) {
      return;
    }

    this.dialog.open<SupplyHistoryDialog, SupplyHistoryDialogData>(SupplyHistoryDialog, {
      data: {
        inventoryId: this.inventoryId,
        productId: supply.productId,
        supplyName: this.localizedSupplyName(supply.productName),
      },
      ariaLabel: $localize`:@@supply.history.ariaLabel:Historique de l'article ${this.localizedSupplyName(supply.productName)}:name:`,
      width: '640px',
      maxWidth: '95vw',
    });
  }

  protected openSupplySummary(supply: Supply): void {
    const canConsume = this.canConsumeSupplies();
    const dialogRef = this.dialog.open<
      SupplySummaryDialog,
      SupplySummaryDialogData,
      SupplySummaryAction
    >(SupplySummaryDialog, {
      data: {
        supply,
        displayName: this.localizedSupplyName(supply.productName),
        displayDescription: this.localizedSupplyDescription(supply.productDescription),
        statusLabel: this.formatStatus(this.effectiveStatus(supply)),
        photoUrl: this.supplyPhotoUrl(supply),
        canManage: this.canManageSupplies(),
        canConsume,
        consumeDisabled:
          supply.productQuantity <= 0 ||
          this.expirationState(supply) === 'expired' ||
          this.consumingSupplyId() === supply.productId,
      },
      ariaLabel: $localize`:@@supply.summary.ariaLabel:Résumé de l'article ${this.localizedSupplyName(supply.productName)}:name:`,
      width: '640px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((action) => {
      switch (action) {
        case 'edit':
          this.editSupply(supply);
          break;
        case 'delete':
          this.deleteSupply(supply);
          break;
        case 'history':
          this.openSupplyHistory(supply);
          break;
        case 'consume':
          this.consumeSupply(supply);
          break;
      }
    });
  }

  protected setFilter(
    field: 'productName' | 'supplierName' | 'productDescription' | 'status',
    event: Event,
  ): void {
    const value = (event.target as HTMLInputElement | HTMLSelectElement).value;
    this.filters.update((filters) => ({ ...filters, [field]: value }));
  }

  protected clearFilters(): void {
    this.filters.set({ productName: '', supplierName: '', productDescription: '', status: '' });
  }

  // Only the initial fetch replaces the table; refreshes keep existing rows visible.
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly addingSupply = signal(false);
  protected readonly addError = signal('');
  protected readonly deleteError = signal('');
  protected readonly downloadingPdf = signal(false);
  protected readonly downloadError = signal('');
  protected readonly consumingSupplyId = signal<string | null>(null);

  protected readonly nameCell = viewChild<TemplateRef<{ $implicit: Supply }>>('nameCell');
  protected readonly priceCell = viewChild<TemplateRef<{ $implicit: Supply }>>('priceCell');
  protected readonly statusCell = viewChild<TemplateRef<{ $implicit: Supply }>>('statusCell');
  protected readonly actionsCell = viewChild<TemplateRef<{ $implicit: Supply }>>('actionsCell');
  protected readonly expirationCell =
    viewChild<TemplateRef<{ $implicit: Supply }>>('expirationCell');
  protected readonly supplyRowId = (supply: Supply): string => supply.productId;

  // Formatting and actions stay here so the shared table has no supply-specific logic.
  protected readonly columns = computed<TableColumn<Supply>[]>(() => [
    { id: 'name', header: $localize`:@@supply.fields.name:Nom`, template: this.nameCell() },
    {
      id: 'supplier',
      header: $localize`:@@supply.fields.supplier:Fournisseur`,
      value: (supply) => this.supplierLabel(supply),
    },
    {
      id: 'description',
      header: $localize`:@@supply.fields.description:Description`,
      value: (supply) => this.localizedSupplyDescription(supply.productDescription),
    },
    {
      id: 'price',
      header: $localize`:@@supply.fields.costPrice:Prix coûtant (CAD)`,
      template: this.priceCell(),
    },
    {
      id: 'quantity',
      header: $localize`:@@supply.fields.quantity:Quantité`,
      value: (supply) => supply.productQuantity,
    },
    {
      id: 'expiration',
      header: $localize`:@@supply.fields.expiration:Date d'expiration`,
      template: this.expirationCell(),
    },
    {
      id: 'status',
      header: $localize`:@@supply.fields.status:État`,
      template: this.statusCell(),
    },
    {
      id: 'actions',
      header: $localize`:@@supply.fields.actions:Actions`,
      template: this.actionsCell(),
    },
  ]);

  protected localizedSupplyName(name: string): string {
    return this.starterSupplyNames[name] ?? name;
  }

  protected localizedSupplyDescription(description: string): string {
    return this.starterSupplyDescriptions[description] ?? description;
  }

  private supplierLabel(supply: Supply): string {
    return supply.supplierName?.trim() || $localize`:@@supply.supplier.notRecorded:Non indiqué`;
  }

  protected readonly canManageSupplies = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
  });

  protected readonly canConsumeSupplies = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.vet);
  });

  constructor() {
    this.loadSupplies();
  }

  private loadSupplies(): void {
    if (!this.inventoryId) {
      this.loading.set(false);
      this.error.set(true);
      return;
    }

    this.error.set(false);

    this.supplyService.getSupplies(this.inventoryId).subscribe({
      next: (supplies) => {
        this.supplies.set(supplies);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  protected openAddDialog(): void {
    if (!this.canManageSupplies()) {
      return;
    }

    void this.openSupplyDialog();
  }

  protected editSupply(supply: Supply): void {
    if (!this.canManageSupplies()) {
      return;
    }

    void this.openSupplyDialog(supply);
  }

  private supplyFields(supply?: Supply): readonly ModalFormField[] {
    const currentImageUrl = supply ? this.supplyPhotoUrl(supply) : null;

    return [
      {
        key: 'productName',
        label: $localize`:@@supply.fields.name:Nom`,
        type: 'string',
        required: true,
      },
      {
        key: 'supplierName',
        label: $localize`:@@supply.fields.supplierName:Nom du fournisseur`,
        type: 'string',
        required: !supply || Boolean(supply.supplierName?.trim()),
        validators: [
          (control) =>
            typeof control.value === 'string' &&
            control.value.length > 0 &&
            control.value.trim().length === 0
              ? { whitespace: true }
              : null,
        ],
        errorMessages: {
          whitespace: $localize`:@@supply.validation.supplierWhitespace:Le nom du fournisseur ne peut pas contenir seulement des espaces.`,
        },
      },
      {
        key: 'productDescription',
        label: $localize`:@@supply.fields.description:Description`,
        type: 'string',
        required: true,
      },
      {
        key: 'productPrice',
        label: $localize`:@@supply.fields.costPrice:Prix coûtant (CAD)`,
        type: 'number',
        required: true,
        min: 0.01,
        step: 0.01,
      },
      {
        key: 'productQuantity',
        label: $localize`:@@supply.fields.quantity:Quantité`,
        type: 'number',
        required: true,
        min: 1,
        step: 1,
      },
      {
        key: 'productSalePrice',
        label: $localize`:@@supply.fields.salePrice:Prix de vente (CAD)`,
        type: 'number',
        required: true,
        min: 0.01,
        step: 0.01,
      },
      {
        key: 'expirationDate',
        label: $localize`:@@supply.fields.expiration:Date d'expiration`,
        type: 'date',
        // Only reject past expiry dates when creating new supplies
        minDate: supply ? undefined : this.todayAsText(),
        errorMessages: {
          minDate: $localize`:@@supply.validation.expirationPast:La date d'expiration ne peut pas être dans le passé.`,
        },
      },
      {
        key: 'photo',
        label: $localize`:@@supply.fields.photo:Photo de l'article`,
        type: 'image',
        currentImageUrl,
      },
    ];
  }

  private async openSupplyDialog(supply?: Supply): Promise<void> {
    this.addError.set('');

    const dialogRef = this.dialog.open<
      ModalForm<SupplyFormValue>,
      ModalFormData<SupplyFormValue>,
      ModalFormResult<SupplyFormValue>
    >(ModalForm, {
      data: {
        title: supply
          ? $localize`:@@supply.actions.editTitle:Modifier l'article`
          : $localize`:@@supply.actions.add:Ajouter un article`,
        submitLabel: supply
          ? $localize`:@@supply.actions.saveChanges:Enregistrer les modifications`
          : $localize`:@@supply.actions.add:Ajouter un article`,
        cancelLabel: $localize`:@@supply.actions.cancel:Annuler`,
        fields: this.supplyFields(supply),
        initialValue: supply
          ? {
              productName: this.localizedSupplyName(supply.productName),
              supplierName: supply.supplierName ?? '',
              productDescription: this.localizedSupplyDescription(supply.productDescription),
              productPrice: supply.productPrice,
              productQuantity: supply.productQuantity,
              productSalePrice: supply.productSalePrice,
              expirationDate: supply.expirationDate ?? '',
            }
          : {
              productName: '',
              supplierName: '',
              productDescription: '',
              productPrice: 0,
              productQuantity: 1,
              productSalePrice: 0,
              expirationDate: '',
            },
      },
      ariaLabel: supply
        ? $localize`:@@supply.actions.editTitle:Modifier l'article`
        : $localize`:@@supply.actions.add:Ajouter un article`,
      width: '560px',
      maxWidth: '95vw',
    });

    // The dialog closes with undefined when the user cancels.
    const result = await firstValueFrom(dialogRef.afterClosed());

    if (!result || !this.canManageSupplies()) {
      return;
    }

    await this.saveSupply(result.value, supply ?? null);
  }

  private async saveSupply(value: SupplyFormValue, original: Supply | null): Promise<void> {
    const inventoryId = this.inventoryId;
    const editingId = original?.productId ?? null;

    if (!inventoryId || this.addingSupply()) {
      return;
    }

    this.addingSupply.set(true);
    this.addError.set('');

    try {
      const payload = await this.buildSupplyRequest(value, original);

      const request$ = editingId
        ? this.supplyService.updateSupply(inventoryId, editingId, payload)
        : this.supplyService.createSupply(inventoryId, payload);

      await firstValueFrom(request$);

      this.loadSupplies();
    } catch {
      this.addError.set(
        editingId
          ? $localize`:@@supply.errors.update:Impossible de modifier l'article ou la photo.`
          : $localize`:@@supply.errors.create:Impossible d'ajouter l'article ou la photo.`,
      );
    } finally {
      this.addingSupply.set(false);
    }
  }

  protected consumeSupply(supply: Supply): void {
    if (!this.canConsumeSupplies() || !this.inventoryId) {
      return;
    }

    // Prevent consuming if quantity is already 0 or if the supply is expired
    if (supply.productQuantity <= 0 || this.expirationState(supply) === 'expired') {
      return;
    }
    this.deleteError.set('');

    this.consumingSupplyId.set(supply.productId);

    this.supplyService.consumeSupply(this.inventoryId, supply.productId).subscribe({
      next: () => {
        this.consumingSupplyId.set(null);
        this.loadSupplies();
      },
      error: () => {
        this.consumingSupplyId.set(null);
        this.deleteError.set($localize`:@@supply.errors.consume:Impossible d'utiliser l'article.`);
      },
    });
  }

  protected deleteSupply(supply: Supply): void {
    if (!this.canManageSupplies()) {
      return;
    }

    this.deleteError.set('');

    const inventoryId = this.inventoryId;

    if (!inventoryId) {
      this.deleteError.set(
        $localize`:@@supply.errors.missingInventory:Impossible de supprimer l'article : identifiant d'inventaire manquant.`,
      );
      return;
    }

    const dialogRef = this.dialog.open<
      ModalForm<Record<string, never>>,
      ModalFormData<Record<string, never>>,
      ModalFormResult<Record<string, never>>
    >(ModalForm, {
      data: {
        title: $localize`:@@supply.actions.deleteTitle:Supprimer l'article`,
        message: $localize`:@@supply.actions.deleteConfirm:Voulez-vous vraiment supprimer ${this.localizedSupplyName(supply.productName)}:name: ?`,
        fields: [],
        initialValue: {},
        submitLabel: $localize`:@@supply.actions.delete:Supprimer`,
        cancelLabel: $localize`:@@supply.actions.cancel:Annuler`,
        destructive: true,
      },
      ariaLabel: $localize`:@@supply.actions.deleteTitle:Supprimer l'article`,
      width: '440px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result || !this.canManageSupplies()) {
        return;
      }

      this.supplyService.deleteSupply(inventoryId, supply.productId).subscribe({
        next: () => {
          this.loadSupplies();
        },
        error: () => {
          this.deleteError.set(
            $localize`:@@supply.errors.delete:Impossible de supprimer l'article.`,
          );
        },
      });
    });
  }

  protected downloadPdf(): void {
    const inventoryId = this.inventoryId;

    if (!inventoryId || this.downloadingPdf() || !this.canManageSupplies()) {
      return;
    }

    this.downloadingPdf.set(true);
    this.downloadError.set('');

    this.supplyService.downloadSupplyPdf(inventoryId).subscribe({
      next: (blob) => {
        this.saveFile(blob, `supply-report-${inventoryId}.pdf`);
        this.downloadingPdf.set(false);
      },
      error: async (err: unknown) => {
        this.downloadError.set(await this.readDownloadError(err));
        this.downloadingPdf.set(false);
      },
    });
  }

  private saveFile(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url));
  }

  private async readDownloadError(err: unknown): Promise<string> {
    // With responseType 'blob', error bodies also arrive as Blobs, so read the message from it
    if (err instanceof HttpErrorResponse && err.error instanceof Blob) {
      try {
        const body = JSON.parse(await err.error.text());
        if (typeof body?.message === 'string' && !this.isFrench) {
          return body.message;
        }
      } catch {
        // Not JSON, fall back to the generic message
      }
    }
    return $localize`:@@supply.errors.download:Impossible de télécharger le PDF.`;
  }

  // ------ Helpers ------

  // Supplies expiring within this many days are flagged as "expiring soon".
  private readonly expiringSoonDays = 30;

  protected effectiveStatus(supply: Supply): string {
    if (this.expirationState(supply) === 'expired') {
      return 'OUT_OF_STOCK';
    }
    return supply.status;
  }

  protected expirationState(supply: Supply): 'none' | 'expired' | 'soon' | 'ok' {
    if (!supply.expirationDate) {
      return 'none';
    }

    // Adding T00:00:00 makes the date local. Without it, "2026-10-20" is read as UTC,
    // which is the evening of the 19th in Montreal and would be off by one day.
    const expiration = new Date(`${supply.expirationDate}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const daysLeft = Math.round((expiration.getTime() - today.getTime()) / 86_400_000);

    if (daysLeft < 0) {
      return 'expired';
    }

    return daysLeft <= this.expiringSoonDays ? 'soon' : 'ok';
  }

  private todayAsText(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');

    return `${now.getFullYear()}-${month}-${day}`;
  }

  protected formatStatus(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return $localize`:@@supply.status.available:Disponible`;
      case 'RE_ORDER':
        return $localize`:@@supply.status.reOrder:À commander`;
      case 'OUT_OF_STOCK':
        return $localize`:@@supply.status.outOfStock:Rupture de stock`;
      default:
        return status.replaceAll('_', ' ');
    }
  }

  protected supplyPhotoUrl(supply: Supply): string | null {
    const type = supply.photoType ?? '';

    if (!supply.photoData || !(ALLOWED_IMAGE_TYPES as readonly string[]).includes(type)) {
      return null;
    }

    return `data:${type};base64,${supply.photoData}`;
  }

  private readPhoto(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = () => {
        const result = reader.result;

        if (typeof result !== 'string') {
          reject(new Error('Unable to read photo.'));
          return;
        }

        resolve(result.substring(result.indexOf(',') + 1));
      };

      reader.onerror = () => reject(new Error('Unable to read photo.'));
      reader.onabort = () => reject(new Error('Photo reading cancelled.'));
      reader.readAsDataURL(file);
    });
  }

  private async buildSupplyRequest(
    value: SupplyFormValue,
    original: Supply | null,
  ): Promise<SupplyRequest> {
    const { photo, expirationDate, supplierName, ...fields } = value;
    const normalizedSupplierName = supplierName.trim();
    const request: SupplyRequest = {
      ...fields,
      productName:
        original && value.productName === this.localizedSupplyName(original.productName)
          ? original.productName
          : value.productName,
      productDescription:
        original &&
        value.productDescription === this.localizedSupplyDescription(original.productDescription)
          ? original.productDescription
          : value.productDescription,
      expirationDate: expirationDate || null,
      ...(normalizedSupplierName ? { supplierName: normalizedSupplierName } : {}),
    };

    switch (photo.action) {
      case 'replace':
        request.photoData = await this.readPhoto(photo.file);
        request.photoType = photo.file.type;
        break;
      case 'remove':
        request.photoData = '';
        request.photoType = null;
        break;
      case 'keep':
        break; // Nothing changes in the backend
    }

    return request;
  }
}
