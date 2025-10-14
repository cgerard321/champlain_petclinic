package mailer

import (
	"testing"

	"github.com/go-playground/validator/v10"
)

func TestMailValidation(t *testing.T) {
	v := validator.New()

	if err := v.Struct(&Mail{}); err == nil {
		t.Fatal("expected validation error for empty struct")
	}

	m := &Mail{To: "a@b.com", Subject: "hi"}
	if err := v.Struct(m); err != nil {
		t.Fatalf("unexpected validation error: %v", err)
	}
}
