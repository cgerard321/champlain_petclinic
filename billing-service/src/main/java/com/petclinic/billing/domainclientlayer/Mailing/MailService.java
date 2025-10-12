package com.petclinic.billing.domainclientlayer.Mailing;

import com.petclinic.billing.domainclientlayer.Mailing.Mail;

public interface MailService {

    String sendMail(Mail mail);
}
