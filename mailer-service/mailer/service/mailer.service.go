package service

import (
	gomail "gopkg.in/mail.v2"
	"mailer-service/mailer"
)

type MailerServiceImpl struct {
	dialer *gomail.Dialer
}

func (m *MailerServiceImpl) New(dialer *gomail.Dialer) {
	m.dialer = dialer
}

func (m MailerServiceImpl) SendMail(mail *mailer.Mail) error {
	panic("implement me")
}

