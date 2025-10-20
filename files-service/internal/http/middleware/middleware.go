package middleware

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	exception2 "files-service/internal/util/exception"
	"io"
	"log"
	"mime"
	"net/http"
	"path"
	"strings"

	"github.com/gin-gonic/gin"
)

func GlobalExceptionHandler(c *gin.Context) {
	c.Next()
	if len(c.Errors) > 0 {
		err := c.Errors[0].Err

		var code int
		var message string

		switch err.(type) {
		case *exception2.NotFoundException:
			code = http.StatusNotFound
			message = err.Error()

		case *exception2.InvalidFileIdException:
			code = http.StatusBadRequest
			message = err.Error()

		case *exception2.InvalidRequestModelException:
			code = http.StatusUnprocessableEntity

		default:
			code = http.StatusInternalServerError
			message = "an unexpected error has occurred: " + err.Error()
		}

		log.Printf("code: %d, message: %s", code, message)
		c.AbortWithStatusJSON(code, message)
	}
}

func ValidateRequestBody(c *gin.Context) {
	raw, err := c.GetRawData()
	if err != nil {
		Cancel(c, exception2.NewInvalidRequestModelException("unable to read request body"))
		return
	}
	c.Request.Body = io.NopCloser(bytes.NewBuffer(raw))

	var body map[string]interface{}
	if err := json.Unmarshal(raw, &body); err != nil {
		Cancel(c, exception2.NewInvalidRequestModelException("invalid JSON"))
		return
	}

	// --- Validate contentType ---
	contentType := requiredField(body, "fileType", c)
	if c.IsAborted() {
		return
	}

	mediaType, _, err := mime.ParseMediaType(contentType)
	if err != nil {
		Cancel(c, exception2.NewInvalidRequestModelValueException("fileType", contentType))
		return
	}

	// --- Validate fileName ---
	fileName := requiredField(body, "fileName", c)
	if c.IsAborted() {
		return
	}
	if strings.TrimSpace(fileName) == "" {
		Cancel(c, exception2.NewInvalidRequestModelValueException("fileName", fileName))
		return
	}
	if len([]rune(fileName)) > 250 {
		Cancel(c, exception2.NewInvalidRequestModelValueException("fileName", fileName))
		return
	}
	if ext := path.Ext(fileName); ext != "" && mime.TypeByExtension(ext) != mediaType {
		Cancel(c, exception2.NewInvalidRequestModelValueException("fileName", fileName))
		return
	}

	// --- Validate fileData ---
	fileData := requiredField(body, "fileData", c)
	if c.IsAborted() {
		return
	}
	if _, err := base64.StdEncoding.DecodeString(fileData); err != nil {
		Cancel(c, exception2.NewInvalidRequestModelValueException("fileData", fileData))
		return
	}
}

func Cancel(c *gin.Context, err error) {
	c.Error(err)
	c.Abort()
}

func requiredField(body map[string]interface{}, field string, c *gin.Context) string {
	value, valid := body[field].(string)
	if !valid {
		Cancel(c, exception2.NewInvalidRequestModelFieldException(field))
		return ""
	}
	return value
}
