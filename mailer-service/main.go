package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	ginSwagger "github.com/swaggo/gin-swagger"
	"github.com/swaggo/gin-swagger/swaggerFiles"
	_ "mailer-service/docs"
	"mailer-service/mailer"
	"mailer-service/mailer/controller"
	"mailer-service/mailer/service"
	"os"
)

// @title Mailer API documentation
// @version 1.0.0
// @host localhost:8080
// @BasePath /mail
func main() {

	engine := gin.Default()
	defer engine.Run()

	engine.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

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
