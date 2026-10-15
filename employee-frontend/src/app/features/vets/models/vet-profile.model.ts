/** Presentation model for a profile; the API adapter belongs to CPC-2095. */
export interface VetProfile {
  readonly vetId: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly active: boolean;
  readonly specialties: readonly string[];
  readonly email?: string;
  readonly phoneNumber?: string;
  readonly biography?: string;
  readonly workingHours: readonly string[];
}
