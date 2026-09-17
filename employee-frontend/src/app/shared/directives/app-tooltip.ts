import { Directive, effect, inject, input } from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';

@Directive({
  selector: '[appTooltip]',
  hostDirectives: [MatTooltip],
  host: {
    class: 'truncate-tooltip',
    style: 'max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;',
  },
})
export class AppTooltip {
  readonly appTooltip = input.required<string>();

  private readonly tooltip = inject(MatTooltip);

  constructor() {
    this.tooltip.showDelay = 300;
    effect(() => {
      this.tooltip.message = this.appTooltip();
    });
  }
}
