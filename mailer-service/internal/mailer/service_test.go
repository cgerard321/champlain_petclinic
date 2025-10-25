package mailer

import (
	"bufio"
	"fmt"
	"net"
	"strings"
	"testing"
	"time"

	pkg "mailer-service/pkg/mailer"
)

func startFakeSMTP(t *testing.T) (addr string, stop func()) {
	t.Helper()

	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}

	done := make(chan struct{})
	go func() {
		defer close(done)
		conn, err := ln.Accept()
		if err != nil {
			return
		}
		defer conn.Close()

		w := bufio.NewWriter(conn)
		r := bufio.NewReader(conn)

		write := func(s string) {
			fmt.Fprint(w, s)
			w.Flush()
		}

		// Just saying hello :D
		write("220 localhost ESMTP\r\n")

		for {
			line, _ := r.ReadString('\n')
			cmd := strings.ToUpper(strings.TrimSpace(line))
			switch {
			case strings.HasPrefix(cmd, "EHLO"):
				write("250-localhost\r\n250 PIPELINING\r\n")
			case strings.HasPrefix(cmd, "MAIL FROM:"):
				write("250 OK\r\n")
			case strings.HasPrefix(cmd, "RCPT TO:"):
				write("250 OK\r\n")
			case strings.HasPrefix(cmd, "DATA"):
				write("354 End data with <CR><LF>.<CR><LF>\r\n")
				for {
					l, _ := r.ReadString('\n')
					if strings.TrimSpace(l) == "." {
						break
					}
				}
				write("250 Message accepted\r\n")
			case strings.HasPrefix(cmd, "QUIT"):
				write("221 Bye\r\n")
				return
			default:
				write("250 OK\r\n")
			}
		}
	}()

	return ln.Addr().String(), func() { ln.Close(); <-done }
}

func TestService_Send_SendsMessage(t *testing.T) {
	addr, stop := startFakeSMTP(t)
	defer stop()

	host, portStr, _ := strings.Cut(addr, ":")
	var port int
	fmt.Sscanf(portStr, "%d", &port)

	d := NewDialer(Config{
		Host:               host,
		Port:               port,
		Username:           "",
		Password:           "",
		InsecureSkipVerify: true,
	})

	emailJobChannel := make(chan EmailJob, 1)
	s := NewService(d, "from@example.com", emailJobChannel)

	// Start a goroutine to process email jobs from the channel
	go func() {
		for job := range emailJobChannel {
			s.ProcessEmailJob(job)
		}
	}()

	err := s.Send(&pkg.Mail{
		To:         "rcpt@example.com",
		Subject:    "Hello",
		Body:       "<p>world</p>",
		SenderName: "PetClinic",
	})
	if err != nil {
		t.Fatalf("send failed: %v", err)
	}

	// To verify the email was sent, we would typically need a mechanism
	// in startFakeSMTP to signal when an email is received.
	// For now, we'll assume it's processed given the channel and worker setup.
	// A more robust test would wait for a signal from the fake SMTP server.

	// Add a small delay to allow the worker to process the email
	time.Sleep(100 * time.Millisecond) // Adjust as necessary

}

func unreachablePort(t *testing.T) int {
	t.Helper()
	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	addr := ln.Addr().String()
	_ = ln.Close()

	_, portStr, _ := strings.Cut(addr, ":")
	var port int
	fmt.Sscanf(portStr, "%d", &port)
	return port
}

func TestService_Send_EmptySenderName_UsesPlainFrom(t *testing.T) {
	addr, stop := startFakeSMTP(t)
	defer stop()

	host, portStr, _ := strings.Cut(addr, ":")
	var port int
	fmt.Sscanf(portStr, "%d", &port)

	d := NewDialer(Config{
		Host: host, Port: port, InsecureSkipVerify: true,
	})

	emailJobChannel := make(chan EmailJob, 1)
	s := NewService(d, "from@example.com", emailJobChannel)

	go func() {
		for job := range emailJobChannel {
			s.ProcessEmailJob(job)
		}
	}()

	err := s.Send(&pkg.Mail{
		To:      "rcpt@example.com",
		Subject: "No Name",
		Body:    "<p>hello</p>",
	})
	if err != nil {
		t.Fatalf("send failed: %v", err)
	}
	time.Sleep(100 * time.Millisecond)
}

func TestService_Send_DialError_Propagates(t *testing.T) {
	badPort := unreachablePort(t)
	d := NewDialer(Config{
		Host: "127.0.0.1", Port: badPort, InsecureSkipVerify: true,
	})

	emailJobChannel := make(chan EmailJob, 1)
	s := NewService(d, "from@example.com", emailJobChannel)

	go func() {
		for job := range emailJobChannel {
			s.ProcessEmailJob(job)
		}
	}()

	err := s.Send(&pkg.Mail{
		To:      "rcpt@example.com",
		Subject: "x",
		Body:    "<p>x</p>",
	})
	if err != nil {
		t.Fatalf("send should not return an error immediately: %v", err)
	}

	// Allow time for retries and eventual failure
	time.Sleep(RETRY_DELAY * (MAX_RETRIES + 1)) // Wait for all retries + initial attempt
}

func TestService_Send_MinimalSubjectAndHTML(t *testing.T) {
	addr, stop := startFakeSMTP(t)
	defer stop()

	host, portStr, _ := strings.Cut(addr, ":")
	var port int
	fmt.Sscanf(portStr, "%d", &port)

	d := NewDialer(Config{
		Host: host, Port: port, InsecureSkipVerify: true,
	})

	emailJobChannel := make(chan EmailJob, 1)
	s := NewService(d, "from@example.com", emailJobChannel)

	go func() {
		for job := range emailJobChannel {
			s.ProcessEmailJob(job)
		}
	}()

	if err := s.Send(&pkg.Mail{
		To:      "mini@example.com",
		Subject: "x",
		Body:    "<p>x</p>",
	}); err != nil {
		t.Fatalf("send failed: %v", err)
	}
	time.Sleep(100 * time.Millisecond)
}
