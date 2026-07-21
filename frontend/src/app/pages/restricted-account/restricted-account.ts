import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

import { RestrictionItem } from '../../components/restriction-item/restriction-item';
import { AccountRestrictionsService } from '../../core/services/account-restrictions.service';

@Component({
  selector: 'app-restricted-account-page',
  standalone: true,
  imports: [MatIconModule, RestrictionItem],
  templateUrl: './restricted-account.html',
  styleUrl: './restricted-account.scss',
})
export class RestrictedAccountPage {
  private readonly router = inject(Router);
  private readonly accountRestrictions = inject(AccountRestrictionsService);

  readonly isAvailable = this.accountRestrictions.facialRecognitionResolved;

  goToFacialRecognition(): void {
    this.router.navigateByUrl('/capture');
  }
}
