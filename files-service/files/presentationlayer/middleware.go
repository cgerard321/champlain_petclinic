package presentationlayer

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"files-service/files/util/exception"
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
		case *exception.NotFoundException:
			code = http.StatusNotFound
			message = err.Error()

		case *exception.InvalidFileIdException:
			code = http.StatusBadRequest
			message = err.Error()

		case *exception.InvalidRequestModelException:
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
		cancel(c, exception.NewInvalidRequestModelException("unable to read request body"))
		return
	}
	c.Request.Body = io.NopCloser(bytes.NewBuffer(raw))

	var body map[string]interface{}
	if err := json.Unmarshal(raw, &body); err != nil {
		cancel(c, exception.NewInvalidRequestModelException("invalid JSON"))
		return
	}

	// --- Validate contentType ---
	contentType := requiredField(body, "contentType", c)
	if c.IsAborted() {
		return
	}

	mediaType, _, err := mime.ParseMediaType(contentType)
	if err != nil {
		cancel(c, exception.NewInvalidRequestModelValueException("contentType", contentType))
		return
	}

	// --- Validate fileName ---
	fileName := requiredField(body, "fileName", c)
	if c.IsAborted() {
		return
	}
	if strings.TrimSpace(fileName) == "" {
		cancel(c, exception.NewInvalidRequestModelValueException("fileName", fileName))
		return
	}
	if len([]rune(fileName)) > 250 {
		cancel(c, exception.NewInvalidRequestModelValueException("fileName", fileName))
		return
	}
	if ext := path.Ext(fileName); ext != "" && mime.TypeByExtension(ext) != mediaType {
		cancel(c, exception.NewInvalidRequestModelValueException("fileName", fileName))
		return
	}

	// --- Validate fileData ---
	fileData := requiredField(body, "fileData", c)
	if c.IsAborted() {
		return
	}
	if _, err := base64.StdEncoding.DecodeString(fileData); err != nil {
		cancel(c, exception.NewInvalidRequestModelValueException("fileData", fileData))
		return
	}
}

func cancel(c *gin.Context, err error) {
	c.Error(err)
	c.Abort()
}

func requiredField(body map[string]interface{}, field string, c *gin.Context) string {
	value, valid := body[field].(string)
	if !valid {
		cancel(c, exception.NewInvalidRequestModelFieldException(field))
		return ""
	}
	return value
}
