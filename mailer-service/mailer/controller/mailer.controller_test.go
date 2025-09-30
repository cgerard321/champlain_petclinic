package controller

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"mailer-service/mailer"
	"mailer-service/mailer/service"
	"net"
	"net/http"
	"net/http/httptest"
	"net/textproto"
	"os"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	gomail "gopkg.in/mail.v2"
)

type MailerServiceMock struct {
	mock.Mock
}

func (m MailerServiceMock) New(dialer *gomail.Dialer) {
}

func (m MailerServiceMock) SendMail(mail *mailer.Mail) error {
	return nil
}

func TestMain(m *testing.M) {

	//Set Gin to Test Mode
	gin.SetMode(gin.TestMode)

	// Run the other tests
	os.Exit(m.Run())
}

func testHTTPResponse(t *testing.T, r *gin.Engine, req *http.Request, f func(w *httptest.ResponseRecorder) bool) {

	// Create a response recorder
	w := httptest.NewRecorder()

	// Create the service and process the above request.
	r.ServeHTTP(w, req)

	if !f(w) {
		t.Fail()
	}
}

func TestMailerControllerImpl_Routes(t *testing.T) {

	router := gin.Default()
	mC := MailerControllerImpl{}
	assert.Nil(t, mC.Routes(router))
}

func TestMailerControllerImpl_Unmarshalls(t *testing.T) {

	router := gin.Default()

	mS := MailerServiceMock{}
	mC := MailerControllerImpl{}
	mC.New(mS)
	assert.Nil(t, mC.Routes(router))

	const email = "test@test.test"
	marshal, _ := json.Marshal(mailer.Mail{EmailSendTo: email, EmailTitle: "emailTitle", TemplateName: "templateName",
		Header: "header", Body: "body", Footer: "footer",
		CorrespondantName: "correspondantName",
		SenderName:        "senderName"})
	serial := string(marshal)

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(serial))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	testHTTPResponse(t, router, req, func(w *httptest.ResponseRecorder) bool {

		assert.Equal(t, http.StatusOK, w.Code)
		body, err := ioutil.ReadAll(w.Result().Body)
		assert.Nil(t, err)
		assert.Contains(t, string(body), "Message sent to "+email)
		return true
	})
}

func TestMailerControllerImpl_ValidateInValidEmail(t *testing.T) {

	router := gin.Default()
	mC := MailerControllerImpl{}
	assert.Nil(t, mC.Routes(router))

	const emailSendTo = ""
	const emailTitle = ""
	const templateName = ""
	const header = ""
	const body = ""
	const footer = ""
	const correspondantName = ""
	const senderName = ""
	marshal, _ := json.Marshal(mailer.Mail{EmailSendTo: emailSendTo, EmailTitle: emailTitle, TemplateName: templateName, Header: header,
		Body: body, Footer: footer, CorrespondantName: correspondantName, SenderName: senderName})
	serial := string(marshal)

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(serial))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	testHTTPResponse(t, router, req, func(w *httptest.ResponseRecorder) bool {

		assert.Equal(t, http.StatusBadRequest, w.Code)
		body, err := ioutil.ReadAll(w.Result().Body)

		assert.Nil(t, err)
		assert.Equal(t,
			"\"Key: 'Mail.EmailSendTo' Error:Field validation for 'EmailSendTo' failed on the 'required' tag\\nKey: 'Mail.EmailTitle' Error:Field validation for 'EmailTitle' failed on the 'required' tag\"",
			string(body))

		return true
	})

}

func TestHandleMailPOST_ValidMail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)
	mS := MailerServiceMock{}
	mC := MailerControllerImpl{}
	mC.New(mS)

	const emailSendTo = "test@test.test"
	const emailTitle = "Test Email Title"
	const templateName = "test TemplateName"
	const header = "test Header"
	const body = "Body Testing, testing, 1, 2, 3"
	const footer = "Footer Testing, testing, 1, 2, 3"
	const correspondantName = "Test Correspondant Name"
	const senderName = "Test Sender Name"

	mail := mailer.Mail{EmailSendTo: emailSendTo, EmailTitle: emailTitle, TemplateName: templateName, Header: header,
		Body: body, Footer: footer, CorrespondantName: correspondantName, SenderName: senderName}

	context.Set("mail", &mail)

	mC.handleMailPost(context)

	assert.Equal(t, http.StatusOK, recorder.Code)
	assert.Equal(t, fmt.Sprintf("\"Message sent to %s\"", emailSendTo), recorder.Body.String())
}

func TestHandleMailPOST_NilMail(t *testing.T) {

	recorder := httptest.NewRecorder()
	context, _ := gin.CreateTestContext(recorder)
	mC := MailerControllerImpl{}

	context.Set("mail", nil)

	mC.handleMailPost(context)

	assert.Equal(t, http.StatusBadRequest, recorder.Code)
	assert.Equal(t, "\"Unable to parse e-mail from body\"", recorder.Body.String())
}

func getRandomPort() int {
	s, e := net.Listen("tcp", ":0")
	if s == nil || e != nil {
		panic("Unable to create a new port")
	}
	defer s.Close()

	return s.Addr().(*net.TCPAddr).Port
}

func TestHandleMailPOST_Full(t *testing.T) {

	port := getRandomPort()
	get, err := startMockSMTPServer(port)
	fmt.Println(err)
	assert.Nil(t, err)

	fullTestEnv(t, port, func(engine *gin.Engine, req *http.Request, m *mailer.Mail) {

		testHTTPResponse(t, engine, req, func(w *httptest.ResponseRecorder) bool {

			assert.Equal(t, http.StatusOK, w.Code)
			got, err := get()
			assert.Nil(t, err)
			assert.Contains(t, got, "To: "+m.EmailSendTo)
			assert.Contains(t, got, "Subject: "+m.EmailTitle)
			// 			assert.Contains(t, got, m.Body)
			assert.Equal(t, fmt.Sprintf("\"Message sent to %s\"", m.EmailSendTo), w.Body.String())
			return true
		})
	})
}

func TestHandleMailPOST_FullInValid(t *testing.T) {

	fullTestEnv(t, getRandomPort(), func(engine *gin.Engine, req *http.Request, _ *mailer.Mail) {

		testHTTPResponse(t, engine, req, func(w *httptest.ResponseRecorder) bool {

			assert.Equal(t, http.StatusInternalServerError, w.Code)
			// assert.Contains(t, w.Body.String(), "actively refused" || "connection refused")
			body := w.Body.String()
			ok := strings.Contains(body, "actively refused") ||
				strings.Contains(body, "connection refused")

			assert.True(t, ok, "unexpected error body: %q", body)
			return true
		})
	})
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

func fullTestEnv(t *testing.T, port int, f func(e *gin.Engine, r *http.Request, m *mailer.Mail)) {

	engine := gin.Default()

	mS := service.MailerServiceImpl{}
	dialer := mailer.CreateDialer("localhost", "a@b.c", "pass", port)
	mS.New(dialer)

	mC := MailerControllerImpl{}
	mC.New(&mS)
	assert.Nil(t, mC.Routes(engine))

	const emailSendTo = "test@test.test"
	const emailTitle = "Test Email Title"
	const templateName = "test TemplateName"
	const header = "test Header"
	const body = "Body Testing, testing, 1, 2, 3"
	const footer = "Footer Testing, testing, 1, 2, 3"
	const correspondantName = "Test Correspondant Name"
	const senderName = "TestSenderName@gmail.com"

	mail := mailer.Mail{EmailSendTo: emailSendTo, EmailTitle: emailTitle, TemplateName: templateName, Header: header,
		Body: body, Footer: footer, CorrespondantName: correspondantName, SenderName: senderName}

	marshal, _ := json.Marshal(mail)
	serial := string(marshal)

	req, err := http.NewRequest(http.MethodPost, "/mail", strings.NewReader(serial))

	if err != nil {
		fmt.Println(err)
		t.Fatal(err)
	}

	f(engine, req, &mail)
}
