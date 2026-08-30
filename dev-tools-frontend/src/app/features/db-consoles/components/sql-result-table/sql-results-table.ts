import { Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { SqlResult } from '@features/db-consoles/models/sql-result';
import { AppTooltip } from '@shared/directives/app-tooltip';

@Component({
  selector: 'app-sql-results-table',
  imports: [MatTableModule, AppTooltip],
  templateUrl: './sql-results-table.html',
  styleUrl: './sql-results-table.css',
})
export class SqlResultsTable {
  readonly result = input.required<SqlResult>();
}
