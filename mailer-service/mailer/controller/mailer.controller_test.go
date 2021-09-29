package controller

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
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

func TestAnyResponseFromMailerEndpoint(t *testing.T) {

	router := gin.Default()

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(""))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	testHTTPResponse(t, router, req, func(w *httptest.ResponseRecorder) bool {

		assert.Equal(t, http.StatusOK, w.Code)
		return true
	})
}
