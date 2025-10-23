package exception

type InvalidFileIdException struct {
	message string
}

func NewInvalidFileIdException(id string) *InvalidFileIdException {
	return &InvalidFileIdException{message: "Invalid fileId: " + id}
}

func (e *InvalidFileIdException) Error() string {
	return e.message
}
