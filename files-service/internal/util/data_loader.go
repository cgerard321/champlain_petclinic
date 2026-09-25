package util

import (
	"context"
	"crypto/tls"
	"database/sql"
	"files-service/internal/domain"
	"log"
	"net/http"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

var defaultData = []domain.FileInfo{
	{"3e5a214b-009d-4a25-9313-344676e6157d", "petclinic base image", "image/jpg"},
}

func WaitForDatabase(db *sql.DB) {
	delay := 2 * time.Second
	maxDelay := 15 * time.Second

	for attempt := 0; attempt < 10; attempt++ {
		ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
		err := db.PingContext(ctx)
		cancel()

		if err == nil {
			log.Printf("Connected to the database on attempt %d", attempt+1)
			return
		}

		log.Printf("Failed to connect to the database on attempt %d, retrying in %s", attempt+1, delay)
		time.Sleep(delay)

		delay = delay * 2
		if delay > maxDelay {
			delay = maxDelay
		}
	}

	log.Fatalf("Failed to connect to the database after %d attempts", 10)
}

func SetupDatabase(db *sql.DB) {
	WaitForDatabase(db)
	SetupTable(db)
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	//makes sure that data is only added on the first execution
	var empty bool
	db.QueryRowContext(ctx, `SELECT EXISTS(SELECT 1 FROM files LIMIT 1)`).Scan(&empty)
	if empty {
		return
	}

	for _, file := range defaultData {
		_, err := db.ExecContext(ctx, `INSERT INTO files (fileId, fileName, fileType) VALUES (?, ?, ?)`, file.FileId, file.FileName, file.FileType)
		if err != nil {
			panic(err)
		}
	}
}

func SetupTable(db *sql.DB) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	//creates the table
	_, err := db.ExecContext(ctx, `
    CREATE TABLE IF NOT EXISTS files (
        id INT AUTO_INCREMENT PRIMARY KEY,
        fileId VARCHAR(36) UNIQUE NOT NULL,
        fileName VARCHAR(255) NOT NULL,
        fileType varchar(20) NOT NULL
    )`)
	if err != nil {
		panic(err)
	}
}

/*
SetupMinio is a function that is only meant to run on the dev build.
It pulls all the default files from the dev bucket and loads them to the dev minio bucket storage
*/
func SetupMinio(lc *minio.Client, key string, secret string, devData string, devBucket string) {
	ec, err := minio.New(devData, &minio.Options{
		Creds:  credentials.NewStaticV4(key, secret, ""),
		Secure: true,
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	})
	if err != nil {
		log.Fatalln(err)
	}

	c := context.Background()
	for _, bucket := range domain.Buckets {
		exists, err := lc.BucketExists(c, bucket)
		if err != nil {
			log.Fatalln(err)
		}

		if exists {
			continue
		}

		err = lc.MakeBucket(c, bucket, minio.MakeBucketOptions{})

		if err != nil {
			log.Fatalln(err)
		}
	}

	for _, fileInfo := range defaultData {
		object, err := ec.GetObject(c, devBucket, fileInfo.GetDevFileLink(), minio.GetObjectOptions{})
		if err != nil {
			log.Fatalln(err)
		}

		stats, err := object.Stat()
		if err != nil {
			log.Fatalln(err)
		}

		_, err = lc.PutObject(c, fileInfo.GetFileBucket(), fileInfo.GetFileLink(), object, stats.Size, minio.PutObjectOptions{ContentType: stats.ContentType})
		if err != nil {
			log.Fatalln(err)
		}

		object.Close()
	}
}
