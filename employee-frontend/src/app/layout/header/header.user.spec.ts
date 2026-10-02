import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuHarness } from '@angular/material/menu/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';

import { Header } from './header';

@Component({
  template: '',
})
class TestSettingsComponent {}

@Component({
  template: '',
})
class TestLoginComponent {}

describe('Header', () => {
  let fixture: ComponentFixture<Header>;
  let loader: HarnessLoader;

  const authStateMock = {
    username: () => 'testuser',
    logout: vi.fn().mockReturnValue(of(undefined)),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [
        provideRouter([
          {
            path: 'settings',
            component: TestSettingsComponent,
          },
          {
            path: 'login',
            component: TestLoginComponent,
          },
        ]),
        {
          provide: AuthState,
          useValue: authStateMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    loader = TestbedHarnessEnvironment.loader(fixture);
  });

  it('should create the header', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should display the username', async () => {
    const button = fixture.nativeElement.querySelector('button[aria-label="User menu"]');

    expect(button).toBeTruthy();
    button.click();
    await fixture.whenStable();
    const menu = await loader.getHarness(MatMenuHarness);
    expect(await menu.isOpen()).toBe(true);
    const usernameElement = document.querySelector('.username');

    expect(usernameElement).toBeTruthy();
    expect(usernameElement?.textContent.trim()).toBe('testuser');
  });

  it('should have a user menu button', () => {
    const button = fixture.nativeElement.querySelector('button[aria-label="User menu"]');

    expect(button).toBeTruthy();
  });

  it('should open the user menu', async () => {
    const button = fixture.nativeElement.querySelector('button[aria-label="User menu"]');

    expect(button).toBeTruthy();

    button.click();

    await fixture.whenStable();

    const menu = await loader.getHarness(MatMenuHarness);

    expect(await menu.isOpen()).toBe(true);
  });

  it('should display Settings and Logout', async () => {
    const button = fixture.nativeElement.querySelector('button[aria-label="User menu"]');

    expect(button).toBeTruthy();

    button.click();

    await fixture.whenStable();

    const menu = await loader.getHarness(MatMenuHarness);
    const items = await menu.getItems();

    expect(items.length).toBe(2);
    const settingsItem = items[0];
    const logoutItem = items[1];

    expect(settingsItem).toBeDefined();
    expect(logoutItem).toBeDefined();

    expect(await settingsItem?.getText()).toContain('Settings');
    expect(await logoutItem?.getText()).toContain('Logout');
  });

  it('should call logout when Logout is clicked', async () => {
    const button = fixture.nativeElement.querySelector('button[aria-label="User menu"]');

    expect(button).toBeTruthy();

    button.click();

    await fixture.whenStable();

    const menu = await loader.getHarness(MatMenuHarness);
    const items = await menu.getItems();

    expect(items.length).toBe(2);

    await items[1]?.click();

    expect(authStateMock.logout).toHaveBeenCalled();
  });

  it('should navigate to Settings when Settings is clicked', async () => {
    const button = fixture.nativeElement.querySelector('button[aria-label="User menu"]');

    expect(button).toBeTruthy();

    button.click();

    await fixture.whenStable();

    const menu = await loader.getHarness(MatMenuHarness);
    const items = await menu.getItems();

    expect(items.length).toBe(2);

    await items[0]?.click();

    await fixture.whenStable();

    const router = TestBed.inject(Router);

    expect(router.url).toBe('/settings');
  });
});
