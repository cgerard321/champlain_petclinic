package mailer

type Mail struct {
	To            string `json:"EmailSendTo"      validate:"required,email"`
	Subject       string `json:"EmailTitle"       validate:"required"`
	TemplateName  string `json:"TemplateName"`
	Header        string `json:"Header"`
	Body          string `json:"Body"`
	Footer        string `json:"Footer"`
	Correspondent string `json:"CorrespondantName"`
	SenderName    string `json:"SenderName"`
}
