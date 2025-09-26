package util

import (
	"context"
	"crypto/tls"
	"database/sql"
	"files-service/files/datalayer"
	"log"
	"net/http"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

var defaultData = []datalayer.FileLink{
	{"3e5a214b-009d-4a25-9313-344676e6157d", "54746305265_48e2332383_c.jpg"},
}

func SetupDatabase(db *sql.DB) { //might add a column to the table for the file type
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	//creates the table
	_, err := db.ExecContext(ctx, `
    CREATE TABLE IF NOT EXISTS files (
        id INT AUTO_INCREMENT PRIMARY KEY,
        fileId VARCHAR(36) UNIQUE NOT NULL,
        url VARCHAR(255) UNIQUE NOT NULL
    )`)
	if err != nil {
		panic(err)
	}

	//makes sure that data is only added on the first execution
	var empty bool
	db.QueryRowContext(ctx, `SELECT EXISTS(SELECT 1 FROM files LIMIT 1)`).Scan(&empty)
	if empty {
		return
	}

	for _, img := range defaultData {
		_, err := db.ExecContext(ctx, `INSERT INTO files (fileId, url) VALUES (?, ?)`, img.FileId, img.Url)
		if err != nil {
			panic(err)
		}
	}

	log.Println("Database created")
}

func SetupMinio(lc *minio.Client, key string, secret string) { //prob should make a dictionary to link bucket name and image type
	exists, err := lc.BucketExists(context.Background(), "pet-clinic-images")

	if err != nil {
		log.Print(err)
		log.Fatalln(err)
	}

	if exists {
		return
	}

	err = lc.MakeBucket(context.Background(), "pet-clinic-images", minio.MakeBucketOptions{})

	if err != nil {
		log.Fatalln(err)
	}

	ec, err := minio.New("pet-clinic-bucket.benmusicgeek.synology.me", &minio.Options{
		Creds:  credentials.NewStaticV4(key, secret, ""),
		Secure: true,
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	})
	if err != nil {
		log.Fatalln(err)
	}

	for _, file := range defaultData {
		object, err := ec.GetObject(context.Background(), "pet-clinic-images", "images/"+file.Url, minio.GetObjectOptions{})
		if err != nil {
			log.Fatalln(err)
		}

		stats, err := object.Stat()
		if err != nil {
			log.Fatalln(err)
		}

		_, err = lc.PutObject(context.Background(), "pet-clinic-images", file.Url, object, stats.Size, minio.PutObjectOptions{})
		if err != nil {
			log.Fatalln(err)
		}

		object.Close()
	}
}
