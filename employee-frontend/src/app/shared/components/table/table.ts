import { NgTemplateOutlet } from '@angular/common';
import { Component, input, TemplateRef } from '@angular/core';

/** Use a template for custom cell markup, or a value callback for plain text. */
export interface TableColumn<T> {
  id: string;
  header: string;
  value?: (row: T) => string | number;
  template?: TemplateRef<{ $implicit: T }>;
}

@Component({
  imports: [NgTemplateOutlet],
  selector: 'app-table',
  styleUrls: ['./table.css'],
  templateUrl: './table.html',
})
export class Table<T> {
  readonly label = input.required<string>();
  readonly rows = input.required<readonly T[]>();
  readonly columns = input.required<readonly TableColumn<T>[]>();
  readonly rowId = input.required<(row: T) => string>();
}
