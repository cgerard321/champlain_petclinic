package main

import "github.com/gin-gonic/gin"

func main() {

	engine := gin.Default()
	defer engine.Run()
}
