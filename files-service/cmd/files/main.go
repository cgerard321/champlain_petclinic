package main

import (
	"crypto/tls"
	"database/sql"
	"files-service/internal/client"
	"files-service/internal/http/handlers"
	"files-service/internal/repository"
	"files-service/internal/service"
	"files-service/internal/util"
	"fmt"
	"log"
	"net/http"

	"os"

	"github.com/gin-gonic/gin"
	"github.com/minio/minio-go/v7/pkg/credentials"
	"github.com/prometheus/client_golang/prometheus/promhttp"

	_ "github.com/go-sql-driver/mysql"
	"github.com/minio/minio-go/v7"
)

// @host localhost:8000
func main() {
	dsn := os.Getenv("FILE_DATABASE")                      //database
	endpoint := os.Getenv("FILE_ENDPOINT")                 //minio
	accessKeyID := os.Getenv("FILE_ACCESS_KEY_ID")         //minio access key
	secretAccessKey := os.Getenv("FILE_SECRET_ACCESS_KEY") //minio secret key
	env := os.Getenv("FILE_ENV")                           //env, dev or prod
	devData := os.Getenv("FILE_DEV_DATA")                  //dev files location
	devBucket := os.Getenv("FILE_DEV_BUCKET")              //dev bucket

	db, err := sql.Open("mysql", dsn)
	if err != nil {
		panic(err)
	}
	defer db.Close()

	util.SetupDatabase(db)

	engine := gin.Default()
	engine.GET("/metrics", gin.WrapH(promhttp.Handler()))
	defer engine.Run(":8000")

	var minioOptions minio.Options

	if env == "dev" {
		minioOptions = minio.Options{
			Creds: credentials.NewStaticV4("user", "password", ""), //minio root user and password
		}
	} else if env == "prod" {
		minioOptions = minio.Options{
			Creds:  credentials.NewStaticV4(accessKeyID, secretAccessKey, ""),
			Secure: true,
			Transport: &http.Transport{
				TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
			},
		}
	}

	// Initialize minio client object.
	minioClient, err := minio.New(endpoint, &minioOptions)
	if err != nil {
		log.Fatalln(err)
	}

	//only for dev env
	if env == "dev" {
		util.SetupMinio(minioClient, accessKeyID, secretAccessKey, devData, devBucket)
	}

	ir := repository.NewFileInfoRepo(db)

	mc := client.NewMinioServiceClient(minioClient)
	is := service.NewFileService(ir, mc)

	ic := handlers.NewFileController(is)

	err2 := ic.Routes(engine)
	if err2 != nil {
		fmt.Println("Unable to route, exiting")
		os.Exit(1)
	}
}
