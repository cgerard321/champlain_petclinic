import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { VetApiService } from '../../api/vet-api.service';

@Component({
  selector: 'app-vet-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="bg-light">
      <div class="container">
        <div class="py-5 text-center"></div>
        <h2 class="titleVetForm" id="title">{{ isEdit ? 'Edit Vet' : 'New Vet Sign Up' }}</h2>
        <p class="lead">Welcome to our team, please fill out the form below ensuring to fill all sections.</p>
      </div>
      <div class="container">
        <div class="py-5 text-center"></div>
      <div class="row g-5">
            <div class="col-md-7 col-lg-12">
              <form (ngSubmit)="submitVetForm(vet)">
                <div class="row g-3">
                  <div class="col-sm-6">
                    <h5 class="form-label">Firstname</h5>
                    <input class="form-control" id="firstName"
                           [(ngModel)]="vet.firstName" [ngModelOptions]="{standalone: true}"
                           placeholder="First Name" required
                           title="Minimum of 3 characters and maximum of 30 characters. Only letters and punctuation mark."
                           type="text">
                  </div>

                  <div class="col-sm-6">
                    <h5 class="form-label">Lastname</h5>
                    <input class="form-control" id="lastName"
                           [(ngModel)]="vet.lastName" [ngModelOptions]="{standalone: true}"
                           pattern="^[a-zA-Z -]+" placeholder="Last Name" required
                           title="Minimum of 2 characters and maximum of 30 characters. Only letters and punctuation mark."
                           type="text">
                  </div>

                  <div class="col-sm-12">
                    <h5 class="form-label">Phone Number</h5>
                    <input class="form-control" id="phoneNumber"
                           [(ngModel)]="vet.phoneNumber" [ngModelOptions]="{standalone: true}"
                           placeholder="Phone Number" required
                           title="Enter phone number" type="text">
                  </div>

                  <hr class="my-4"/>

                  <div class="parent">
                    <img alt="Profile picture preview" [src]="vetPhoto?.photo ? 'data:image/*;base64,' + vetPhoto.photo : '/images/vet_default.jpg'" width="150" height="150" class="image1">
                    <img alt="" [src]="previewImage ? 'data:image/jpeg;base64,' + previewImage : null" *ngIf="previewImage" width="150" height="150" class="image2"/>
                    <input type="file" accept="image/jpeg" class="fileInput" id="photoVet" (change)="onFileSelected($event)"/>
                  </div>
                      
                  <hr class="my-4"/>

                  <div class="col-sm-12">
                    <h5 class="form-label">Career Resume</h5>
                    <textarea class="form-control" id="vetResume"
                              [(ngModel)]="vet.resume" [ngModelOptions]="{standalone: true}"
                              pattern="^(.*)" placeholder="Resume">
                    </textarea>
                  </div>

                  <hr class="my-4"/>

                  <div class="row gy-3">
                    <div class="col-md-12">
                      <h5 class="mb-3">Specialty</h5>
                      <div class="form-check">
                        <input class="form-check-input specialty" id="radiology" name="radiology"
                               type="checkbox"
                               value='{"id":1, "specialtyId":100001, "name":"radiology"}'>
                        <label class="form-check-label vet-label-input" for="radiology">Radiology</label>
                      </div>

                      <div class="form-check">
                        <input class="form-check-input specialty" id="surgery" name="surgery"
                               type="checkbox"
                               value='{"id":2, "specialtyId":100002, "name":"surgery"}'>
                        <label class="form-check-label vet-label-input" for="surgery">Surgery</label>
                      </div>

                      <div class="form-check">
                        <input class="form-check-input specialty" id="dentistry" name="dentistry"
                               type="checkbox"
                               value='{"id":3, "specialtyId":100003, "name":"dentistry"}'>
                        <label class="form-check-label vet-label-input" for="dentistry">Dentistry</label>
                      </div>

                      <div class="form-check">
                        <input class="form-check-input specialty" id="general" name="general"
                               type="checkbox"
                               value='{"id":4, "specialtyId":100004, "name":"general"}'>
                        <label class="form-check-label vet-label-input" for="general">General</label>
                      </div>
                    </div>
                  </div>

                  <div class="col-sm-6">
                    <div class="my-3 check-align">
                      <h5 class="mb-3">Available Work Days</h5>
                      <div class="form-check">
                        <input class="form-check-input workday" id="Monday" name="Monday" type="checkbox" value="Monday">
                        <label class="form-check-label vet-label-input" for="Monday">Monday</label>
                      </div>
                      <div class="form-check">
                        <input class="form-check-input workday" id="Tuesday" name="Tuesday" type="checkbox" value="Tuesday">
                        <label class="form-check-label vet-label-input" for="Tuesday">Tuesday</label>
                      </div>
                      <div class="form-check">
                        <input class="form-check-input workday" id="Wednesday" name="Wednesday" type="checkbox" value="Wednesday">
                        <label class="form-check-label vet-label-input" for="Wednesday">Wednesday</label>
                      </div>
                      <div class="form-check">
                        <input class="form-check-input workday" id="Thursday" name="Thursday" type="checkbox" value="Thursday">
                        <label class="form-check-label vet-label-input" for="Thursday">Thursday</label>
                      </div>
                      <div class="form-check">
                        <input class="form-check-input workday" id="Friday" name="Friday" type="checkbox" value="Friday">
                        <label class="form-check-label vet-label-input" for="Friday">Friday</label>
                      </div>
                    </div>
                  </div>

                  <div class="my-3 check-align">
                    <h5 class="mb-3">Active Vet</h5>
                    <div class="form-check">
                      <label class="form-check-label vet-label-input">
                        <input class="form-check-input isActiveRadio" name="active" [(ngModel)]="vet.active"
                               type="radio" [value]="true">Yes
                      </label>
                    </div>
                    <div class="form-check">
                      <label class="form-check-label vet-label-input">
                        <input class="form-check-input isActiveRadio" name="active" [(ngModel)]="vet.active"
                               type="radio" [value]="false">No
                      </label>
                    </div>
                  </div>

                  <div id="user-info" *ngIf="!isEdit">
                    <div class="form-group">
                      <label for="username">Username</label>
                      <input id="username" class="form-control" [(ngModel)]="vet.username" [ngModelOptions]="{standalone: true}" required />
                      <span *ngIf="!vet.username || vet.username === ''" class="help-block">Username is required.</span>
                    </div>

                    <div id="email-info" class="col-12">
                      <h5 class="form-label">Email Address</h5>
                      <input class="form-control" id="email"
                             [(ngModel)]="vet.email" [ngModelOptions]="{standalone: true}"
                             pattern="\b[\w.%-]+@[-.\w]+\.[A-Za-z]{2,3}\b" placeholder="Email"
                             required
                             title="Minimum of 6 characters and maximum of 320 characters. Top level domain should have 2 to 3 characters."
                             type="email">
                    </div>

                    <div class="form-group">
                      <label for="password">Password</label>
                      <input id="password" class="form-control" [(ngModel)]="vet.password" [ngModelOptions]="{standalone: true}" type="password" required />
                      <span *ngIf="!vet.password || vet.password === ''" class="help-block">Password is required.</span>
                    </div>
                  </div>

                  <hr class="my-4"/>
                  <button class="w-100 btn btn-primary btn-lg" type="submit">
                    Submit
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
  `,
  styles: [`
    @import url('/css/vets/form.css');
  `]
})
export class VetFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private vetApi = inject(VetApiService);

  vetId: string | null = null;
  isEdit = false;
  vet: any = {
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    resume: '',
    workday: '',
    specialties: [],
    username: '',
    password: '',
    active: true
  };
  vetPhoto: any = null;
  previewImage: string | null = null;

  ngOnInit(): void {
    this.vetId = this.route.snapshot.paramMap.get('vetId');
    this.isEdit = !!this.vetId;
    
    if (this.isEdit && this.vetId) {
      this.vetApi.getVetById(this.vetId).subscribe({
        next: (vet) => {
          this.vet = vet;
        },
        error: () => alert('Failed to load vet')
      });
    }
  }

  submitVetForm(vet: any): void {
    const specialties = this.getSelectedSpecialties();
    if (specialties.length === 0) {
      alert("vet should have at least one specialty");
      return;
    }

    const workdays = this.getSelectedWorkdays();
    if (workdays.length === 0) {
      alert("vet should have at least one workday");
      return;
    }

    const namePattern = /^[a-zA-Z -]+/;
    if (!namePattern.test(vet.firstName) || vet.firstName.length > 30 || vet.firstName.length < 2) {
      alert("first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " + vet.firstName);
      return;
    }

    if (!namePattern.test(vet.lastName) || vet.lastName.length > 30 || vet.lastName.length < 2) {
      alert("last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " + vet.lastName);
      return;
    }

    if (!vet.phoneNumber || vet.phoneNumber.trim() === '') {
      alert("Phone number is required");
      return;
    }

    if (vet.resume.length < 10) {
      alert("resume should be minimum 10 characters: " + vet.resume);
      return;
    }

    const vetData = {
      firstName: vet.firstName,
      lastName: vet.lastName,
      email: vet.email,
      phoneNumber: vet.phoneNumber,
      resume: vet.resume,
      workday: workdays,
      specialties: specialties,
      active: vet.active
    };

    if (this.isEdit && this.vetId) {
      this.vetApi.updateVet(this.vetId, vetData).subscribe({
        next: (updatedVet) => {
          this.updatePhoto(updatedVet.vetId);
        },
        error: (error) => {
          console.error('Error updating vet:', error);
          alert('Invalid vet fields');
          this.router.navigate(['/vets']);
        }
      });
    } else {
      const createData = {
        username: vet.username,
        password: vet.password,
        email: vet.email,
        vet: vetData
      };

      this.vetApi.createVetUser(createData).subscribe({
        next: (createdVet) => {
          this.uploadPhoto(createdVet.vetId);
        },
        error: (error) => {
          console.error('Error creating vet:', error);
          alert('Invalid vet profile picture');
          this.router.navigate(['/vets']);
        }
      });
    }
  }

  getSelectedWorkdays(): string[] {
    const workdays: string[] = [];
    const workdayInputs = document.querySelectorAll('input.workday:checked');
    workdayInputs.forEach((input: any) => {
      workdays.push(input.value);
    });
    return workdays;
  }

  getSelectedSpecialties(): any[] {
    const specialties: any[] = [];
    const specialtyInputs = document.querySelectorAll('input.specialty:checked');
    specialtyInputs.forEach((input: any) => {
      try {
        const specialty = JSON.parse(input.value);
        specialties.push(specialty);
      } catch (e) {
        console.error('Error parsing specialty:', e);
      }
    });
    return specialties;
  }

  uploadPhoto(vetId: string): void {
    const fileInput = document.querySelector('input[id="photoVet"]') as HTMLInputElement;
    if (!fileInput || !fileInput.files || !fileInput.files[0]) {
      alert('Vet created successfully!');
      this.router.navigate(['/vets']);
      return;
    }

    const file = fileInput.files[0];
    const reader = new FileReader();
    
    reader.onloadend = () => {
      const vetPhoto = (reader.result as string)
        .replace('data:', '')
        .replace(/^.+,/, '');
      
      this.previewImage = vetPhoto;
      
      const image = {
        name: file.name,
        type: "jpeg",
        photo: vetPhoto
      };

      this.vetApi.uploadVetPhoto(vetId, image.name, image).subscribe({
        next: () => {
          alert('Vet created successfully!');
          this.router.navigate(['/vets']);
        },
        error: (error) => {
          console.error('Error uploading photo:', error);
          alert('Vet created but photo upload failed');
          this.router.navigate(['/vets']);
        }
      });
    };
    
    reader.readAsDataURL(file);
  }

  updatePhoto(vetId: string): void {
    const fileInput = document.querySelector('input[id="photoVet"]') as HTMLInputElement;
    if (!fileInput || !fileInput.files || !fileInput.files[0]) {
      alert('Vet updated successfully!');
      this.router.navigate(['/vets', vetId]);
      return;
    }

    const file = fileInput.files[0];
    const reader = new FileReader();
    
    reader.onloadend = () => {
      const vetPhoto = (reader.result as string)
        .replace('data:', '')
        .replace(/^.+,/, '');
      
      this.previewImage = vetPhoto;
      
      const image = {
        name: file.name,
        type: "jpeg",
        photo: vetPhoto
      };

      this.vetApi.updateVetPhoto(vetId, image.name, image).subscribe({
        next: () => {
          alert('Vet updated successfully!');
          this.router.navigate(['/vets', vetId]);
        },
        error: (error) => {
          console.error('Error updating photo:', error);
          alert('Vet updated but photo upload failed');
          this.router.navigate(['/vets', vetId]);
        }
      });
    };
    
    reader.readAsDataURL(file);
  }

  cancel(): void {
    if (this.isEdit && this.vetId) {
      this.router.navigate(['/vets', this.vetId]);
    } else {
      this.router.navigate(['/vets']);
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImage = e.target.result.split(',')[1]; 
      };
      reader.readAsDataURL(file);
    }
  }
}

