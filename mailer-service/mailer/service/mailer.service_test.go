package service

import (
	"bufio"
	"bytes"
	"fmt"
	"mailer-service/mailer"
	"net"
	"net/textproto"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
)

var ValidMail = &mailer.Mail{To: "example@test.com", Subject: "Test subject", Message: "Test message"}


func TestMain(m *testing.M) {

	os.Exit(m.Run())
}

func TestMailerService_New(t *testing.T) {
	mS := MailerServiceImpl{}
	dialer := mailer.CreateDialer("smtp.invalid.com", "a@b.c", "pass")
	mS.New(dialer)
}

func TestMailerServiceImpl_SendMailValidMail(t *testing.T) {
	mS := MailerServiceImpl{}
	const from = "a@b.c"
	dialer := mailer.CreateDialer("localhost", from, "pass", 2000)
	mS.New(dialer)

	get, err := startMockSMTPServer(2000)
	assert.Nil(t, err)

	assert.Nil(t, mS.SendMail(ValidMail))

	got, err := get()
	assert.Nil(t, err)
	assert.Contains(t, got, "From: " + from)
	assert.Contains(t, got, "To: " + ValidMail.To)
	assert.Contains(t, got, "Subject: " + ValidMail.Subject)
	assert.Contains(t, got, ValidMail.Message)
}

// Absolute legend https://titanwolf.org/Network/Articles/Article?AID=5749f0a3-9be8-4add-a1d3-9699e7554251#gsc.tab=0

type receivedMailTextGetter func() (string, error)
func startMockSMTPServer(port int, serverResponses ...string) (receivedMailTextGetter, error) {
	if len(serverResponses) == 0 {
		// default server responses
		serverResponses = []string{
			"220 smtp.example.com Service ready",
			"250-ELHO -> ok",
			"250-Show Options for ESMTP",
			"250-8BITMIME",
			"250-SIZE",
			"250-AUTH LOGIN PLAIN",
			"250 HELP",
			"235 AUTH -> ok",
			"250 MAIL FROM -> ok",
			"250 RCPT TO -> ok",
			"354 DATA",
			"250 ... -> ok",
			"221 QUIT",
		}
	}
	var errOrDone = make(chan error)

	var buffer bytes.Buffer
	bufferWriter := bufio.NewWriter(&buffer)

	mockSmtpServer, err := net.Listen("tcp", fmt.Sprintf("127.0.0.1:%d", port))
	if err != nil {
		return nil, err
	}

	// prevent data race on writer
	go func() {
		defer close(errOrDone)
		defer bufferWriter.Flush()
		defer mockSmtpServer.Close()

		conn, err := mockSmtpServer.Accept()
		if err != nil {
			errOrDone <- err
			return
		}
		defer conn.Close()

		tc := textproto.NewConn(conn)
		defer tc.Close()

	LoopServerResponse:
		for _, res := range serverResponses {
			if res == "" {
				break
			}

			tc.PrintfLine("%s", res)

			if len(res) >= 4 && res[3] == '-' {
				continue LoopServerResponse
			}

			if res == "221 QUIT" {
				return
			}

			for {
				msg, err := tc.ReadLine()
				if err != nil {
					errOrDone <- err
					return
				}
				bufferWriter.Write([]byte(msg + "\r\n"))

				if res != "354 DATA" || msg == "." {
					break
				}
			}
		}
	}()
	// a function for getting received data
	getReceivedData := func() (string, error) {
		err, hasErr := <-errOrDone
		// if catch error
		if hasErr {
			return "", err
		}
		return buffer.String(), nil
	}

	return getReceivedData, nil
}