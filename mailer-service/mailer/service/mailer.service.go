package service

import (
	"mailer-service/mailer"
)

type MailerServiceImpl struct {

}

func (m MailerServiceImpl) New() {
	panic("implement me")
}

func (m MailerServiceImpl) SendMail(mail *mailer.Mail) error {
	panic("implement me")
}
