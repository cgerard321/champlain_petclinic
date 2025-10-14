package mailer

type Mail struct {
	To            string `json:"to"               validate:"required,email"`
	Subject       string `json:"subject"          validate:"required"`
	TemplateName  string `json:"template_name"`
	Header        string `json:"header"`
	Body          string `json:"body"`
	Footer        string `json:"footer"`
	Correspondent string `json:"correspondent"`
	SenderName    string `json:"sender_name"`
}
