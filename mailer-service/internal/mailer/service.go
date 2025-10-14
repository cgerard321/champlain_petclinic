package mailer

import (
	gomail "gopkg.in/mail.v2"

	pkg "mailer-service/pkg/mailer"
)

type Service interface {
	Send(m *pkg.Mail) error
}

type service struct {
	dialer *gomail.Dialer
	from   string
}

func NewService(dialer *gomail.Dialer, from string) Service {
	return &service{dialer: dialer, from: from}
}

func (s *service) Send(m *pkg.Mail) error {
	msg := gomail.NewMessage()

	if m.SenderName != "" {
		msg.SetAddressHeader("From", s.from, m.SenderName)
	} else {
		msg.SetHeader("From", s.from)
	}

	msg.SetHeader("To", m.To)
	msg.SetHeader("Subject", m.Subject)

	msg.SetBody("text/html", m.Body)

	return s.dialer.DialAndSend(msg)
}

var _ Service = (*service)(nil)
