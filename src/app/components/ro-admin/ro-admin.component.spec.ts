import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoAdminComponent } from './ro-admin.component';

describe('RoAdminComponent', () => {
  let component: RoAdminComponent;
  let fixture: ComponentFixture<RoAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoAdminComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RoAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
