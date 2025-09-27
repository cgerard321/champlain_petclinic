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
	EmailSendTo       string `json:"EmailSendTo" validate:"required,email"`
	EmailTitle        string `json:"EmailTitle" validate:"required"`
	TemplateName      string `json:"TemplateName"`
	Header            string `json:"Header"`
	Body              string `json:"Body"`
	Footer            string `json:"Footer"`
	CorrespondantName string `json:"CorrespondantName"`
	SenderName        string `json:"SenderName"`
}

func CreateDialer(host, email, password string, port ...int) *gomail.Dialer {

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
