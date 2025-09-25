package mailer

import (
	"crypto/tls"
	"github.com/gin-gonic/gin"
	gomail "gopkg.in/mail.v2"
)

type MailerController interface {
	New(service MailerService)
	Routes(engine *gin.Engine) error
}

type MailerService interface {
	New(dialer *gomail.Dialer)
	SendMail(mail *Mail) error
}

// New mailing struct
type Mail struct {
    EmailSendTo string `json:"email_send_to" validate:"required,email"`
    EmailTitle string `json:"email_title" validate:"required"`
    TemplateName string `json:"template_name"`
    Header string `json:"header"`
    Body string `json:"body"`
    Footer string `json:"footer"`
    CorrespondantName string `json:"correspondant_name"`
    SenderName string `json:"sender_name"`
}

func CreateDialer(host, email, password string, port... int) *gomail.Dialer {

	truePort := 587

	// Ghetto optional param
	if len(port) != 0 {
		truePort = port[0]
	}

	dialer := gomail.NewDialer(host, truePort, email, password)

	// Set to false for prod
	dialer.TLSConfig = &tls.Config{InsecureSkipVerify: true}

	return dialer
}
