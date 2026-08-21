import {
  Component,
  ElementRef,
  OnDestroy,
  computed,
  effect,
  inject,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { applyWhen, form, FormField, required } from '@angular/forms/signals';

import { DockerServices } from '@features/docker-logs/services/docker-services';
import { LogService } from '@features/docker-logs/services/docker-logs';
import { LogViewerFormModel } from '@features/docker-logs/models/log-viewer-form';
import { BadgeVariant, StatusBadge } from '@shared/components/status-badge/status-badge';

@Component({
  selector: 'app-log-viewer',
  imports: [
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIcon,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    FormField,
    StatusBadge,
  ],
  templateUrl: './log-viewer.html',
  styleUrl: './log-viewer.css',
})
export class LogViewer implements OnDestroy {
  private readonly dockerServices = inject(DockerServices);
  protected readonly logSocket = inject(LogService);

  private readonly logOutput = viewChild<ElementRef<HTMLDivElement>>('logOutput');
  protected readonly autoScroll = signal(true);

  protected readonly servicesResource = rxResource({
    stream: () => this.dockerServices.listMonitoredServices(),
  });

  protected readonly services = computed(() => this.servicesResource.value() ?? []);
  protected readonly isLoadingServices = computed(() => this.servicesResource.isLoading());
  protected readonly loadError = computed(() =>
    this.servicesResource.error() ? 'Failed to load monitored services.' : null,
  );

  protected readonly model = signal<LogViewerFormModel>({
    serviceName: null,
    specifyDb: false,
    dbName: null,
    numberOfLines: 200,
  });

  protected readonly logForm = form(this.model, (schemaPath) => {
    required(schemaPath.serviceName, { message: 'Select a service' });

    applyWhen(
      schemaPath.dbName,
      (ctx) => ctx.valueOf(schemaPath.specifyDb),
      (dbNamePath) => {
        required(dbNamePath, { message: 'Select a database' });
      },
    );
  });

  protected readonly selectedService = computed(
    () => this.services().find((s) => s.name === this.model().serviceName) ?? null,
  );

  protected readonly availableDbs = computed(() => this.selectedService()?.dbs ?? []);

  protected readonly displayedMessages = computed(() => {
    const limit = Math.max(1, this.model().numberOfLines ?? 200);
    return this.logSocket.messages().slice(-limit);
  });

  protected readonly statusBadgeVariant = computed<BadgeVariant>(() => {
    switch (this.logSocket.status().toLocaleLowerCase()) {
      case 'open':
        return 'success';
      case 'connecting':
        return 'warning';
      case 'error':
        return 'danger'
      default:
        return 'neutral';
    }
  });

  constructor() {
    effect(() => {
      this.displayedMessages();

      if (this.autoScroll()) {
        untracked(() => {
          queueMicrotask(() => {
            const container = this.logOutput()?.nativeElement;
            if (container) {
              container.scrollTop = container.scrollHeight;
            }
          });
        });
      }
    });
  }

  protected isErrorLine(line: { type_name: string; message: string }): boolean {
    if (line.type_name?.toLowerCase() === 'stderr') {
      return true;
    }

    const msg = line.message ?? '';

    return (
      /\b(ERROR|FATAL|SEVERE)\b/i.test(msg) ||
      /\b\w*Exception\b/.test(msg) ||
      /\bCaused by:/i.test(msg) ||
      /^\s*at\s+/.test(msg)
    );
  }

  protected isWarningLine(line: { type_name: string; message: string }): boolean {
    if (this.isErrorLine(line)) {
      return false;
    }

    const msg = line.message ?? '';
    return /\b(WARN|WARNING)\b/i.test(msg);
  }

  ngOnDestroy(): void {
    this.logSocket.stopLogStream();
  }

  protected onServiceChange(name: string): void {
    this.model.update((m) => ({ ...m, serviceName: name, specifyDb: false, dbName: null }));
    this.logSocket.stopLogStream();
  }

  protected onSpecifyDbChange(checked: boolean): void {
    this.model.update((m) => ({ ...m, specifyDb: checked, dbName: checked ? m.dbName : null }));
  }

  protected connect(): void {
    if (this.logForm().invalid()) {
      return;
    }

    const service = this.selectedService();
    if (!service) {
      return;
    }

    const { specifyDb, dbName, numberOfLines } = this.model();

    this.logSocket.getLogsStream({
      service: service.dockerService,
      numberOfLines,
      containerType: specifyDb ? 'db' : 'service',
      dbName: specifyDb ? (dbName ?? undefined) : undefined,
    });
  }

  protected disconnect(): void {
    this.logSocket.stopLogStream();
  }
}
