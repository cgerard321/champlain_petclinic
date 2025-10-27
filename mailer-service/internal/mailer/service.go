package mailer

import (
	"log"
	"time"

	gomail "gopkg.in/mail.v2"

	pkg "mailer-service/pkg/mailer"
)

const (
	MAX_RETRIES = 3
	RETRY_DELAY = 5 * time.Second
)

type Service interface {
	Send(m *pkg.Mail) error
	ProcessEmailJob(job EmailJob)
}

// this struct represents an email sending task with retry information.
type EmailJob struct {
	Mail *pkg.Mail
	RetryCount int
}

type service struct {
	dialer *gomail.Dialer
	from   string
	EmailJobChannel chan EmailJob
}

func NewService(dialer *gomail.Dialer, from string, emailJobChannel chan EmailJob) Service {
	return &service{dialer: dialer, from: from, EmailJobChannel: emailJobChannel}
}

func (s *service) Send(m *pkg.Mail) error {
	s.EmailJobChannel <- EmailJob{Mail: m, RetryCount: 0}
	return nil
}

func (s *service) ProcessEmailJob(job EmailJob) {
	for attempts := 0; attempts <= MAX_RETRIES; attempts++ {
		if attempts > 0 {
			log.Printf("Retrying email (attempt %d/%d)", attempts, MAX_RETRIES)
			time.Sleep(RETRY_DELAY)
		}

		msg := gomail.NewMessage()

		if job.Mail.SenderName != "" {
			msg.SetAddressHeader("From", s.from, job.Mail.SenderName)
		} else {
			msg.SetHeader("From", s.from)
		}

		msg.SetHeader("To", job.Mail.To)
		msg.SetHeader("Subject", job.Mail.Subject)

		msg.SetBody("text/html", job.Mail.Body)

		err := s.dialer.DialAndSend(msg)
		if err != nil {
			log.Printf("Failed to send email (attempt %d/%d): %v", attempts, MAX_RETRIES, err)
			if attempts == MAX_RETRIES {
				log.Printf("Permanently failed to send email after %d retries", MAX_RETRIES)
				break
			}
		} else {
			log.Printf("Email sent successfully")
			break // success
		}
	}
}

var _ Service = (*service)(nil)
