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

// new mailer SendMail function
func (m MailerServiceImpl) SendMail(mail *mailer.Mail) error {

	trueMail := gomail.NewMessage()

	if mail.SenderName != ""{
	trueMail.SetHeader("From", mail.SenderName)
	}

	trueMail.SetHeader("To", mail.EmailSendTo)
	trueMail.SetHeader("Subject", mail.EmailTitle)
	trueMail.SetBody("text/html", mail.Body)

	return m.dialer.DialAndSend(trueMail)
}
