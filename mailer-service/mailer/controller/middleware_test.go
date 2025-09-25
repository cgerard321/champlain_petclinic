package controller

import (
	"encoding/json"
	"mailer-service/mailer"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func TestUnMarshallMailValid(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	const emailSendTo = "test@test.test"
	const templateName = "test TemplateName"
	const header = "test Header"
	const body = "Body Testing, testing, 1, 2, 3"
	const footer = "Footer Testing, testing, 1, 2, 3"
	const correspondantName = "Test Correspondant Name"
	const senderName = "Test Sender Name"
	marshal, _ := json.Marshal(mailer.Mail{EmailSendTo: emailSendTo, TemplateName: templateName, Header: header,
	    Body: body, Footer: footer, CorrespondantName: correspondantName, SenderName: senderName})
	serial := string(marshal)

	context.Request, _ = http.NewRequest("test-method", "test-url", strings.NewReader(serial))
	UnMarshallMail(context)

	get, exists := context.Get("mail")
	
	assert.True(t, exists)

	mail := get.(*mailer.Mail)

	assert.Equal(t, emailSendTo, mail.EmailSendTo)
	assert.Equal(t, templateName, mail.TemplateName)
	assert.Equal(t, header, mail.Header)
	assert.Equal(t, body, mail.Body)
	assert.Equal(t, footer, mail.Footer)
	assert.Equal(t, correspondantName, mail.CorrespondantName)
	assert.Equal(t, senderName, mail.SenderName)
}

func TestUnMarshallMailValidEmptytemplateName(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	
	const emailSendTo = "test@test.test"
	const templateName = ""
	const header = "test Header"
	const body = "Body Testing, testing, 1, 2, 3"
	const footer = "Footer Testing, testing, 1, 2, 3"
	const correspondantName = "Test Correspondant Name"
	const senderName = "Test Sender Name"
	marshal, _ := json.Marshal(mailer.Mail{EmailSendTo: emailSendTo, TemplateName: templateName, Header: header,
	    Body: body, Footer: footer, CorrespondantName: correspondantName, SenderName: senderName})
	serial := string(marshal)

	context.Request, _ = http.NewRequest("test-method", "test-url", strings.NewReader(serial))
	UnMarshallMail(context)

	get, exists := context.Get("mail")

	assert.True(t, exists)

	mail := get.(*mailer.Mail)

	assert.Equal(t, emailSendTo, mail.EmailSendTo)
	assert.Empty(t, mail.TemplateName)
	assert.Equal(t, header, mail.Header)
	assert.Equal(t, body, mail.Body)
	assert.Equal(t, footer, mail.Footer)
	assert.Equal(t, correspondantName, mail.CorrespondantName)
	assert.Equal(t, senderName, mail.SenderName)
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

	const emailSendTo = "test@test.test"
	const templateName = "test TemplateName"
	const header = "test Header"
	const body = "Body Testing, testing, 1, 2, 3"
	const footer = "Footer Testing, testing, 1, 2, 3"
	const correspondantName = "Test Correspondant Name"
	const senderName = "Test Sender Name"
	context.Set("mail", &mailer.Mail{EmailSendTo: emailSendTo, TemplateName: templateName, Header: header,
	    Body: body, Footer: footer, CorrespondantName: correspondantName, SenderName: senderName})

	ValidateEmail(context)

	assert.Equal(t, http.StatusOK, recorder.Code)
}