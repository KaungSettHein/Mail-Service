package com.mail.reader;

import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.FlagTerm;

import java.io.File;
import java.util.*;

public class EmailClient {
    private String host;
    private String user;
    private String pass;

    public EmailClient(String host, String user, String pass) {
        this.host = host;
        this.user = user;
        this.pass = pass;
    }

    public List<EmailMessage> fetchLastUnreadAndMarkOthersRead() throws Exception {
        List<EmailMessage> emails = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.trust", "*");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, user, pass);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Flags seen = new Flags(Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message[] messages = inbox.search(unseenFlagTerm);

        Arrays.sort(messages, Comparator.comparing(m -> {
            try {
                return m.getReceivedDate();
            } catch (MessagingException e) {
                return new Date(0);
            }
        }));

        for (Message message : messages) {
            emails.add(buildEmailMessage(message));
        }

        for (int i = 0; i < messages.length - 1; i++) {
            messages[i].setFlag(Flag.SEEN, true);
        }

        inbox.close(true);
        store.close();

        return emails;
    }

    private EmailMessage buildEmailMessage(Message message) throws Exception {
        String subject = message.getSubject();
        Address address = message.getFrom()[0];
        String email = ((InternetAddress) address).getAddress();
        String personal = ((InternetAddress) address).getPersonal();
        Date sentDate = message.getSentDate();

        List<String> attachments = new ArrayList<>();
        String body = extractContentAndAttachments(message, attachments);

        return new EmailMessage(subject, body, email, personal != null ? personal : "", sentDate, attachments);
    }

    private String extractContentAndAttachments(Part p, List<String> attachments) throws Exception {
        if (p.isMimeType("text/plain")) {
            return p.getContent().toString();
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String disposition = bp.getDisposition();
                if (disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT))) {
                    String fileName = bp.getFileName();
                    attachments.add(fileName);
                    File dir = new File("attachments");
                    if (!dir.exists()) dir.mkdirs();
                    if (bp instanceof MimeBodyPart mimeBodyPart) {
                        mimeBodyPart.saveFile(new File(dir, fileName));
                    }

                } else if (bp.isMimeType("text/plain")) {
                    body.append(bp.getContent().toString());
                } else {
                    body.append(extractContentAndAttachments(bp, attachments));
                }
            }
            return body.toString();
        }
        return "";
    }
}