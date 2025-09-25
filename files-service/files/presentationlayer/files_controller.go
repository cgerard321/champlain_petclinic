package presentationlayer

import (
	"errors"
	"files-service/files/businesslayer"
	"files-service/files/util"
	"files-service/files/util/exception"
	"net/http"

	"github.com/gin-gonic/gin"
)

type FilesControllerImpl struct {
	s businesslayer.FilesService
}

func NewFilesLinkController(service businesslayer.FilesService) *FilesControllerImpl {
	return &FilesControllerImpl{
		s: service,
	}
}

func (i *FilesControllerImpl) getFile(c *gin.Context) {
	bucket, exists := c.Get("bucket")
	if !exists { //if you ever have this error, you simply did not use the UseBucket("bucketName") for the group calling this method, see the imagesGroup example in Routes
		util.HandleExceptions(c, errors.New("bucket not found in context"))
		return
	}

	id := c.Param("id")
	if len([]rune(id)) != 36 {
		util.HandleExceptions(c, exception.NewInvalidFileIdException(id))
		return
	}

	img, err := i.s.GetFile(bucket.(string), id)
	if err != nil {
		util.HandleExceptions(c, err)
		return
	}

	c.IndentedJSON(http.StatusOK, img)
}

func (i *FilesControllerImpl) Routes(engine *gin.Engine) error {
	imagesGroup := engine.Group("/images").Use(UseBucket("images"))

	imagesGroup.GET("/:id", i.getFile)

	//for anybody reading this in the future that wants to add handling for another type than image just make a new group and give it a new bucket
	//make sure to create the bucket in minio or the cloud if that is what you guys are using by then
	return nil
}
