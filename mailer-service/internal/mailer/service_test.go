package mailer

import (
	"bufio"
	"fmt"
	"net"
	"strings"
	"testing"

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
	s := NewService(d, "from@example.com")

	err := s.Send(&pkg.Mail{
		To:         "rcpt@example.com",
		Subject:    "Hello",
		Body:       "<p>world</p>",
		SenderName: "PetClinic",
	})
	if err != nil {
		t.Fatalf("send failed: %v", err)
	}
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
	s := NewService(d, "from@example.com")

	err := s.Send(&pkg.Mail{
		To:      "rcpt@example.com",
		Subject: "No Name",
		Body:    "<p>hello</p>",
	})
	if err != nil {
		t.Fatalf("send failed: %v", err)
	}
}

func TestService_Send_DialError_Propagates(t *testing.T) {
	badPort := unreachablePort(t)
	d := NewDialer(Config{
		Host: "127.0.0.1", Port: badPort, InsecureSkipVerify: true,
	})
	s := NewService(d, "from@example.com")

	err := s.Send(&pkg.Mail{
		To:      "rcpt@example.com",
		Subject: "x",
		Body:    "<p>x</p>",
	})
	if err == nil {
		t.Fatalf("expected error dialing unreachable SMTP server")
	}
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
	s := NewService(d, "from@example.com")

	if err := s.Send(&pkg.Mail{
		To:      "mini@example.com",
		Subject: "x",
		Body:    "<p>x</p>",
	}); err != nil {
		t.Fatalf("send failed: %v", err)
	}
}
