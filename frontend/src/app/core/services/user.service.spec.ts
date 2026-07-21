import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { User } from '../models/user.model';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const sampleUser: User = { id: 1, name: 'Ana', email: 'ana@example.com', createdAt: '2026-01-01T00:00:00Z' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.removeItem('biometric.currentUser');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('biometric.currentUser');
  });

  it('posts name and email when creating or fetching a user', () => {
    service.createOrGetUser('Ana', 'ana@example.com').subscribe((user) => {
      expect(user).toEqual(sampleUser);
    });

    const req = httpMock.expectOne('/api/users');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Ana', email: 'ana@example.com' });
    req.flush(sampleUser);
  });

  it('returns null when no user is stored', () => {
    expect(service.getStoredUser()).toBeNull();
  });

  it('round-trips a user through localStorage', () => {
    service.storeUser(sampleUser);
    expect(service.getStoredUser()).toEqual(sampleUser);

    service.clearStoredUser();
    expect(service.getStoredUser()).toBeNull();
  });
});
