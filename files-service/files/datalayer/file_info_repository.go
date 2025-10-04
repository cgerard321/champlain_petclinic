package datalayer

type FileInfoRepository interface {
	GetFileInfo(fileId string) *FileInfo
	AddFileInfo(fileInfo *FileInfo) error
	DeleteFileInfo(fileId string) error
	ExistsById(fileId string) (bool, error)
}
