import { Component, computed, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MongoResult } from '@features/db-consoles/models/mongo-result';
import { AppTooltip } from '@shared/directives/app-tooltip';

interface MongoRow {
  [key: string]: unknown;
}

@Component({
  selector: 'app-mongo-results-table',
  imports: [MatTableModule, AppTooltip],
  templateUrl: './mongo-results-table.html',
  styleUrl: './mongo-results-table.css',
})
export class MongoResultsTable {
  readonly result = input.required<MongoResult>();

  protected readonly columns = computed(() => {
    const keys = new Set<string>();
    for (const doc of this.result().bson) {
      if (doc && typeof doc === 'object') {
        Object.keys(doc as MongoRow).forEach((key) => keys.add(key));
      }
    }
    return Array.from(keys);
  });

  protected readonly rows = computed<MongoRow[]>(() =>
    this.result().bson.map((doc) => (doc && typeof doc === 'object' ? (doc as MongoRow) : {})),
  );

  protected formatCell(value: unknown): string {
    if (value === undefined) {
      return '';
    }
    if (value === null) {
      return 'null';
    }
    return typeof value === 'object' ? JSON.stringify(value) : String(value);
  }
}
