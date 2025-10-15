package presentationlayer

import (
	"files-service/files/businesslayer"
	"files-service/files/models"
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
		cancel(c, exception.NewInvalidFileIdException(id))
		return
	}

	file, err := i.s.GetFile(id)
	if err != nil {
		cancel(c, err)
		return
	}

	c.IndentedJSON(http.StatusOK, file)
}

func (i *FilesController) addFile(c *gin.Context) {
	model := &models.FileRequestModel{}
	err := c.ShouldBindJSON(model)
	if err != nil {
		cancel(c, err)
		return
	}

	file, err := i.s.AddFile(model)
	if err != nil {
		cancel(c, err)
		return
	}

	c.IndentedJSON(http.StatusCreated, file)
}

func (i *FilesController) updateFile(c *gin.Context) {
	id := c.Param("id")
	if len([]rune(id)) != 36 {
		cancel(c, exception.NewInvalidFileIdException(id))
		return
	}

	model := &models.FileRequestModel{}
	err := c.ShouldBindJSON(model)
	if err != nil {
		cancel(c, err)
		return
	}

	file, err := i.s.UpdateFile(id, model)
	if err != nil {
		cancel(c, err)
		return
	}

	c.IndentedJSON(http.StatusOK, file)
}

func (i *FilesController) deleteFileByFileId(c *gin.Context) {
	id := c.Param("id")
	if len([]rune(id)) != 36 {
		cancel(c, exception.NewInvalidFileIdException(id))
		return
	}

	err := i.s.DeleteFileByFileId(id)
	if err != nil {
		cancel(c, err)
		return
	}

	c.Status(http.StatusNoContent)
}

func (i *FilesController) Routes(engine *gin.Engine) error {
	filesGroup := engine.Group("/files").Use(GlobalExceptionHandler)

	filesGroup.GET("/:id", i.getFile)
	filesGroup.POST("/", ValidateRequestBody, i.addFile)
	filesGroup.PUT("/:id", ValidateRequestBody, i.updateFile)
	filesGroup.DELETE("/:id", i.deleteFileByFileId)
	return nil
}
