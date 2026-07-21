import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'account-restrictions:facial-recognition-resolved';

@Injectable({ providedIn: 'root' })
export class AccountRestrictionsService {
  readonly facialRecognitionResolved = signal<boolean>(localStorage.getItem(STORAGE_KEY) === 'true');

  resolveFacialRecognition(): void {
    this.facialRecognitionResolved.set(true);
    localStorage.setItem(STORAGE_KEY, 'true');
  }
}
