import { VetProfile } from '@features/vets/models/vet-profile.model';

// Fictional presentation data for CPC-2094, explicitly labelled in the page.
// I am going to replace it with the VETS service when implementing CPC-2095.
export const VET_PROFILE_PREVIEWS: readonly VetProfile[] = [
  {
    vetId: 'preview-alex-morin',
    firstName: 'Alex',
    lastName: 'Morin',
    active: true,
    specialties: [
      $localize`:@@vets.preview.general_medicine:Médecine générale`,
      $localize`:@@vets.preview.preventive_care:Soins préventifs`,
    ],
    email: 'alex.morin@example.com',
    phoneNumber: '450 555-0101',
    biography: $localize`:@@vets.preview.alex_biography:Accompagne les familles dans les soins quotidiens de leurs animaux, de la prévention aux bilans de santé.`,
    workingHours: [
      $localize`:@@vets.preview.alex_days:Lundi, mercredi et vendredi`,
      $localize`:@@vets.preview.alex_hours:9 h – 17 h`,
    ],
  },
  {
    vetId: 'preview-julie-lambert',
    firstName: 'Julie',
    lastName: 'Lambert',
    active: true,
    specialties: [
      $localize`:@@vets.preview.surgery:Chirurgie`,
      $localize`:@@vets.preview.dentistry:Dentisterie`,
    ],
    email: 'julie.lambert@example.com',
    phoneNumber: '450 555-0102',
    biography: $localize`:@@vets.preview.julie_biography:Se consacre aux soins chirurgicaux et dentaires, avec une attention particulière au confort et au rétablissement des animaux.`,
    workingHours: [
      $localize`:@@vets.preview.julie_days:Mardi et jeudi`,
      $localize`:@@vets.preview.julie_hours:8 h – 16 h`,
    ],
  },
  {
    vetId: 'preview-sam-tremblay',
    firstName: 'Sam',
    lastName: 'Tremblay',
    active: false,
    specialties: [$localize`:@@vets.preview.dermatology:Dermatologie`],
    email: 'sam.tremblay@example.com',
    biography: $localize`:@@vets.preview.sam_biography:Aide les animaux souffrant de problèmes de peau et accompagne leurs familles dans le suivi des soins.`,
    workingHours: [],
  },
];
