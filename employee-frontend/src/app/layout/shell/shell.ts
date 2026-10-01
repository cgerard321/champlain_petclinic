import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';

import { Header } from '@layout/header/header';

@Component({
  selector: 'app-shell',
  imports: [MatSidenavModule, Header],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {}
