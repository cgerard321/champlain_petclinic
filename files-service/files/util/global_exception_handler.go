package util

import (
	"files-service/files/util/exception"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
)

func HandleExceptions(ctx *gin.Context, err error) {
	var code int
	var message string

	switch err.(type) {
	case *exception.NotFoundException:
		code = http.StatusNotFound
		message = err.Error()

	case *exception.InvalidFileIdException:
		code = http.StatusBadRequest
		message = err.Error()

	default:
		code = http.StatusInternalServerError
		message = "an unexpected error has occurred: " + err.Error()
	}

	log.Printf("code: %d, message: %s", code, message)
	ctx.AbortWithStatusJSON(code, message)
}
