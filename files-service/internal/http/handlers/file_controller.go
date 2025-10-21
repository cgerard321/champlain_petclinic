package handlers

import (
	"files-service/internal/domain"
	"files-service/internal/http/middleware"
	"files-service/internal/model"
	"files-service/internal/util/exception"
	"net/http"

	"github.com/gin-gonic/gin"
)

type FilesController struct {
	s domain.FileService
}

func NewFileController(service domain.FileService) *FilesController {
	return &FilesController{
		s: service,
	}
}

func (i *FilesController) getFile(c *gin.Context) {
	id := c.Param("id")
	if len([]rune(id)) != 36 {
		middleware.Cancel(c, exception.NewInvalidFileIdException(id))
		return
	}

	file, err := i.s.GetFile(id)
	if err != nil {
		middleware.Cancel(c, err)
		return
	}

	c.IndentedJSON(http.StatusOK, file)
}

func (i *FilesController) addFile(c *gin.Context) {
	model := &model.FileRequestModel{}
	err := c.ShouldBindJSON(model)
	if err != nil {
		middleware.Cancel(c, err)
		return
	}

	file, err := i.s.AddFile(model)
	if err != nil {
		middleware.Cancel(c, err)
		return
	}

	c.IndentedJSON(http.StatusCreated, file)
}

func (i *FilesController) updateFile(c *gin.Context) {
	id := c.Param("id")
	if len([]rune(id)) != 36 {
		middleware.Cancel(c, exception.NewInvalidFileIdException(id))
		return
	}

	model := &model.FileRequestModel{}
	err := c.ShouldBindJSON(model)
	if err != nil {
		middleware.Cancel(c, err)
		return
	}

	file, err := i.s.UpdateFile(id, model)
	if err != nil {
		middleware.Cancel(c, err)
		return
	}

	c.IndentedJSON(http.StatusOK, file)
}

func (i *FilesController) deleteFile(c *gin.Context) {
	id := c.Param("id")
	if len([]rune(id)) != 36 {
		middleware.Cancel(c, exception.NewInvalidFileIdException(id))
		return
	}

	err := i.s.DeleteFile(id)
	if err != nil {
		middleware.Cancel(c, err)
		return
	}

	c.Status(http.StatusNoContent)
}

func (i *FilesController) Routes(engine *gin.Engine) error {
	filesGroup := engine.Group("/files").Use(middleware.GlobalExceptionHandler)

	filesGroup.GET("/:id", i.getFile)
	filesGroup.POST("/", middleware.ValidateRequestBody, i.addFile)
	filesGroup.PUT("/:id", middleware.ValidateRequestBody, i.updateFile)
	filesGroup.DELETE("/:id", i.deleteFile)
	return nil
}
