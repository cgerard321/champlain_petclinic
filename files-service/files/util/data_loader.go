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

var defaultData = []datalayer.FileInfo{
	{"3e5a214b-009d-4a25-9313-344676e6157d", "54746305265_48e2332383_c.jpg", "image"},
}

func SetupDatabase(db *sql.DB) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	//creates the table
	_, err := db.ExecContext(ctx, `
    CREATE TABLE IF NOT EXISTS files (
        id INT AUTO_INCREMENT PRIMARY KEY,
        fileType varchar(20) NOT NULL,
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

	for _, file := range defaultData {
		_, err := db.ExecContext(ctx, `INSERT INTO files (fileId, url, fileType) VALUES (?, ?, ?)`, file.FileId, file.Url, file.FileType)
		if err != nil {
			panic(err)
		}
	}
}

func SetupMinio(lc *minio.Client, key string, secret string) {
	ec, err := minio.New("petclinic-bucket.benmusicgeek.synology.me", &minio.Options{
		Creds:  credentials.NewStaticV4(key, secret, ""),
		Secure: true,
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	})
	if err != nil {
		log.Fatalln(err)
	}

	grouped := make(map[string][]datalayer.FileInfo)
	for _, f := range defaultData {
		grouped[f.FileType] = append(grouped[f.FileType], f)
	}

	c := context.Background()
	for fileType, files := range grouped {
		bucket := datalayer.Buckets[fileType]
		err := lc.MakeBucket(c, bucket, minio.MakeBucketOptions{})

		if err != nil {
			log.Fatalln(err)
		}

		for _, f := range files {
			object, err := ec.GetObject(c, bucket /*fileType+"/"+*/, f.Url, minio.GetObjectOptions{})
			if err != nil {
				log.Fatalln(err)
			}

			stats, err := object.Stat()
			if err != nil {
				log.Fatalln(err)
			}

			_, err = lc.PutObject(c, bucket, f.Url, object, stats.Size, minio.PutObjectOptions{})
			if err != nil {
				log.Print("5")
				log.Fatalln(err)
			}

			object.Close()
		}
	}
}
