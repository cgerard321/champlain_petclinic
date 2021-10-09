package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"mailer-service/mailer"
	"net/http"
)

type MailerControllerImpl struct {
	s mailer.MailerService
}

func (m *MailerControllerImpl) New(service mailer.MailerService) {
	m.s = service
}

// SendMail ... Send Mail
// @Summary Sends e-mail
// @Description accepts an e-mail, in the request body, to send
// @Tags Mail
// @Accept json
// @Param mail body mailer.Mail true "Mail"
// @Success 200 {array} mailer.Mail
// @Failure 400 {object} object
// @Failure 500 {object} object
// @Router / [post]
func (m MailerControllerImpl) handleMailPOST(context *gin.Context) {

	get, exists := context.Get("mail")
	if !exists || get == nil {
		fmt.Println("E-mail not in context")
		context.JSON(http.StatusBadRequest, "Unable to parse e-mail from body")
		context.Abort()
		return
	}

	mail := get.(*mailer.Mail)

	if err := m.s.SendMail(mail); err != nil {
		fmt.Println(err.Error())
		context.JSON(http.StatusInternalServerError, err.Error())
		context.Abort()
		return
	}

	context.IndentedJSON(http.StatusOK, fmt.Sprintf("Message sent to %s", mail.To))
}

func (m MailerControllerImpl) Routes(engine *gin.Engine) error {

	group := engine.Group("/mail").Use(UnMarshallMail, ValidateEmail)

	group.POST("", m.handleMailPOST)

	return nil
}
