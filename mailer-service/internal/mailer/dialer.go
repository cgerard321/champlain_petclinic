package mailer

import (
	"crypto/tls"

	gomail "gopkg.in/mail.v2"
)

type Config struct {
	Host               string
	Port               int
	Username           string
	Password           string
	InsecureSkipVerify bool
}

func NewDialer(cfg Config) *gomail.Dialer {
	port := cfg.Port

	if port == 0 {
		port = 587
	}

	d := gomail.NewDialer(cfg.Host, port, cfg.Username, cfg.Password)
	d.TLSConfig = &tls.Config{InsecureSkipVerify: cfg.InsecureSkipVerify}

	return d
}
