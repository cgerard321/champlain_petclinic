package controller

import (
	"encoding/json"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"io/ioutil"
	"mailer-service/mailer"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"
)

func TestMain(m *testing.M) {

	//Set Gin to Test Mode
	gin.SetMode(gin.TestMode)

	// Run the other tests
	os.Exit(m.Run())
}

func testHTTPResponse(t *testing.T, r *gin.Engine, req *http.Request, f func(w *httptest.ResponseRecorder) bool) {

	// Create a response recorder
	w := httptest.NewRecorder()

	// Create the service and process the above request.
	r.ServeHTTP(w, req)

	if !f(w) {
		t.Fail()
	}
}

func TestMailerControllerImpl_Routes(t *testing.T) {

	router := gin.Default()
	mC := MailerControllerImpl{}
	assert.Nil(t, mC.Routes(router))
}

func TestMailerControllerImpl_Unmarshalls(t *testing.T) {

	router := gin.Default()
	mC := MailerControllerImpl{}
	assert.Nil(t, mC.Routes(router))

	const email = "test@test.test"
	marshal, _ := json.Marshal(mailer.Mail{To: email, Subject: "Subject", Message: "Message"})
	serial := string(marshal)

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(serial))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	testHTTPResponse(t, router, req, func(w *httptest.ResponseRecorder) bool {

		assert.Equal(t, http.StatusOK, w.Code)
		body, err := ioutil.ReadAll(w.Result().Body)
		assert.Nil(t, err)
		assert.Contains(t, string(body), "Message sent to " + email)
		return true
	})
}

func TestMailerControllerImpl_ValidateInValidEmail(t *testing.T) {

	router := gin.Default()
	mC := MailerControllerImpl{}
	assert.Nil(t, mC.Routes(router))

	const email = ""
	const subject = ""
	const message = ""
	marshal, _ := json.Marshal(mailer.Mail{To: email, Subject: subject, Message: message})
	serial := string(marshal)

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(serial))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	testHTTPResponse(t, router, req, func(w *httptest.ResponseRecorder) bool {

		assert.Equal(t, http.StatusBadRequest, w.Code)
		body, err := ioutil.ReadAll(w.Result().Body)

		assert.Nil(t, err)
		assert.Equal(t,
			"\"Key: 'Mail.To' Error:Field validation for 'To' failed on the 'required' tag" +
				"\\nKey: 'Mail.Message' Error:Field validation for 'Message' failed on the 'required' tag\"",
			string(body))

		return true
	})


}

func TestHandleMailPOST_ValidMail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	const email = "test@test.test"
	const subject = "subject"
	const message = "message"
	mail := mailer.Mail{To: email, Subject: subject, Message: message}

	context.Set("mail", &mail)

	handleMailPOST(context)

	assert.Equal(t, http.StatusOK, recorder.Code)
	assert.Equal(t, fmt.Sprintf("\"Message sent to %s\"", email), recorder.Body.String())
}


func TestHandleMailPOST_NilMail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)

	context.Set("mail", nil)

	handleMailPOST(context)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)
	assert.Equal(t, "\"Unable to parse e-mail from body\"", recorder.Body.String())
}
