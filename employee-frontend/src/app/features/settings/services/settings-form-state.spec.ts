import { signal, WritableSignal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Mock, vi } from 'vitest';

import { SettingsFormState } from './settings-form-state';

interface FakeSection {
  isDirty: WritableSignal<boolean>;
  canSave: WritableSignal<boolean>;
  save: Mock;
  cancel: Mock;
}

function fakeSection(isDirty = false, canSave = false): FakeSection {
  return {
    isDirty: signal(isDirty),
    canSave: signal(canSave),
    save: vi.fn(),
    cancel: vi.fn(),
  };
}

describe('SettingsFormState (VETS-CPC-2090)', () => {
  let state: SettingsFormState;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [SettingsFormState] });
    state = TestBed.inject(SettingsFormState);
  });

  afterEach(() => vi.restoreAllMocks());

  // 1. NEGATIVE - An empty page is clean, so the footer Save stays disabled and no leave
  it('reports nothing to save and nothing to lose when no section is registered', () => {
    // Assert
    expect(state.isDirty()).toBe(false);
    expect(state.canSave()).toBe(false); //dcan't save because enpty
  });

  // 2. POSITIVE - A registered section drives the page state.
  it('reflects the state of a registered section', () => {
    // Arrange
    const section = fakeSection(true, true);

    // Act
    state.register('username', section);

    // Assert
    expect(state.isDirty()).toBe(true);
    expect(state.canSave()).toBe(true);
  });

  // 3. POSITIVE - The computed values follow a section that changes after registration.
  it('follows a section that becomes dirty after it registered', () => {
    // Arrange
    const section = fakeSection();
    state.register('username', section);
    expect(state.isDirty()).toBe(false);

    // Act
    section.isDirty.set(true);

    // Assert
    expect(state.isDirty()).toBe(true);
  });

  // 4. POSITIVE - One dirty section out of two is enough.
  it('is dirty as soon as any section is dirty', () => {
    // Arrange
    state.register('username', fakeSection(false, false));
    state.register('display', fakeSection(true, false));

    // Assert
    expect(state.isDirty()).toBe(true);
    expect(state.canSave()).toBe(false);
  });

  // 5. POSITIVE - A section that leaves the page stops counting.
  it('forgets a section once it unregisters', () => {
    // Arrange
    const section = fakeSection(true, true);
    state.register('username', section);

    // Act
    state.unregister('username');

    // Assert
    expect(state.isDirty()).toBe(false);
    expect(state.canSave()).toBe(false);
  });

  // 6. POSITIVE - Save drives every section that is ready.
  it('saves the sections that can be saved', () => {
    // Arrange
    const ready = fakeSection(true, true);
    state.register('username', ready);

    // Act
    state.saveAll();

    // Assert
    expect(ready.save).toHaveBeenCalledTimes(1);
  });

  // 7. NEGATIVE - Save never submits a section that is not ready.

  it('leaves a section that cannot be saved alone', () => {
    // Arrange
    const ready = fakeSection(true, true);
    const notReady = fakeSection(true, false);
    state.register('username', notReady);
    state.register('display', ready);

    // Act
    state.saveAll();

    // Assert
    expect(notReady.save).not.toHaveBeenCalled();
    expect(ready.save).toHaveBeenCalledTimes(1);
  });

  // 8. POSITIVE - Cancel restores every section, ready or not.
  it('cancels every section', () => {
    // Arrange
    const first = fakeSection(true, true);
    const second = fakeSection(true, false);
    state.register('username', first);
    state.register('display', second);

    // Act
    state.cancelAll();

    // Assert
    expect(first.cancel).toHaveBeenCalledTimes(1);
    expect(second.cancel).toHaveBeenCalledTimes(1);
  });

  // 9. POSITIVE - Registering twice under the same id replaces the entry.
  it('replaces a section registered twice under the same id', () => {
    // Arrange
    const first = fakeSection(true, true);
    const second = fakeSection(false, false);

    // Act
    state.register('username', first);
    state.register('username', second);
    state.saveAll();

    // Assert
    expect(state.isDirty()).toBe(false);
    expect(first.save).not.toHaveBeenCalled();
  });
});
