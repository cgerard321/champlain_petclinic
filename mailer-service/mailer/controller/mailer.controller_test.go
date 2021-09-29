package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"io/ioutil"
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

	const email = "example@test.com"

	bodyS := fmt.Sprintf("\"to\": \"%s\", \"message\": \"%s\", \"subject\": \"%s\"", email, "message", "subject")

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(bodyS))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	testHTTPResponse(t, router, req, func(w *httptest.ResponseRecorder) bool {

		assert.Equal(t, http.StatusOK, w.Code)
		body, err := ioutil.ReadAll(w.Result().Body)
		assert.Nil(t, err)
		assert.Contains(t, body, "Message sent to " + email)
		return true
	})
}
