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
	marshal, _ := json.Marshal(mailer.Mail{To: email, Subject: "", Message: message})
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