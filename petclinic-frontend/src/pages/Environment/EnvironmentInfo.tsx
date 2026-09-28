import { useCallback, useEffect, useState } from 'react';
import {
  Badge,
  Button,
  Card,
  Col,
  Container,
  Row,
  Table,
} from 'react-bootstrap';
import { NavBar } from '@/layouts/AppNavBar';
import { AppFooter } from '@/layouts/AppFooter';
import { Reveal } from '@/shared/components';
import {
  ENVIRONMENT_ENDPOINT_MISSING,
  EnvironmentInfoModel,
  getEnvironmentInfo,
} from '@/shared/api/environment';

import './EnvironmentInfo.css';

type LoadState = 'loading' | 'found' | 'missing';

interface DetailRow {
  label: string;
  value: string;
}

const badgeVariantFor = (
  environment: string
): 'success' | 'warning' | 'danger' | 'secondary' => {
  const normalized = environment.toLowerCase();

  if (normalized.includes('prod')) return 'danger';
  if (normalized.includes('stag')) return 'warning';
  if (normalized.includes('local') || normalized.includes('docker'))
    return 'success';

  return 'secondary';
};

function toRows(info: EnvironmentInfoModel): DetailRow[] {
  const rows: DetailRow[] = [
    { label: 'Application', value: info.application },
    { label: 'Environment', value: info.environment },
    { label: 'Active profiles', value: info.activeProfiles || 'none' },
    { label: 'Build version', value: info.buildVersion },
    { label: 'Build label', value: info.buildLabel },
    { label: 'Build time', value: info.buildTime },
    { label: 'Git commit', value: info.gitCommit },
    { label: 'Git branch', value: info.gitBranch },
    { label: 'Started at', value: info.startedAt },
    { label: 'Uptime', value: info.uptime },
    { label: 'Port', value: String(info.port) },
    { label: 'Host name', value: info.hostName },
    { label: 'Java version', value: info.javaVersion },
    {
      label: 'Allowed frontend origins',
      value: info.allowedFrontendOrigins || 'none',
    },
    { label: 'Server time', value: info.serverTime },
  ];

  Object.entries(info.markers ?? {}).forEach(([key, value]) => {
    rows.push({ label: `marker.${key}`, value });
  });

  return rows;
}

export default function EnvironmentInfo(): JSX.Element {
  const [state, setState] = useState<LoadState>('loading');
  const [info, setInfo] = useState<EnvironmentInfoModel | null>(null);

  const load = useCallback(async (): Promise<void> => {
    setState('loading');

    const result = await getEnvironmentInfo();

    if (result) {
      setInfo(result);
      setState('found');
    } else {
      setInfo(null);
      setState('missing');
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className="envinfo-root">
      <NavBar />

      <header className="envinfo-intro">
        <div className="envinfo-intro-inner">
          <h1 className="envinfo-title">Environment Checker</h1>
          <p className="envinfo-sub">
            Shows which api-gateway this page is talking to. Open this page on
            local and staging to see the endpoint, then on the deployed
            environment to confirm it is not there yet.
          </p>
        </div>
      </header>

      <Container className="py-4">
        <Row className="g-3 justify-content-center">
          <Col lg={9}>
            <Reveal>
              <Card className="shadow-soft envinfo-card">
                <Card.Body>
                  <div className="envinfo-summary">
                    <div className="envinfo-summary-item">
                      <span className="envinfo-summary-label">Backend</span>
                      <Badge
                        bg={
                          state === 'found'
                            ? badgeVariantFor(info?.environment ?? '')
                            : 'secondary'
                        }
                      >
                        {state === 'loading' && 'checking...'}
                        {state === 'found' && info?.environment}
                        {state === 'missing' && ENVIRONMENT_ENDPOINT_MISSING}
                      </Badge>
                    </div>

                    <Button
                      variant="outline-secondary"
                      size="sm"
                      onClick={() => void load()}
                      disabled={state === 'loading'}
                    >
                      {state === 'loading' ? 'Refreshing...' : 'Refresh'}
                    </Button>
                  </div>

                  {state === 'missing' && (
                    <div
                      className="envinfo-alert envinfo-alert-warn"
                      role="alert"
                    >
                      <strong>
                        The environment endpoint does not exist on the deployed
                        backend.
                      </strong>
                      <p className="mb-0">
                        The request to <code>/api/v2/gateway/environment</code>{' '}
                        did not return environment data, which means the backend
                        currently deployed does not contain this change yet.
                        Compare this against local and staging, where the
                        endpoint is present.
                      </p>
                    </div>
                  )}

                  <Table bordered size="sm" className="envinfo-table">
                    <thead>
                      <tr>
                        <th style={{ width: '38%' }}>Property</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      {state === 'found' && info ? (
                        toRows(info).map(row => (
                          <tr key={row.label}>
                            <td>{row.label}</td>
                            <td>{row.value}</td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={2} className="text-center text-muted">
                            {state === 'loading'
                              ? 'Contacting the api-gateway...'
                              : 'No environment data returned by this backend.'}
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            </Reveal>
          </Col>
        </Row>
      </Container>

      <AppFooter />
    </div>
  );
}
