import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  imports: [],
  selector: 'app-supply-page',
  templateUrl: './supply-page.html',
  styleUrl: './supply-page.css',
})
// This supply page is only a placeholder used to test functionality on the inventory page
// feel free to replace it if you wish
export class SupplyPage {
  private readonly route = inject(ActivatedRoute);
  protected readonly inventoryId = this.route.snapshot.paramMap.get('inventoryId');
}
