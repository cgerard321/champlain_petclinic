package service

import (
	"mailer-service/mailer"
	"os"
	"testing"
)

var ValidMail = &mailer.Mail{To: "example@test.com", Subject: "Test subject", Message: "Test message"}


func TestMain(m *testing.M) {

	os.Exit(m.Run())
}

func TestMailerService_New(t *testing.T) {
	mS := MailerServiceImpl{}
	dialer := mailer.CreateDialer("smtp.invalid.com", "a@b.c", "pass")
	mS.New(dialer)
}

func TestMailerServiceImpl_SendMailValidMail(t *testing.T) {
	mS := MailerServiceImpl{}
	dialer := mailer.CreateDialer("smtp.invalid.com", "a@b.c", "pass")
	mS.New(dialer)

	mS.SendMail(ValidMail)
}