package mailer

import "github.com/gin-gonic/gin"

type MailerController interface {
	New()
	Routes(engine *gin.Engine) error
}

type Mail struct {
	To string `json:"to" validate:"required,email"`
	Message string `json:"message" validate:"required"`
	Subject string `json:"subject"`
}
