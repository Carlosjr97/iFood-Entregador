import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { RestrictedAccountPage } from './restricted-account';

describe('RestrictedAccountPage', () => {
  let fixture: ComponentFixture<RestrictedAccountPage>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RestrictedAccountPage],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(RestrictedAccountPage);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the restriction warning and the region name on the map', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Existem restrições na sua conta');
    expect(compiled.textContent).toContain('VILA SEIXAS');
    expect(compiled.textContent).toContain('Reconhecimento facial');
  });

  it('navigates to /capture when the facial recognition restriction is resolved', () => {
    spyOn(router, 'navigateByUrl');
    fixture.componentInstance.goToFacialRecognition();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/capture');
  });
});
