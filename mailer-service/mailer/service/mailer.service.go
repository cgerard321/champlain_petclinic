package service

import (
	"mailer-service/mailer"
	gomail "gopkg.in/mail.v2"
)

type MailerServiceImpl struct {
	dialer gomail.Dialer
}

func (m MailerServiceImpl) New() {
	panic("implement me")
}

func (m MailerServiceImpl) SendMail(mail *mailer.Mail) error {
	panic("implement me")
}
