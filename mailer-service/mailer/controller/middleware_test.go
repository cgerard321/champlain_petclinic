package controller

import (
	"encoding/json"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"mailer-service/mailer"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestUnMarshallMailValid(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	const email = "test@test.test"
	const subject = "subject"
	const message = "message"
	marshal, _ := json.Marshal(mailer.Mail{To: email, Subject: subject, Message: message})
	serial := string(marshal)

	context.Request, _ = http.NewRequest("test-method", "test-url", strings.NewReader(serial))
	UnMarshallMail(context)

	get, exists := context.Get("mail")

	assert.True(t, exists)

	mail := get.(*mailer.Mail)

	assert.Equal(t, email, mail.To)
	assert.Equal(t, subject, mail.Subject)
	assert.Equal(t, message, mail.Message)
}

func TestUnMarshallMailValidEmptySubject(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	const email = "test@test.test"
	const subject = ""
	const message = "message"
	marshal, _ := json.Marshal(mailer.Mail{To: email, Subject: subject, Message: message})
	serial := string(marshal)

	context.Request, _ = http.NewRequest("test-method", "test-url", strings.NewReader(serial))
	UnMarshallMail(context)

	get, exists := context.Get("mail")

	assert.True(t, exists)

	mail := get.(*mailer.Mail)

	assert.Equal(t, email, mail.To)
	assert.Equal(t, subject, mail.Subject)
	assert.Equal(t, message, mail.Message)
}

func TestUnMarshallMailInvalidMail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	context.Request, _ = http.NewRequest("test-method", "test-url", strings.NewReader("invalid-test"))
	UnMarshallMail(context)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)
}

func TestUnMarshallMailNilMail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	context.Request, _ = http.NewRequest("test-method", "test-url", nil)
	UnMarshallMail(context)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)
}


func TestValidateEmailInValidEmail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	context.Set("mail", &mailer.Mail{})
	ValidateEmail(context)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)
	assert.Equal(t, "\"Key: 'Mail.To' Error:Field validation for 'To' failed on the 'required' tag\\nKey: 'Mail.Message' Error:Field validation for 'Message' failed on the 'required' tag\"", recorder.Body.String())
}


func TestValidateEmailNilEmail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	context.Set("mail", nil)
	ValidateEmail(context)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)
	assert.Equal(t, "\"e-mail not found\"", recorder.Body.String())
}

func TestValidateEmailValidEmail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	const email = "test@example.com"
	const subject = ""
	const message = "test"
	context.Set("mail", &mailer.Mail{To: email, Subject: subject, Message: message})

	ValidateEmail(context)

	assert.Equal(t, http.StatusOK, recorder.Code)
}