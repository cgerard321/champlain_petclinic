import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';

import { Footer } from '@layout/footer/footer';
import { Header } from '@layout/header/header';

@Component({
  selector: 'app-shell',

  imports: [RouterOutlet, MatSidenavModule, Header, Footer],

  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {}
