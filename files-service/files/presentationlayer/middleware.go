package presentationlayer

import "github.com/gin-gonic/gin"

func UseBucket(name string) gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Set("bucket", name)
		c.Next()
	}
}
