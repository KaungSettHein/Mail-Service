package com.mail.reader;

import java.util.Date;
import java.util.List;

public class EmailMessage {
    private final String subject;
    private final String body;
    private final String from;
    private final String personalName;
    private final Date sentDate;
    private final List<String> attachments;

    public EmailMessage(String subject, String body, String from, String personalName, Date sentDate, List<String> attachments) {
        this.subject = subject;
        this.body = body;
        this.from = from;
        this.personalName = personalName;
        this.sentDate = sentDate;
        this.attachments = attachments;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getFrom() {
        return from;
    }

    public String getPersonalName() {
        return personalName;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return from;
    }

}
