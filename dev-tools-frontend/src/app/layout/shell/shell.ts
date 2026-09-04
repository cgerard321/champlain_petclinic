import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';

import { Header } from '../header/header';
import { Sidenav } from '../sidenav/sidenav';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, MatSidenavModule, Header, Sidenav],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {}
