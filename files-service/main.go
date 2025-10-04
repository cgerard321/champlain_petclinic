package main

import (
	"crypto/tls"
	"database/sql"
	"files-service/files/clientlayer"
	"files-service/files/datalayer"
	"files-service/files/util"
	"fmt"
	"log"
	"net/http"

	"files-service/files/businesslayer"
	"files-service/files/presentationlayer"
	"os"

	"github.com/gin-gonic/gin"
	"github.com/minio/minio-go/v7/pkg/credentials"

	_ "github.com/go-sql-driver/mysql"
	"github.com/minio/minio-go/v7"
)

// @host localhost:8000
func main() {
	dsn := "user:pwd@tcp(mysql-files:3306)/files-db"
	db, err := sql.Open("mysql", dsn)
	if err != nil {
		panic(err)
	}
	defer db.Close()

	util.SetupDatabase(db)

	engine := gin.Default()
	defer engine.Run(":8000")

	endpoint := os.Getenv("FILE_ENDPOINT")
	accessKeyID := os.Getenv("FILE_ACCESS_KEY_ID")
	secretAccessKey := os.Getenv("FILE_SECRET_ACCESS_KEY")
	env := os.Getenv("FILE_ENV")

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
		util.SetupMinio(minioClient, accessKeyID, secretAccessKey)
	}

	ir := datalayer.NewFileInfoRepo(db)

	mc := clientlayer.NewMinioServiceClient(minioClient)
	is := businesslayer.NewFileService(ir, mc)

	ic := presentationlayer.NewFileController(is)

	err2 := ic.Routes(engine)
	if err2 != nil {
		fmt.Println("Unable to route, exiting")
		os.Exit(1)
	}
}
