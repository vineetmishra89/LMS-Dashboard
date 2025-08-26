import { Directive, ElementRef, EventEmitter, Output, OnDestroy, OnInit } from '@angular/core';

@Directive({
  selector: '[appLazyLoad]'
})
export class LazyLoadDirective implements OnInit, OnDestroy {
  @Output() inView = new EventEmitter<void>();
  
  private observer?: IntersectionObserver;

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
      rootMargin: '50px',
      threshold: 0.1
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          this.inView.emit();
          this.observer?.unobserve(entry.target);
        }
      });
    }, options);

    this.observer.observe(this.element.nativeElement);
  }
}