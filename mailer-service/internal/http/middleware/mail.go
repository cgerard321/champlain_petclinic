package middleware

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"

	pkg "mailer-service/pkg/mailer"
)

// We should definitely make this an ENV later
const ctxKeyMail = "mail"

func UnmarshalMail() gin.HandlerFunc {
	validate := validator.New()

	return func(c *gin.Context) {
		var m pkg.Mail

		if err := c.ShouldBindJSON(&m); err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			c.Abort()
			return
		}

		if err := validate.Struct(&m); err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			c.Abort()
			return
		}

		c.Set(ctxKeyMail, &m)
		c.Next()
	}
}
