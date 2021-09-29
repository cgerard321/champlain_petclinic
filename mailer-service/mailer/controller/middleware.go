package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"mailer-service/mailer"
	"net/http"
)

func UnMarshallMail(c *gin.Context) {

	var mail mailer.Mail

	if err := c.ShouldBindJSON(&mail); err != nil {
		fmt.Println(err)
		c.JSON(http.StatusBadRequest, err)
		c.Abort()
		return
	}

	c.Set("mail", &mail)
	c.Next()
}

func ValidateEmail(c *gin.Context) {

	validate := validator.New()

	get, exists := c.Get("mail")
	if !exists || get == nil {
		fmt.Println("E-mail not in context")
		c.JSON(http.StatusBadRequest, "e-mail not found")
		c.Abort()
		return
	}

	mail := get.(*mailer.Mail)


	if err := validate.Struct(mail); err != nil {
		fmt.Println(err)
		c.JSON(http.StatusBadRequest, err.Error())
		c.Abort()
		return
	}

	c.Next()
}
