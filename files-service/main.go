package main

import (
	"database/sql"
	"files-service/files/clientlayer"
	"files-service/files/datalayer"
	"files-service/files/util"
	"fmt"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"

	"files-service/files/businesslayer"
	"files-service/files/presentationlayer"
	"os"

	_ "github.com/go-sql-driver/mysql"
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

	engine.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	ir := datalayer.NewFileLinkRepo(db)

	mc := clientlayer.NewMinioServiceClient("http://minio:9100/")
	is := businesslayer.NewFileLinkService(ir, mc)

	ic := presentationlayer.NewFilesLinkController(is)

	err2 := ic.Routes(engine)
	if err2 != nil {
		fmt.Println("Unable to route, exiting")
		os.Exit(1)
	}
}
