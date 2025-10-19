package model

type FileResponseModel struct {
	FileId   string `json:"fileId"`
	FileName string `json:"fileName"`
	FileType string `json:"fileType"`
	FileData []byte `json:"fileData"`
}
