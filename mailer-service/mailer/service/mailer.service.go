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

	trueMail := gomail.NewMessage()

	trueMail.SetHeader("From", m.dialer.Username)
	trueMail.SetHeader("To", mail.To)
	trueMail.SetHeader("Subject", mail.Subject)
	trueMail.SetBody("text/html", mail.Message)

	return m.dialer.DialAndSend(trueMail)
}

