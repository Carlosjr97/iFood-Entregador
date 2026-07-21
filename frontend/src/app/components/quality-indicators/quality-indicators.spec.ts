import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FrameAnalysis } from '../../core/models/frame-analysis.model';
import { QualityIndicators } from './quality-indicators';

function buildAnalysis(overrides: Partial<FrameAnalysis>): FrameAnalysis {
  return {
    faceDetected: true,
    brightness: 60,
    blur: 90,
    contrast: 70,
    centered: true,
    distance: 'good',
    faceSize: 0.35,
    boundingBox: { x: 10, y: 10, width: 100, height: 120 },
    score: 90,
    warnings: [],
    ...overrides,
  };
}

describe('QualityIndicators', () => {
  let fixture: ComponentFixture<QualityIndicators>;
  let component: QualityIndicators;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QualityIndicators],
    }).compileComponents();

    fixture = TestBed.createComponent(QualityIndicators);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('returns no indicators when no face is detected', () => {
    component.analysis = buildAnalysis({ faceDetected: false });
    expect(component.indicators).toEqual([]);
  });

  it('marks brightness as good when within the ideal band', () => {
    component.analysis = buildAnalysis({ brightness: 60 });
    const brightness = component.indicators.find((i) => i.label === 'Brilho');
    expect(brightness?.level).toBe('good');
  });

  it('marks brightness as bad when far outside the ideal band', () => {
    component.analysis = buildAnalysis({ brightness: 5 });
    const brightness = component.indicators.find((i) => i.label === 'Brilho');
    expect(brightness?.level).toBe('bad');
  });

  it('reports the overall level from the score', () => {
    component.analysis = buildAnalysis({ score: 20 });
    expect(component.overallLevel).toBe('bad');

    component.analysis = buildAnalysis({ score: 80 });
    expect(component.overallLevel).toBe('good');
  });

  it('flags a non-centered face as a warn-level indicator', () => {
    component.analysis = buildAnalysis({ centered: false });
    const framing = component.indicators.find((i) => i.label === 'Enquadramento');
    expect(framing?.level).toBe('warn');
    expect(framing?.value).toBe('Descentralizado');
  });

  it('shows a waiting status before any face is detected', () => {
    component.analysis = null;
    expect(component.status).toBe('waiting');
  });

  it('shows a suggestion status with the first warning while checks are failing', () => {
    component.analysis = buildAnalysis({
      warnings: ['Ambiente muito escuro. Procure um local mais iluminado.', 'Centralize o rosto no quadro.'],
    });
    expect(component.status).toBe('suggestion');
    expect(component.statusMessage).toBe('Ambiente muito escuro. Procure um local mais iluminado.');
  });

  it('shows a success status once a face is detected with no warnings', () => {
    component.analysis = buildAnalysis({ warnings: [] });
    expect(component.status).toBe('success');
    expect(component.statusMessage).toContain('sucesso');
  });
});
