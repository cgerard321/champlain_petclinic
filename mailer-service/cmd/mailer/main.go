package main

import (
	"log"
	"time"

	"github.com/gin-gonic/gin"
	ginSwagger "github.com/swaggo/gin-swagger"
	"github.com/swaggo/gin-swagger/swaggerFiles"

	"mailer-service/internal/http/handlers"
	"mailer-service/internal/http/middleware"
	mailsvc "mailer-service/internal/mailer"
	"mailer-service/internal/util"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

const (
	MAX_RETRIES = 3
	RETRY_DELAY = 5 * time.Second
	EMAIL_QUEUE_CAPACITY = 100 //buffer size
	NUM_WORKERS = 5 
)

// @title Mailer Service API
// @version 1.0
// @description REST endpoints for sending emails.
// @BasePath /
func main() {
	r := gin.Default()

	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
	r.GET("/metrics", gin.WrapH(promhttp.Handler()))

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

	emailJobChannel := make(chan mailsvc.EmailJob, EMAIL_QUEUE_CAPACITY)
	svc := mailsvc.NewService(dialer, user, emailJobChannel)

	h := handlers.NewMailHandler(svc)
	g := r.Group("/mail")
	g.Use(middleware.UnmarshalMail())
	g.POST("", h.Post)

	log.Printf("Mailer Service using %s:587", host)

	for i := 0; i < NUM_WORKERS; i++ {
		go func(workerID int) {
			log.Printf("Worker %d started", workerID)
			for job := range emailJobChannel {
				svc.ProcessEmailJob(job)
			}
		}(i)
	}

	if err := r.Run(); err != nil {
		log.Fatal(err)
	}
}
