package mailer

import "testing"

func TestNewDialer_DefaultsPortAndTLS(t *testing.T) {
	d := NewDialer(Config{
		Host:               "smtp.test",
		Username:           "user",
		Password:           "pass",
		InsecureSkipVerify: true,
	})

	if d == nil {
		t.Fatal("dialer nil")
	}

	if d.Port != 587 {
		t.Fatalf("expected default port 587, got %d", d.Port)
	}
	if d.TLSConfig == nil || !d.TLSConfig.InsecureSkipVerify {
		t.Fatalf("expected TLSConfig.InsecureSkipVerify=true")
	}
}

func TestNewDialer_CustomPortAndTLS(t *testing.T) {
	d := NewDialer(Config{
		Host:               "smtp.test",
		Port:               2525,
		InsecureSkipVerify: true,
	})
	if d.Port != 2525 {
		t.Fatalf("expected custom port 2525, got %d", d.Port)
	}
	if d.TLSConfig == nil || !d.TLSConfig.InsecureSkipVerify {
		t.Fatalf("expected InsecureSkipVerify=true")
	}
}

func TestNewDialer_TLSVerifyEnabledByDefault(t *testing.T) {
	d := NewDialer(Config{Host: "smtp.local"})
	if d.TLSConfig == nil {
		t.Fatalf("expected TLSConfig to be non-nil")
	}
	if d.TLSConfig.InsecureSkipVerify {
		t.Fatalf("expected InsecureSkipVerify to be false by default")
	}
}
