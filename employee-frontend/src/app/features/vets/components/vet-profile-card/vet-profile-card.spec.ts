import { TestBed } from '@angular/core/testing';

import { VetProfileCard } from '@features/vets/components/vet-profile-card/vet-profile-card';
import { VetProfile } from '@features/vets/models/vet-profile.model';

describe('VetProfileCard', () => {
  const profile: VetProfile = {
    vetId: 'test-vet',
    firstName: 'Alex',
    lastName: 'Morin',
    active: true,
    specialties: ['Chirurgie', 'Dentisterie'],
    biography: 'Une biographie de test.',
    email: 'alex@example.com',
    phoneNumber: '450 555-0101',
    workingHours: ['Lundi', '9 h – 17 h'],
  };

  it('displays the supplied profile and updates when its input changes', () => {
    const fixture = TestBed.createComponent(VetProfileCard);
    fixture.componentRef.setInput('vet', profile);
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;

    for (const text of [
      'Alex Morin',
      'AM',
      'Chirurgie',
      'Dentisterie',
      'Une biographie de test.',
      'alex@example.com',
      '450 555-0101',
      'Lundi',
      '9 h – 17 h',
      'Profil actif',
    ]) {
      expect(element.textContent).toContain(text);
    }

    fixture.componentRef.setInput('vet', {
      ...profile,
      firstName: 'Sam',
      lastName: 'Tremblay',
      active: false,
    });
    fixture.detectChanges();

    expect(element.querySelector('h2')?.textContent).toContain('Sam Tremblay');
    expect(element.querySelector('.avatar')?.textContent).toBe('ST');
    expect(element.textContent).toContain('Profil inactif');
    expect(element.textContent).not.toContain('Alex Morin');
  });

  it('keeps missing information understandable without displaying undefined values', () => {
    const fixture = TestBed.createComponent(VetProfileCard);
    fixture.componentRef.setInput('vet', {
      vetId: 'incomplete-vet',
      firstName: 'Sam',
      lastName: 'Tremblay',
      active: false,
      specialties: [],
      workingHours: [],
    } satisfies VetProfile);
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).toContain('Aucune spécialité renseignée.');
    expect(element.textContent).toContain('Biographie non renseignée.');
    expect(element.textContent).toContain('Horaire non renseigné.');
    expect(
      Array.from(element.querySelectorAll('dd'), (detail) => detail.textContent?.trim()),
    ).toEqual(['Courriel non renseigné', 'Téléphone non renseigné']);
    expect(element.textContent).not.toContain('undefined');
  });
});
