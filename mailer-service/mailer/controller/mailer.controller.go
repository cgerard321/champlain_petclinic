package controller

import (
	"github.com/gin-gonic/gin"
	"net/http"
)

type MailerControllerImpl struct {
	
}

func (m MailerControllerImpl) New() {
}

func (m MailerControllerImpl) Routes(engine *gin.Engine) error {

	group := engine.Group("/mail")
	group.POST("", func(context *gin.Context) {
		context.IndentedJSON(http.StatusOK, "Hello there")
	})
	return nil
}
