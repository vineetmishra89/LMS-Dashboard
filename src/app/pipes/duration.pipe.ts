import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'duration',
  standalone: true
})
export class DurationPipe implements PipeTransform {

transform(minutes: number, format: 'short' | 'long' = 'short'): string {
    if (!minutes || minutes < 0) return '0m';
    
    const hours = Math.floor(minutes / 60);
    const mins = Math.floor(minutes % 60);
    
    if (format === 'long') {
      if (hours > 0) {
        return `${hours} hour${hours > 1 ? 's' : ''} ${mins} minute${mins !== 1 ? 's' : ''}`;
      }
      return `${mins} minute${mins !== 1 ? 's' : ''}`;
    }
    
    // Short format
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  }

}
