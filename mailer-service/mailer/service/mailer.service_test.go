package service

import (
	"mailer-service/mailer"
	"os"
	"testing"
)

func TestMain(m *testing.M) {

	os.Exit(m.Run())
}

func TestMailerService_New(t *testing.T) {
	mS := MailerServiceImpl{}
	dialer := mailer.CreateDialer("smtp.invalid.com", "a@b.c", "pass")
	mS.New(dialer)
}