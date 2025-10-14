package util_test

import (
	"os"
	"os/exec"
	"testing"

	"mailer-service/internal/util"
)

func TestGetenv_Success(t *testing.T) {
	const key, val = "SOME_ENV_KEY", "ok"
	t.Setenv(key, val)
	got := util.Getenv(key)
	if got != val {
		t.Fatalf("expected %q, got %q", val, got)
	}
}

func TestGetenv_Missing_Helper(t *testing.T) {
	if os.Getenv("GO_WANT_HELPER_PROCESS") != "1" {
		t.SkipNow()
	}

	_ = util.Getenv("MISSING_ENV_KEY")
	t.Fatal("should not reach here")
}

func TestGetenv_MissingVariable(t *testing.T) {
	cmd := exec.Command(os.Args[0], "-test.run=TestGetenv_Missing_Helper")
	cmd.Env = append(os.Environ(), "GO_WANT_HELPER_PROCESS=1")
	if err := cmd.Run(); err == nil {
		t.Fatalf("expected process to exit with error when env is missing")
	}
}
