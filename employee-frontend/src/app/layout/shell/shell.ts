import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';

import { Header } from '@layout/header/header';
import { Sidenav } from '@layout/sidenav/sidenav';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, MatSidenavModule, Header, Sidenav],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {}
