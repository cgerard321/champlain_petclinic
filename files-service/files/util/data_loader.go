package util

import (
	"context"
	"database/sql"
	"files-service/files/datalayer"
	"time"
)

func SetupDatabase(db *sql.DB) {
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

	//adds the data to the table
	sampleData := []datalayer.FileLink{
		{"3e5a214b-009d-4a25-9313-344676e6157d", "54746305265_48e2332383_c.jpg"},
	}

	for _, img := range sampleData {
		_, err := db.ExecContext(ctx, `INSERT INTO files (fileId, url) VALUES (?, ?)`, img.FileId, img.Url)
		if err != nil {
			panic(err)
		}
	}
}
