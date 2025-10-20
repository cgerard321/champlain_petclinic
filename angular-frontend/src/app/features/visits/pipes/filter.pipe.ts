import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter',
  standalone: true,
})
export class FilterPipe implements PipeTransform {
  transform<T extends Record<string, unknown>>(
    array: T[],
    searchObj: Record<string, unknown>
  ): T[] {
    if (!array || !searchObj) {
      return array;
    }

    return array.filter(item => {
      return Object.keys(searchObj).every(key => {
        const searchValue = searchObj[key];
        if (!searchValue) {
          return true; // If no search value, include the item
        }

        const itemValue = this.getNestedProperty(item, key);
        if (itemValue === null || itemValue === undefined) {
          return false;
        }

        return itemValue.toString().toLowerCase().includes(String(searchValue).toLowerCase());
      });
    });
  }

  private getNestedProperty(obj: Record<string, unknown>, path: string): unknown {
    return path
      .split('.')
      .reduce((o: unknown, p: string) => o && (o as Record<string, unknown>)[p], obj);
  }
}
