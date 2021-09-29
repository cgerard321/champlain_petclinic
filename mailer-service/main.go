package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"mailer-service/mailer"
	"mailer-service/mailer/controller"
	"mailer-service/mailer/service"
	"os"
)

func main() {

	engine := gin.Default()
	defer engine.Run()

	SMTP_SERVER := os.Getenv("SMTP_SERVER")
	SMTP_USER := os.Getenv("SMTP_USER")
	SMTP_PASS := os.Getenv("SMTP_PASS")

	mS := service.MailerServiceImpl{}
	mS.New(mailer.CreateDialer(SMTP_SERVER, SMTP_USER, SMTP_PASS))

	mC := controller.MailerControllerImpl{}
	mC.New(&mS)

	err := mC.Routes(engine)
	if err != nil {
		fmt.Println("Unable to route, exiting")
		os.Exit(1)
	}
}
