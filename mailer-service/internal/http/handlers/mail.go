package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"

	mailsvc "mailer-service/internal/mailer"
	pkg "mailer-service/pkg/mailer"
)

type MailHandler struct {
	s mailsvc.Service
}

func NewMailHandler(s mailsvc.Service) *MailHandler {
	return &MailHandler{s: s}
}

func (h *MailHandler) Register(r *gin.Engine) {
	r.Group("/mail")
}

func (h *MailHandler) Post(c *gin.Context) {
	v, ok := c.Get("mail")

	if !ok || v == nil {
		c.JSON(http.StatusBadRequest, "mail not found")
		return
	}

	m := v.(*pkg.Mail)

	h.s.Send(m)

	c.IndentedJSON(http.StatusOK, "Message queued")
}
