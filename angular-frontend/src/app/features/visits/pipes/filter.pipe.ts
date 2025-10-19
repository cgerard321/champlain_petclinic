import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter',
  standalone: true
})
export class FilterPipe implements PipeTransform {
  transform(array: any[], searchObj: any, _query: string): any[] {
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

        return itemValue.toString().toLowerCase().includes(searchValue.toLowerCase());
      });
    });
  }

  private getNestedProperty(obj: any, path: string): any {
    return path.split('.').reduce((o, p) => o && o[p], obj);
  }
}
