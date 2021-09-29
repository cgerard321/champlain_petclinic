package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
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

		validate := validator.New()

		if err := validate.Struct(mail); err != nil {
			fmt.Println(err)
			context.JSON(http.StatusBadRequest, err.Error())
			return
		}

		context.IndentedJSON(http.StatusOK, fmt.Sprintf("Message sent to %s", mail.To))
	})
	return nil
}
