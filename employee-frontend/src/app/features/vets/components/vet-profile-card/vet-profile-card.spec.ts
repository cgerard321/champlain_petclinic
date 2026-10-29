
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

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

  beforeEach(async () => {
    const componentDirectory =
      'src/app/features/vets/components/vet-profile-card';

    const template = readFileSync(
      resolve(process.cwd(), componentDirectory, 'vet-profile-card.html'),
      'utf8',
    );

    await TestBed.configureTestingModule({})
      .overrideComponent(VetProfileCard, {
        set: {
          templateUrl: undefined,
          styleUrl: undefined,
          template,
          styles: [],
        },
      })
      .compileComponents();
  });

  it('displays the supplied profile and updates when its input changes', () => {
    const fixture = TestBed.createComponent(VetProfileCard);

    const vetSignal = signal<VetProfile>(profile);

    Object.defineProperty(fixture.componentInstance, 'vet', {
      value: vetSignal,
      configurable: true,
    });

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

    vetSignal.set({
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

    const incompleteProfile: VetProfile = {
      vetId: 'incomplete-vet',
      firstName: 'Sam',
      lastName: 'Tremblay',
      active: false,
      specialties: [],
      workingHours: [],
    };

    Object.defineProperty(fixture.componentInstance, 'vet', {
      value: signal<VetProfile>(incompleteProfile),
      configurable: true,
    });

    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;

    expect(element.textContent).toContain('Aucune spécialité renseignée.');
    expect(element.textContent).toContain('Biographie non renseignée.');
    expect(element.textContent).toContain('Horaire non renseigné.');

    expect(
      Array.from(
        element.querySelectorAll('dd'),
        (detail) => detail.textContent?.trim(),
      ),
    ).toEqual([
      'Courriel non renseigné',
      'Téléphone non renseigné',
    ]);

    expect(element.textContent).not.toContain('undefined');
  });
});
