import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderBy',
  standalone: true
})
export class OrderByPipe implements PipeTransform {
  transform(array: any[], property: string, reverse: boolean = false): any[] {
    if (!array || !property) {
      return array;
    }

    return array.sort((a, b) => {
      const aVal = this.getNestedProperty(a, property);
      const bVal = this.getNestedProperty(b, property);

      if (aVal < bVal) {
        return reverse ? 1 : -1;
      }
      if (aVal > bVal) {
        return reverse ? -1 : 1;
      }
      return 0;
    });
  }

  private getNestedProperty(obj: any, path: string): any {
    return path.split('.').reduce((o, p) => o && o[p], obj);
  }
}
