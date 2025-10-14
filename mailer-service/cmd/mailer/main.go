package main

import (
	"log"

	"github.com/gin-gonic/gin"
	ginSwagger "github.com/swaggo/gin-swagger"
	"github.com/swaggo/gin-swagger/swaggerFiles"

	"mailer-service/internal/http/handlers"
	"mailer-service/internal/http/middleware"
	mailsvc "mailer-service/internal/mailer"
	"mailer-service/internal/util"
)

// @title Mailer Service API
// @version 1.0
// @description REST endpoints for sending emails.
// @BasePath /
func main() {
	r := gin.Default()

	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	host := util.Getenv("SMTP_SERVER")
	user := util.Getenv("SMTP_USER")
	pass := util.Getenv("SMTP_PASS")

	dialer := mailsvc.NewDialer(mailsvc.Config{
		Host:               host,
		Port:               587,
		Username:           user,
		Password:           pass,
		InsecureSkipVerify: true,
	})

	svc := mailsvc.NewService(dialer, user)

	h := handlers.NewMailHandler(svc)
	g := r.Group("/mail")
	g.Use(middleware.UnmarshalMail())
	g.POST("", h.Post)

	log.Printf("Mailer Service using %s:587 as %s", host, user)

	if err := r.Run(); err != nil {
		log.Fatal(err)
	}
}
