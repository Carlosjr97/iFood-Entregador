import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RestrictionItem } from './restriction-item';

describe('RestrictionItem', () => {
  let fixture: ComponentFixture<RestrictionItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RestrictionItem],
    }).compileComponents();

    fixture = TestBed.createComponent(RestrictionItem);
    fixture.componentInstance.icon = 'block';
    fixture.componentInstance.title = 'Reconhecimento facial';
    fixture.componentInstance.description = 'Descrição de teste.';
    fixture.detectChanges();
  });

  it('renders the provided title and description', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Reconhecimento facial');
    expect(compiled.textContent).toContain('Descrição de teste.');
  });

  it('defaults the action label to "Resolver"', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.resolve-link')?.textContent?.trim()).toBe('Resolver');
  });

  it('emits resolve when the action link is clicked', () => {
    let emitted = false;
    fixture.componentInstance.resolve.subscribe(() => (emitted = true));
    const button = fixture.nativeElement.querySelector('.resolve-link') as HTMLButtonElement;
    button.click();
    expect(emitted).toBeTrue();
  });
});
