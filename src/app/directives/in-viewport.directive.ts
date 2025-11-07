import { Directive, ElementRef, EventEmitter, Output, OnDestroy, OnInit, Input } from '@angular/core';

@Directive({
  selector: '[appInViewport]'
})
export class InViewportDirective implements OnInit, OnDestroy {
  @Input() threshold: number = 0.5;
  @Input() rootMargin: string = '0px';
  
  @Output() enterViewport = new EventEmitter<void>();
  @Output() exitViewport = new EventEmitter<void>();
  
  private observer?: IntersectionObserver;
  private isInViewport = false;

  constructor(private element: ElementRef) {}

  ngOnInit(): void {
    this.createObserver();
  }

  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  private createObserver(): void {
    const options = {
      root: null,
      rootMargin: this.rootMargin,
      threshold: this.threshold
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !this.isInViewport) {
          this.isInViewport = true;
          this.enterViewport.emit();
        } else if (!entry.isIntersecting && this.isInViewport) {
          this.isInViewport = false;
          this.exitViewport.emit();
        }
      });
    }, options);

    this.observer.observe(this.element.nativeElement);
  }
}
