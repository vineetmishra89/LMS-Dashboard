import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'progress',
  standalone: true
})
export class ProgressPipe implements PipeTransform {

 transform(value: number, format: 'percentage' | 'fraction' | 'bar' = 'percentage'): string {
    if (value < 0 || value > 100) return '0%';
    
    switch (format) {
      case 'fraction':
        return `${Math.round(value)}/100`;
      case 'bar':
        return `[${'='.repeat(Math.round(value / 5))}${'.'.repeat(20 - Math.round(value / 5))}]`;
      default:
        return `${Math.round(value)}%`;
    }
  }

}
