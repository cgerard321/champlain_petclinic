package handlers_test

import (
	"bytes"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"

	"mailer-service/internal/http/handlers"
	mw "mailer-service/internal/http/middleware"
	pkg "mailer-service/pkg/mailer"
)

type mockService struct {
	sendErr error
	got     *pkg.Mail
}

func (m *mockService) Send(mm *pkg.Mail) error { m.got = mm; return m.sendErr }

func TestMailHandler_Post_Success(t *testing.T) {
	gin.SetMode(gin.TestMode)
	ms := &mockService{}
	h := handlers.NewMailHandler(ms)

	r := gin.New()
	r.Use(mw.UnmarshalMail())
	r.POST("/mail", h.Post)

	body := []byte(`{"to":"a@b.com","subject":"hi","body":"<p>x</p>","sender_name":"PetClinic"}`)
	req := httptest.NewRequest(http.MethodPost, "/mail", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("want 200, got %d: %s", w.Code, w.Body.String())
	}
	if ms.got == nil || ms.got.To != "a@b.com" {
		t.Fatalf("handler did not pass parsed mail to service")
	}
	if !strings.Contains(w.Body.String(), "Message sent to a@b.com") {
		t.Fatalf("unexpected body: %s", w.Body.String())
	}
}

func TestMailHandler_Post_ServiceError_Returns500(t *testing.T) {
	gin.SetMode(gin.TestMode)
	ms := &mockService{sendErr: errors.New("smtp down")}
	h := handlers.NewMailHandler(ms)

	r := gin.New()
	r.Use(mw.UnmarshalMail())
	r.POST("/mail", h.Post)

	body := []byte(`{"to":"a@b.com","subject":"hi"}`)
	req := httptest.NewRequest(http.MethodPost, "/mail", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusInternalServerError {
		t.Fatalf("want 500, got %d", w.Code)
	}
}

func TestMailHandler_Post_NoMailInContext_Returns400(t *testing.T) {
	gin.SetMode(gin.TestMode)
	ms := &mockService{}
	h := handlers.NewMailHandler(ms)

	r := gin.New()
	// intentionally do NOT use UnmarshalMail
	r.POST("/mail", h.Post)

	req := httptest.NewRequest(http.MethodPost, "/mail", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("want 400, got %d", w.Code)
	}
}

func TestMailHandler_Register_Smoke(t *testing.T) {
	gin.SetMode(gin.TestMode)

	ms := &mockService{}
	h := handlers.NewMailHandler(ms)

	r := gin.New()
	h.Register(r)

	req := httptest.NewRequest(http.MethodGet, "/mail", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", w.Code)
	}
}
