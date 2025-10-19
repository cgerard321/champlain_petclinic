import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VetApiService } from '../../../vets/api/vet-api.service';

@Component({
  selector: 'app-vet-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <link href="/css/vets/form.css" rel="stylesheet" type="text/css"/>

    <div class="bg-light">
      <div class="container">
        <div class="py-5 text-center"></div>
        <h2 class="titleVetForm" id="title">New Vet Sign Up</h2>
        <p class="lead">Welcome to our team, please fill out the form below ensuring to fill all sections.</p>
      </div>
      <div class="container">
        <div class="py-5 text-center"></div>
        <div class="row g-5">
          <div class="col-md-7 col-lg-12">
            <form enctype="multipart/form-data" id="vetForm" name="vetForm" #vetForm="ngForm" (ngSubmit)="submitVetForm()">
              <div class="row g-3">
                <div class="col-sm-6">
                  <h5 class="form-label">Firstname</h5>
                  <input 
                    class="form-control" 
                    id="firstName"
                    name="firstName" 
                    [(ngModel)]="vet.firstName" 
                    [ngModelOptions]="{standalone: true}"
                    placeholder="First Name" 
                    required
                    title="Minimum of 3 characters and maximum of 30 characters. Only letters and punctuation mark."
                    type="text">
                </div>

                <div class="col-sm-6">
                  <h5 class="form-label">Lastname</h5>
                  <input 
                    class="form-control" 
                    id="lastName" 
                    name="lastName"
                    [(ngModel)]="vet.lastName" 
                    [ngModelOptions]="{standalone: true}"
                    pattern="^[a-zA-Z -]+" 
                    placeholder="Last Name" 
                    required
                    title="Minimum of 2 characters and maximum of 30 characters. Only letters and punctuation mark."
                    type="text">
                </div>

                <div class="col-sm-12">
                  <h5 class="form-label">Phone Number</h5>
                  <input 
                    class="form-control" 
                    id="phoneNumber"
                    name="phoneNumber" 
                    [(ngModel)]="phoneNumberInput" 
                    [ngModelOptions]="{standalone: true}"
                    placeholder="Phone Number" 
                    required
                    title="Accepts only numbers for 4 characters." 
                    type="number"
                    maxlength="4">
                </div>

                <hr class="my-4"/>

                <div class="parent">
                  <img alt="Profile picture preview" [src]="vetPhoto?.photo ? 'data:image/*;base64, ' + vetPhoto.photo : ''" width="150" height="150" class="image1">
                  <img alt="" [src]="previewImage ? 'data:image/jpeg;base64, ' + previewImage : ''" *ngIf="previewImage" width="150" height="150" class="image2"/>
                  <input type="file" accept="image/jpeg" class="fileInput" id="photoVet" (change)="onFileSelected($event)"/>
                </div>
                  
                <hr class="my-4"/>

                <div class="col-sm-12">
                  <h5 class="form-label">Career Resume</h5>
                  <textarea 
                    class="form-control" 
                    id="vetResume" 
                    name="resume"
                    [(ngModel)]="vet.resume" 
                    [ngModelOptions]="{standalone: true}"
                    pattern="^(.*)" 
                    placeholder="Resume"
                    rows="4">
                  </textarea>
                </div>

                <hr class="my-4"/>

                <div class="row gy-3">
                  <div class="col-md-12">
                    <h5 class="mb-3">Specialty</h5>
                    <div class="form-check">
                      <input 
                        class="form-check-input specialty" 
                        id="radiology" 
                        name="specialties"
                        type="checkbox"
                        value='{"id":1, "specialtyId":100001, "name":"radiology"}'
                        (change)="onSpecialtyChange($event)">
                      <label class="form-check-label vet-label-input" for="radiology">Radiology</label>
                    </div>

                    <div class="form-check">
                      <input 
                        class="form-check-input specialty" 
                        id="surgery"
                        type="checkbox"
                        value='{"id":2, "specialtyId":100002, "name":"surgery"}'
                        (change)="onSpecialtyChange($event)">
                      <label class="form-check-label vet-label-input" for="surgery">Surgery</label>
                    </div>

                    <div class="form-check">
                      <input 
                        class="form-check-input specialty" 
                        id="dentistry"
                        type="checkbox"
                        value='{"id":3, "specialtyId":100003, "name":"dentistry"}'
                        (change)="onSpecialtyChange($event)">
                      <label class="form-check-label vet-label-input" for="dentistry">Dentistry</label>
                    </div>

                    <div class="form-check">
                      <input 
                        class="form-check-input specialty" 
                        id="general"
                        type="checkbox"
                        value='{"id":4, "specialtyId":100004, "name":"general"}'
                        (change)="onSpecialtyChange($event)">
                      <label class="form-check-label vet-label-input" for="general">General</label>
                    </div>
                  </div>
                </div>

                <div class="col-sm-6">
                  <div class="my-3 check-align">
                    <h5 class="mb-3">Available Work Days</h5>
                    <div class="form-check">
                      <input class="form-check-input workday" id="Monday" type="checkbox" value="Monday" (change)="onWorkdayChange($event)">
                      <label class="form-check-label vet-label-input" for="Monday">Monday</label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input workday" id="Tuesday" type="checkbox" value="Tuesday" (change)="onWorkdayChange($event)">
                      <label class="form-check-label vet-label-input" for="Tuesday">Tuesday</label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input workday" id="Wednesday" type="checkbox" value="Wednesday" (change)="onWorkdayChange($event)">
                      <label class="form-check-label vet-label-input" for="Wednesday">Wednesday</label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input workday" id="Thursday" type="checkbox" value="Thursday" (change)="onWorkdayChange($event)">
                      <label class="form-check-label vet-label-input" for="Thursday">Thursday</label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input workday" id="Friday" type="checkbox" value="Friday" (change)="onWorkdayChange($event)">
                      <label class="form-check-label vet-label-input" for="Friday">Friday</label>
                    </div>
                  </div>
                </div>

                <div class="my-3 check-align">
                  <h5 class="mb-3">Active Vet</h5>
                  <div class="form-check">
                    <label class="form-check-label vet-label-input">
                      <input 
                        class="form-check-input isActiveRadio" 
                        name="isActive" 
                        [(ngModel)]="vet.active"
                        [ngModelOptions]="{standalone: true}"
                        type="radio" 
                        [value]="true">Yes
                    </label>
                  </div>
                  <div class="form-check">
                    <label class="form-check-label vet-label-input">
                      <input 
                        class="form-check-input isActiveRadio" 
                        name="isActive" 
                        [(ngModel)]="vet.active"
                        [ngModelOptions]="{standalone: true}"
                        type="radio" 
                        [value]="false">No
                    </label>
                  </div>
                </div>

                <div id="user-info">
                  <div class="form-group">
                    <label for="username">Username</label>
                    <input 
                      id="username" 
                      class="form-control" 
                      [(ngModel)]="vet.username" 
                      name="username" 
                      [ngModelOptions]="{standalone: true}"
                      required />
                    <span *ngIf="!vet.username" class="help-block">Username is required.</span>
                  </div>

                  <div id="email-info" class="col-12">
                    <h5 class="form-label">Email Address</h5>
                    <input 
                      class="form-control" 
                      id="email" 
                      name="email"
                      [(ngModel)]="vet.email" 
                      [ngModelOptions]="{standalone: true}"
                      pattern="\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,3}\\b" 
                      placeholder="Email"
                      required
                      title="Minimum of 6 characters and maximum of 320 characters. Top level domain should have 2 to 3 characters."
                      type="email">
                  </div>

                  <div class="form-group">
                    <label for="password">Password</label>
                    <div class="input-group">
                      <input 
                        id="password" 
                        class="form-control" 
                        [type]="showPassword ? 'text' : 'password'" 
                        [(ngModel)]="vet.password" 
                        name="password" 
                        [ngModelOptions]="{standalone: true}"
                        (ngModelChange)="updatePasswordStrength()"
                        required />
                      <span class="input-group-addon" (click)="togglePasswordVisibility()" style="cursor: pointer;">
                        <i [class]="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
                      </span>
                    </div>
                    <span *ngIf="!vet.password" class="help-block">Password is required.</span>
                    <div [class]="'password-strength strength-' + passwordStrength">
                      {{ strengthText }}
                    </div>
                  </div>

                  <div style="padding-left: 47%" *ngIf="isLoading">
                    <div class="loader m-2"></div>
                  </div>
                </div>

                <hr class="my-4"/>
                <button class="w-100 btn btn-primary btn-lg" type="submit" [disabled]="isLoading">
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
    .loader {
      border: 8px solid #f3f3f3;
      border-top: 8px solid #005d9a;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .help-block {
      color: red;
    }

    .password-strength {
      font-weight: bold;
      margin-top: 5px;
    }

    .strength-1 {
      color: red;
    }

    .strength-2 {
      color: orange;
    }

    .strength-3 {
      color: green;
    }
  `]
})
export class VetFormComponent {
  private vetApi = inject(VetApiService);
  private router = inject(Router);

  vet = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    phoneNumber: '',
    resume: '',
    specialties: [],
    workday: [],
    active: true,
    photoDefault: true
  };

  phoneNumberInput = '';
  vetPhoto: any = null;
  previewImage: string | null = null;
  selectedSpecialties: any[] = [];
  selectedWorkdays: string[] = [];
  isLoading = false;
  showPassword = false;
  passwordStrength = 0;
  strengthText = '';

  ngOnInit(): void {
    this.isLoading = false;
  }

  submitVetForm(): void {
    this.isLoading = true;

    if (this.selectedSpecialties.length === 0) {
      alert("vet should have at least one specialty");
      this.isLoading = false;
      return;
    }

    if (this.selectedWorkdays.length === 0) {
      alert("vet should have at least one workday");
      this.isLoading = false;
      return;
    }

    const namePattern = /^[a-zA-Z -]+/;
    if (!namePattern.test(this.vet.firstName)) {
      alert("first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " + this.vet.firstName);
      this.isLoading = false;
      return;
    }
    if (this.vet.firstName.length > 30 || this.vet.firstName.length < 2) {
      alert("first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " + this.vet.firstName);
      this.isLoading = false;
      return;
    }

    if (!namePattern.test(this.vet.lastName)) {
      alert("last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " + this.vet.lastName);
      this.isLoading = false;
      return;
    }
    if (this.vet.lastName.length > 30 || this.vet.lastName.length < 2) {
      alert("last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: " + this.vet.lastName);
      this.isLoading = false;
      return;
    }

    if (this.phoneNumberInput.length !== 4) {
      alert("phoneNumber length not equal to 4: " + this.phoneNumberInput);
      this.isLoading = false;
      return;
    }

    if (this.vet.resume.length < 10) {
      alert("resume should be minimum 10 characters: " + this.vet.resume);
      this.isLoading = false;
      return;
    }

    const vetData = {
      ...this.vet,
      phoneNumber: "(514)-634-8276 #" + this.phoneNumberInput,
      specialties: this.selectedSpecialties,
      workday: this.selectedWorkdays
    };

    const payload = {
      username: this.vet.username,
      password: this.vet.password,
      email: this.vet.email,
      vet: vetData
    };

    this.vetApi.createVetUser(payload).subscribe({
      next: (response) => {
        this.isLoading = false;
        
        if (this.previewImage) {
          this.uploadPhoto(response.vetId);
        }
        
        alert('Veterinarian created successfully!');
        this.router.navigate(['/admin-panel']);
      },
      error: (_error) => {
        this.isLoading = false;
        alert('Invalid vet profile picture');
      }
    });
  }

  onSpecialtyChange(event: any): void {
    const checkbox = event.target;
    const specialty = JSON.parse(checkbox.value);
    
    if (checkbox.checked) {
      this.selectedSpecialties.push(specialty);
    } else {
      this.selectedSpecialties = this.selectedSpecialties.filter(s => s.id !== specialty.id);
    }
  }

  onWorkdayChange(event: any): void {
    const checkbox = event.target;
    const workday = checkbox.value;
    
    if (checkbox.checked) {
      this.selectedWorkdays.push(workday);
    } else {
      this.selectedWorkdays = this.selectedWorkdays.filter(w => w !== workday);
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        this.previewImage = reader.result?.toString().replace('data:', '').replace(/^.+,/, '') || null;
        this.vet.photoDefault = false;
      };
      reader.readAsDataURL(file);
    }
  }

  uploadPhoto(vetId: string): void {
    if (!this.previewImage) return;

    const fileInput = document.getElementById('photoVet') as HTMLInputElement;
    const file = fileInput?.files?.[0];
    
    if (file) {
      const imageData = {
        name: file.name,
        type: "jpeg",
        photo: this.previewImage
      };

      this.vetApi.uploadVetPhoto(vetId, file.name, imageData).subscribe({
        next: (_response) => {
        },
        error: (_error) => {
        }
      });
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  updatePasswordStrength(): void {
    const password = this.vet.password;
    this.passwordStrength = this.calculatePasswordStrength(password);
    this.strengthText = this.getStrengthText(this.passwordStrength);
  }

  calculatePasswordStrength(password: string): number {
    const pattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/;

    if (pattern.test(password)) {
      return 3;
    } else if (password.length >= 8) {
      return 2;
    } else if (password.length > 0) {
      return 1;
    }
    return 0;
  }

  getStrengthText(strength: number): string {
    switch (strength) {
      case 1:
        return "Weak";
      case 2:
        return "Medium";
      case 3:
        return "Strong";
      default:
        return "";
    }
  }
}