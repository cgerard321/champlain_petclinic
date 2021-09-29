package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"mailer-service/mailer"
	"net/http"
)

type MailerControllerImpl struct {
	
}

func (m MailerControllerImpl) New() {
}

func (m MailerControllerImpl) Routes(engine *gin.Engine) error {

	group := engine.Group("/mail").Use(UnMarshallMail)
	group.POST("", func(context *gin.Context) {

		get, exists := context.Get("mail")
		if !exists {
			fmt.Println("E-mail not in context")
			context.JSON(http.StatusBadRequest, "Unable to parse e-mail from body")
			return
		}

		mail := get.(*mailer.Mail)

		context.IndentedJSON(http.StatusOK, fmt.Sprintf("Message sent to %s", mail.To))
	})
	return nil
}
