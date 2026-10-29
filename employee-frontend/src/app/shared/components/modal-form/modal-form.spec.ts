import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { describe, expect, it, beforeEach, vi } from 'vitest';

import { ModalForm } from './modal-form';
import { ModalFormData, ModalFormResult } from './modal-form-models';

interface TestFormValue {
  name: string;
  age: number;
}

describe('ModalForm', () => {
  let dialogRef: {
    close: ReturnType<typeof vi.fn>;
  };

  const data: ModalFormData<TestFormValue> = {
    title: 'Test Form',
    fields: [
      {
        key: 'name',
        label: 'Name',
        type: 'string',
        required: true,
      },
      {
        key: 'age',
        label: 'Age',
        type: 'number',
        required: true,
        min: 18,
      },
    ],
    initialValue: {
      name: 'John',
      age: 25,
    },
  };

  beforeEach(() => {
    dialogRef = {
      close: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [ModalForm],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: data,
        },
        {
          provide: MatDialogRef,
          useValue: dialogRef,
        },
      ],
    });
  });

  it('should render the initial field values', () => {
    const fixture = TestBed.createComponent(ModalForm<TestFormValue>);
    fixture.detectChanges();

    const inputs = fixture.nativeElement.querySelectorAll('input');

    expect(inputs[0].value).toBe('John');
    expect(inputs[1].value).toBe('25');
  });

  it('should prevent submission when required fields are invalid', () => {
    const fixture = TestBed.createComponent(ModalForm<TestFormValue>);
    fixture.detectChanges();

    const nameInput = fixture.nativeElement.querySelector('#name');

    nameInput.value = '';
    nameInput.dispatchEvent(new Event('input'));
    nameInput.dispatchEvent(new Event('blur'));

    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('.dialog-submit-button');

    submitButton.click();

    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('should submit the entered form values', () => {
    const fixture = TestBed.createComponent(ModalForm<TestFormValue>);
    fixture.detectChanges();

    const nameInput = fixture.nativeElement.querySelector('#name');
    const ageInput = fixture.nativeElement.querySelector('#age');

    nameInput.value = 'Jane';
    nameInput.dispatchEvent(new Event('input'));

    ageInput.value = '30';
    ageInput.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('.dialog-submit-button');

    submitButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith({
      value: {
        name: 'Jane',
        age: 30,
      },
    } satisfies ModalFormResult<TestFormValue>);
  });

  it('should close the dialog without a result when cancelled', () => {
    const fixture = TestBed.createComponent(ModalForm<TestFormValue>);
    fixture.detectChanges();

    const cancelButton = fixture.nativeElement.querySelector('.dialog-cancel-button');

    cancelButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith();
  });
});
