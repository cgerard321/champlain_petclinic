import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { VetApiService } from '../../api/vet-api.service';
import { Vet } from '../../models/vet.model';

@Component({
  selector: 'app-vet-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  encapsulation: ViewEncapsulation.None,
  template: `
    <style>
      .centered-count {
        text-align: center;
      }
    </style>
    <div>
      <h2>Veterinarians</h2>
      <br />
      <form onsubmit="void(0)" style="max-width: 20em; margin-top: 2em;">
        <div class="form-group">
          <input
            class="form-control"
            [(ngModel)]="query"
            name="query"
            placeholder="Search"
            type="text"
          />
        </div>
      </form>

      <div style="margin-bottom: 1%; margin-top: 1%">
        <label for="filterOption">Filter</label>
        <select
          id="filterOption"
          [(ngModel)]="selectedFilter"
          (change)="reloadData()"
          name="filter"
        >
          <option value="All">All</option>
          <option value="Active">Active</option>
          <option value="Inactive">Inactive</option>
          <option value="TopVets">Top Vets</option>
        </select>
      </div>
      <br />

      <table class="table table-striped">
        <thead>
          <tr>
            <th class="vet_name">Name</th>
            <th class="vet_phone">Phone</th>
            <th class="vet_email">Email</th>
            <th class="vet_speciality">Specialties</th>
            <th class="getAverageRating">Average Rating</th>
            <th class="getCountOfRatings">Number of Ratings</th>
            <th>Delete</th>
          </tr>
        </thead>

        <tr *ngFor="let vet of filteredVets; trackBy: trackByVetId">
          <td class="isActive" style="display: none">{{ vet.active }}</td>
          <td class="vet_name">
            <a style="text-decoration: none;" [routerLink]="['/vets', vet.vetId]">
              <span
                class="info v{{ vet.vetId }}"
                (mouseenter)="show($event, vet.vetId)"
                (mouseleave)="hide($event, vet.vetId)"
              >
                {{ vet.firstName }} {{ vet.lastName }}
              </span>
            </a>
          </td>
          <td class="vet_phone">
            <span>{{ vet.phoneNumber }}</span>
          </td>
          <td class="vet_email">
            <span>{{ vet.email }}</span>
          </td>
          <td class="vet_speciality">
            <span *ngFor="let specialty of vet.specialties">{{ specialty.name + ' ' }}</span>
          </td>
          <td class="vet-rating v{{ vet.vetId }} centered-count">
            <span>{{ vet.rating }}</span>
          </td>
          <td class="vet-rating v{{ vet.vetId }} centered-count">
            <span>{{ vet.count }}</span>
          </td>
          <td>
            <a class="btn btn-danger" href="javascript:void(0)" (click)="deleteVet(vet.vetId)">
              Delete Vet
            </a>
          </td>
        </tr>

        <div class="modal m{{ vet.vetId }} modalOff" *ngFor="let vet of vets">
          <span class="modal_image">
            <img *ngIf="vet.image != null" [src]="'data:image/png;base64,' + vet.image" />
            <img *ngIf="vet.image == null" src="images/vet_default.jpg" />
          </span>
          <span class="modal_name"><b>Name:</b> {{ vet.firstName }} {{ vet.lastName }}</span>
          <span class="modal_phone"><b>Phone:</b> {{ vet.phoneNumber }}</span>
          <div class="modal_specialty">
            <span class="modal_specialty_title"><b>Specialization(s):</b></span>
            <span class="modal_specialty_items" *ngFor="let specialty of vet.specialties">
              {{ specialty.name != null ? '  -' + specialty.name + ' ' : 'no specialties' }}
            </span>
          </div>
          <span class="modal_email"
            ><b>Email:</b> <br />
            {{ vet.email }}</span
          >
        </div>
      </table>
    </div>
  `,
  styles: [
    `
      @import url('/css/vets/vetTable.css');
      @import url('/css/vets/modal.css');

      .centered-count {
        text-align: center;
      }

      .table > thead > tr > th {
        background-color: #3c3834;
        color: #f1f1f1;
      }

      .vet_name a {
        color: #007bff;
        font-weight: 500;
      }

      .vet_name a:hover {
        color: #0056b3;
        text-decoration: underline !important;
      }

      .vet_name .info {
        cursor: pointer;
      }
    `,
  ],
})
export class VetListComponent implements OnInit {
  private vetApi = inject(VetApiService);

  vets: Vet[] = [];
  query: string = '';
  selectedFilter: string = 'All';

  get filteredVets(): Vet[] {
    let filtered = this.vets;

    if (this.selectedFilter === 'TopVets') {
      filtered = filtered
        .filter(vet => vet.rating !== undefined)
        .sort((a, b) => (b.rating || 0) - (a.rating || 0))
        .slice(0, 3);
    }

    if (this.query) {
      filtered = filtered.filter(
        vet =>
          vet.firstName.toLowerCase().includes(this.query.toLowerCase()) ||
          vet.lastName.toLowerCase().includes(this.query.toLowerCase()) ||
          vet.email.toLowerCase().includes(this.query.toLowerCase())
      );
    }

    return filtered;
  }

  ngOnInit(): void {
    this.loadVets();
  }

  loadVets(): void {
    this.vetApi.getAllVets().subscribe({
      next: vets => {
        this.vets = vets;
        this.vets.forEach(vet => {
          this.getAverageRating(vet);
          this.getCountOfRatings(vet);
          this.getTopThreeVetsWithHighestRating(vet);
        });
      },
      error: error => console.error('Error loading vets:', error),
    });
  }

  reloadData(): void {
    if (this.selectedFilter === 'All' || this.selectedFilter === 'TopVets') {
      this.loadVets();
      return;
    }

    this.vetApi.getFilteredVets(this.selectedFilter).subscribe({
      next: vets => {
        this.vets = vets;
        this.vets.forEach(vet => {
          this.getAverageRating(vet);
          this.getCountOfRatings(vet);
          this.getTopThreeVetsWithHighestRating(vet);
        });
      },
      error: error => console.error('Error loading filtered vets:', error),
    });
  }

  getAverageRating(vet: Vet): void {
    this.vetApi.getVetAverageRating(vet.vetId).subscribe({
      next: rating => {
        vet.rating = parseFloat(rating.toFixed(1));
        vet.showRating = true;
      },
      error: error => console.error('Error loading average rating:', error),
    });
  }

  getCountOfRatings(vet: Vet): void {
    this.vetApi.getVetRatingCount(vet.vetId).subscribe({
      next: count => {
        vet.count = count;
      },
      error: error => console.error('Error loading rating count:', error),
    });
  }

  getTopThreeVetsWithHighestRating(): void {}

  deleteVet(vetId: string): void {
    const isConfirmed = confirm('Want to delete vet with vetId:' + vetId + '. Are you sure?');
    if (isConfirmed) {
      this.vetApi.deleteVet(vetId).subscribe({
        next: () => {
          alert(vetId + ' Deleted Successfully!');
          this.loadVets();
        },
        error: error => {
          console.error('Error deleting vet:', error);
          alert('Failed to delete vet');
        },
      });
    }
  }

  show(event: MouseEvent, vetId: string): void {
    const modal = document.querySelector(`.m${vetId}`) as HTMLElement;
    if (modal) {
      const left = event.pageX;
      const top = event.clientY;

      if (document.documentElement.clientWidth > 960) {
        modal.style.left = left + 221 + 'px';
      } else if (document.documentElement.clientWidth < 420) {
        modal.style.left = '170px';
      } else if (document.documentElement.clientWidth < 510) {
        modal.style.left = left + 334.5 / 2.5 + 'px';
      } else {
        modal.style.left = left + 200 + 'px';
      }

      modal.style.top = top + 'px';
      modal.classList.remove('modalOff');
      modal.classList.add('modalOn');
    }
  }

  hide(_event: MouseEvent, vetId: string): void {
    const modal = document.querySelector(`.m${vetId}`) as HTMLElement;
    if (modal) {
      modal.classList.remove('modalOn');
      modal.classList.add('modalOff');
    }
  }

  trackByVetId(_index: number, vet: Vet): string {
    return vet.vetId;
  }
}
