import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-veterinarians',
  imports: [MatIconModule, RouterLink],
  templateUrl: './veterinarians.html',
  styleUrl: './veterinarians.css',
})
export class Veterinarians {}
