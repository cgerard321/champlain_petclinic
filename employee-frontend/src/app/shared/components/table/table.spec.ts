import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Table, TableColumn } from './table';

interface TestRow {
  id: string;
  name: string;
  quantity: number;
}

describe('Table', () => {
  let fixture: ComponentFixture<Table<TestRow>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Table],
    }).compileComponents();

    fixture = TestBed.createComponent(Table<TestRow>);

    const rows: TestRow[] = [{ id: '1', name: 'Bandages', quantity: 3 }];
    const columns: TableColumn<TestRow>[] = [
      { id: 'name', header: 'Name', value: (row) => row.name },
      { id: 'quantity', header: 'Quantity', value: (row) => row.quantity },
    ];

    fixture.componentRef.setInput('label', 'Test supplies');
    fixture.componentRef.setInput('rows', rows);
    fixture.componentRef.setInput('columns', columns);
    fixture.componentRef.setInput('rowId', (row: TestRow) => row.id);

    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders its label, headers, and row values', () => {
    const table = fixture.nativeElement.querySelector('table') as HTMLTableElement;

    expect(table.getAttribute('aria-label')).toBe('Test supplies');
    expect(Array.from(table.querySelectorAll('th'), (cell) => cell.textContent?.trim())).toEqual([
      'Name',
      'Quantity',
    ]);

    expect(
      Array.from(table.querySelectorAll('tbody td'), (cell) => cell.textContent?.trim()),
    ).toEqual(['Bandages', '3']);
  });
});
