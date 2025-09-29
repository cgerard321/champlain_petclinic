package presentationlayer

import (
	"files-service/files/businesslayer"
	"files-service/files/util"
	"files-service/files/util/exception"
	"net/http"

	"github.com/gin-gonic/gin"
)

type FilesController struct {
	s businesslayer.FileService
}

func NewFileController(service businesslayer.FileService) *FilesController {
	return &FilesController{
		s: service,
	}
}

func (i *FilesController) getFile(c *gin.Context) {
	id := c.Param("id")
	if len([]rune(id)) != 36 {
		util.HandleExceptions(c, exception.NewInvalidFileIdException(id))
		return
	}

	file, err := i.s.GetFile(id)
	if err != nil {
		util.HandleExceptions(c, err)
		return
	}

	c.IndentedJSON(http.StatusOK, file)
}

func (i *FilesController) Routes(engine *gin.Engine) error { //TODO a way to make sure only image can be saved to the image bucket should be implemented
	filesGroup := engine.Group("/files")

	filesGroup.GET("/:id", i.getFile)

	return nil
}
