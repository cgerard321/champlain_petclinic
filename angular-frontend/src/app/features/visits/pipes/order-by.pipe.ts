import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderBy',
  standalone: true,
})
export class OrderByPipe implements PipeTransform {
  transform<T extends Record<string, unknown>>(
    array: T[],
    property: string,
    reverse: boolean = false
  ): T[] {
    if (!array || !property) {
      return array;
    }

    return array.sort((a, b) => {
      const aVal = this.getNestedProperty(a, property);
      const bVal = this.getNestedProperty(b, property);

      if (String(aVal) < String(bVal)) {
        return reverse ? 1 : -1;
      }
      if (String(aVal) > String(bVal)) {
        return reverse ? -1 : 1;
      }
      return 0;
    });
  }

  private getNestedProperty(obj: Record<string, unknown>, path: string): unknown {
    return path
      .split('.')
      .reduce((o: unknown, p: string) => o && (o as Record<string, unknown>)[p], obj);
  }
}
