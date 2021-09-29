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

	group := engine.Group("/mail")
	group.POST("", func(context *gin.Context) {
		var mail mailer.Mail

		if err := context.ShouldBindJSON(&mail); err != nil {
			fmt.Println(err)
			context.JSON(http.StatusBadRequest, err)
			return
		}
		context.IndentedJSON(http.StatusOK, fmt.Sprintf("Message sent to %s", mail.To))
	})
	return nil
}
