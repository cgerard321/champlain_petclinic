package middleware_test

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"

	mw "mailer-service/internal/http/middleware"
)

func TestUnmarshalMail_ValidPayload(t *testing.T) {
	gin.SetMode(gin.TestMode)

	r := gin.New()
	r.Use(mw.UnmarshalMail())

	r.POST("/mail", func(c *gin.Context) {
		if _, ok := c.Get("mail"); !ok {
			t.Fatalf("expected mail in context")
		}
		c.Status(http.StatusOK)
	})

	body := []byte(`{"to":"a@b.com","subject":"hi","body":"<p>x</p>"}`)
	req := httptest.NewRequest(http.MethodPost, "/mail", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")

	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("want 200, got %d: %s", w.Code, w.Body.String())
	}
}

func TestUnmarshalMail_InvalidPayload_Returns400(t *testing.T) {
	gin.SetMode(gin.TestMode)

	r := gin.New()

	r.Use(mw.UnmarshalMail())
	r.POST("/mail", func(c *gin.Context) { c.Status(http.StatusOK) })

	body := []byte(`{"to":"a@b.com","body":"<p>x</p>"}`)
	req := httptest.NewRequest(http.MethodPost, "/mail", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")

	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("want 400, got %d", w.Code)
	}
}

func TestUnmarshalMail_MalformedJSON_Returns400(t *testing.T) {
	gin.SetMode(gin.TestMode)

	r := gin.New()
	r.Use(mw.UnmarshalMail())
	r.POST("/mail", func(c *gin.Context) {
		c.Status(http.StatusOK)
	})

	body := `{"to":"a@b.com","subject":"hi","body":"<p>x</p>"`
	req := httptest.NewRequest(http.MethodPost, "/mail", strings.NewReader(body))
	req.Header.Set("Content-Type", "application/json")

	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("want 400, got %d", w.Code)
	}
}
