package mailer

import "github.com/gin-gonic/gin"

type MailerController interface {
	New()
	Routes(engine *gin.Engine) error
}