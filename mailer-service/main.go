package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
	"mailer-service/mailer/controller"
	"os"
)

func main() {

	engine := gin.Default()
	defer engine.Run()

	mC := controller.MailerControllerImpl{}

	err := mC.Routes(engine)
	if err != nil {
		fmt.Println("Unable to route, exiting")
		os.Exit(1)
	}
}
