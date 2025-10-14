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

	if d.TLSConfig == nil || !d.TLSConfig.InsecureSkipVerify {
		t.Fatalf("expected TLSConfig.InsecureSkipVerify=true")
	}
}
