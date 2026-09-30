import { Component } from '@angular/core';

// VETS-CPC-1927: the layout/ folder had no footer at all, unlike header, sidenav and shell.
// Created here as a minimal component, purely to carry the translated strings required by
// acceptance criterion #4 of the i18n ticket.
@Component({
  selector: 'app-footer',
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer {}
