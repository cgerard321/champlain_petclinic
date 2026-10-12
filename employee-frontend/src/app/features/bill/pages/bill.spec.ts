import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Roles } from '@shared/models/roles';

import { Bill } from './bill';

describe('Bill page', () => {
  async function render(roles: string[] = []): Promise<HTMLElement> {
    const hasRole = vi.fn((role: string) => roles.includes(role));

    await TestBed.configureTestingModule({
      imports: [Bill],
      providers: [{ provide: AuthState, useValue: { hasRole } }],
    }).compileComponents();

    const fixture = TestBed.createComponent(Bill);
    fixture.detectChanges();

    return fixture.nativeElement as HTMLElement;
  }

  it('renders the basic Bill page UI', async () => {
    const host = await render();

    expect(host.querySelector('h1')?.textContent).toContain('Bill');
    expect(host.querySelector('h2')?.textContent).toContain('Bill overview');
    expect(host.querySelector('.bill-empty-state')?.textContent).toContain('No bills to display');
  });

  it('shows Create Bill to veterinarians', async () => {
    const host = await render([Roles.vet]);

    expect(host.querySelector('button')?.textContent).toContain('Create Bill');
  });

  it('hides Create Bill from other employee roles', async () => {
    const host = await render([Roles.receptionist]);

    expect(host.querySelector('button')).toBeNull();
  });
});
