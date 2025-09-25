package clientlayer

import (
	"errors"
	"files-service/files/models"
	"files-service/files/util/exception"
	"io"
	"net/http"
	"path"
	"strings"
)

type MinioServiceClient struct {
	baseUrl string
}

func NewMinioServiceClient(url string) *MinioServiceClient {
	return &MinioServiceClient{
		baseUrl: url,
	}
}

func (msc *MinioServiceClient) GetFile(FileUrl string) (*models.FileResponseModel, error) {
	url := msc.baseUrl + FileUrl
	resp, err := http.Get(url)

	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		if resp.StatusCode == http.StatusNotFound { //only possible if the file is deleted in minio but not in the file database
			return nil, exception.NewNotFoundException("file not found at url: " + url)
		} else { //something being wrong with the url should be the only reason how we get here
			return nil, errors.New("something is wrong with the url: " + url)
		}
	}

	defer resp.Body.Close()

	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	//get file name from url
	fileNameWithExt := path.Base(FileUrl)
	fileName := strings.Replace(strings.TrimSuffix(fileNameWithExt, path.Ext(fileNameWithExt)), "_", " ", -1) //removes extension from name and replaces "_" with " "

	file := models.FileResponseModel{
		FileId:   "",
		FileName: fileName,
		FileType: resp.Header.Get("Content-Type"),
		FileData: data,
	}

	return &file, nil
}
