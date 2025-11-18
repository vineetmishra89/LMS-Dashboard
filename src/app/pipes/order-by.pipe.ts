// order-by.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'orderBy',
  standalone: true // If using standalone components/pipes
})
export class OrderByPipe implements PipeTransform {
  transform<T>(value: T[], property?: keyof T, direction: 'asc' | 'desc' = 'asc'): T[] {
    if (!value || value.length <= 1 || !property) {
      return value;
    }

    return [...value].sort((a, b) => {
      const valA = a[property];
      const valB = b[property];

      if (typeof valA === 'string' && typeof valB === 'string') {
        return direction === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA);
      } else if (typeof valA === 'number' && typeof valB === 'number') {
        return direction === 'asc' ? valA - valB : valB - valA;
      }
      return 0; // Fallback for other types or if properties are not comparable
    });
  }
}