package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"mailer-service/mailer"
	"net/http"
)

func UnMarshallMail(c *gin.Context) {

	var mail mailer.Mail

	if err := c.ShouldBindJSON(&mail); err != nil {
		fmt.Println(err)
		c.JSON(http.StatusBadRequest, err)
		return
	}

	c.Set("mail", &mail)
	c.Next()
}

func ValidateEmail(c *gin.Context) {
	
}
