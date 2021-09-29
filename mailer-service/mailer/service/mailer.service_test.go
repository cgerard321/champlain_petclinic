package service

import (
	"os"
	"testing"
)

func TestMain(m *testing.M) {

	os.Exit(m.Run())
}

func TestMailerService_New(t *testing.T) {
	mS := MailerServiceImpl{}
	mS.New()
}