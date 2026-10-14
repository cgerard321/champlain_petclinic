import { computed, Injectable, Signal, signal } from '@angular/core';

export interface SettingsSectionForm {
  isDirty: Signal<boolean>;
  canSave: Signal<boolean>;
  save(): void;
  cancel(): void;
}
@Injectable()
export class SettingsFormState {
  private readonly sections = signal<ReadonlyMap<string, SettingsSectionForm>>(new Map());

  readonly isDirty = computed(() =>
    [...this.sections().values()].some((section) => section.isDirty()),
  );

  readonly canSave = computed(() =>
    [...this.sections().values()].some((section) => section.canSave()),
  );
  register(id: string, section: SettingsSectionForm): void {
    this.sections.update((current) => new Map(current).set(id, section));
  }
  unregister(id: string): void {
    this.sections.update((current) => {
      const next = new Map(current);
      next.delete(id);

      return next;
    });
  }
  saveAll(): void {
    for (const section of this.sections().values()) {
      if (section.canSave()) {
        section.save();
      }
    }
  }

  cancelAll(): void {
    for (const section of this.sections().values()) {
      section.cancel();
    }
  }
}
