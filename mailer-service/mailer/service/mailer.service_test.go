package service

import (
	"mailer-service/mailer"
	"os"
	"testing"
	// "github.com/stretchr/testify/assert"
)

// func getRandomPort() int {
//     l, err := net.Listen("tcp", "127.0.0.1:0")
//     if err != nil {
//         panic(err)
//     }
//     defer l.Close()
//     return l.Addr().(*net.TCPAddr).Port
// }

var ValidMail = &mailer.Mail{EmailSendTo: "emailSendTo", EmailTitle: "emailTitle", TemplateName: "templateName", Header: "header",
	    Body: "body", Footer: "footer", CorrespondantName: "correspondantName", SenderName: "senderName"}


func TestMain(m *testing.M) {

	os.Exit(m.Run())
}

func TestMailerService_New(t *testing.T) {
	mS := MailerServiceImpl{}
	dialer := mailer.CreateDialer("smtp.invalid.com", "a@b.c", "pass")
	mS.New(dialer)
}

// func TestMailerServiceImpl_SendMailValidMail(t *testing.T) {	
// 	port := getRandomPort()

// 	get, err := startMockSMTPServer(port)
// 	assert.Nil(t, err)

// 	time.Sleep(50 * time.Millisecond)

// 	mS := MailerServiceImpl{}
// 	const from = "a@b.c"
// 	dialer := mailer.CreateDialer("127.0.0.1", from, "pass", port)
// 	mS.New(dialer)

// 	t.Logf("Dialing %s:%d", "127.0.0.1", port)

// 	assert.Nil(t, mS.SendMail(ValidMail))

// 	got, err := get()
// 	assert.Nil(t, err)
// 	assert.Contains(t, got, "From: " + from)
// 	assert.Contains(t, got, "To: " + ValidMail.EmailSendTo)
// 	assert.Contains(t, got, "Subject: " + ValidMail.EmailTitle)
// 	assert.Contains(t, got, ValidMail.Body)
// 	assert.Contains(t, got, ValidMail.Header)
// 	assert.Contains(t, got, ValidMail.Footer)
// 	assert.Contains(t, got, ValidMail.SenderName)
// 	assert.Contains(t, got, ValidMail.CorrespondantName)
// }

// Absolute legend https://titanwolf.org/Network/Articles/Article?AID=5749f0a3-9be8-4add-a1d3-9699e7554251#gsc.tab=0

// type receivedMailTextGetter func() (string, error)
// func startMockSMTPServer(port int, serverResponses ...string) (receivedMailTextGetter, error) {
// 	if len(serverResponses) == 0 {
// 		// default server responses
// 		serverResponses = []string{
// 			"220 smtp.example.com Service ready",
// 			"250-ELHO -> ok",
// 			"250-Show Options for ESMTP",
// 			"250-8BITMIME",
// 			"250-SIZE",
// 			"250-AUTH LOGIN PLAIN",
// 			"250 HELP",
// 			"235 AUTH -> ok",
// 			"250 MAIL FROM -> ok",
// 			"250 RCPT TO -> ok",
// 			"354 DATA",
// 			"250 ... -> ok",
// 			"221 QUIT",
// 		}
// 	}
// 	var errOrDone = make(chan error)

// 	var buffer bytes.Buffer
// 	bufferWriter := bufio.NewWriter(&buffer)

// 	mockSmtpServer, err := net.Listen("tcp", fmt.Sprintf("127.0.0.1:%d", port))
// 	if err != nil {
// 		return nil, err
// 	}

// 	// prevent data race on writer
// 	go func() {
// 		defer close(errOrDone)
// 		defer bufferWriter.Flush()
// 		defer mockSmtpServer.Close()

// 		conn, err := mockSmtpServer.Accept()
// 		if err != nil {
// 			errOrDone <- err
// 			return
// 		}
// 		defer conn.Close()

// 		tc := textproto.NewConn(conn)
// 		defer tc.Close()

// 	LoopServerResponse:
// 		for _, res := range serverResponses {
// 			if res == "" {
// 				break
// 			}

// 			tc.PrintfLine("%s", res)

// 			if len(res) >= 4 && res[3] == '-' {
// 				continue LoopServerResponse
// 			}

// 			if res == "221 QUIT" {
// 				return
// 			}

// 			for {
// 				msg, err := tc.ReadLine()
// 				if err != nil {
// 					errOrDone <- err
// 					return
// 				}
// 				bufferWriter.Write([]byte(msg + "\r\n"))

// 				if res != "354 DATA" || msg == "." {
// 					break
// 				}
// 			}
// 		}
// 	}()
// 	// a function for getting received data
// 	getReceivedData := func() (string, error) {
// 		err, hasErr := <-errOrDone
// 		// if catch error
// 		if hasErr {
// 			return "", err
// 		}
// 		return buffer.String(), nil
// 	}

// 	return getReceivedData, nil
// }