// Main.java
package com.mail.reader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Collections;

public class Main {
    private static final String HOST = "imap.gmail.com";
    private static final String USER = "ksh33528@gmail.com";
    private static final String PASS = "jzsz gqta fyup irfr";

    private static DefaultListModel<EmailMessage> listModel = new DefaultListModel<>();
    private static JTextArea bodyArea = new JTextArea();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Email Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        ImageIcon icon = new ImageIcon("src/main/resources/email_icon.png");
        frame.setIconImage(icon.getImage());

        JList<EmailMessage> emailList = new JList<>(listModel);
        JScrollPane listScroll = new JScrollPane(emailList);

        bodyArea.setEditable(false);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);

        DefaultListModel<String> attachmentListModel = new DefaultListModel<>();
        JList<String> attachmentList = new JList<>(attachmentListModel);
        attachmentList.setBorder(BorderFactory.createTitledBorder("Attachments"));
        attachmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attachmentList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selectedFile = attachmentList.getSelectedValue();
                    if (selectedFile != null) {
                        try {
                            Desktop.getDesktop().open(new File("attachments", selectedFile));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Failed to open file: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, bodyScroll);

        JButton exportButton = new JButton("Export to Excel");
        exportButton.addActionListener(e -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new java.io.File("emails.xlsx"));
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    ExcelExporter.exportToExcel(Collections.list(listModel.elements()), filePath);
                    JOptionPane.showMessageDialog(frame, "Exported to Excel successfully!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(exportButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        System.out.println("GUI initialized.");

        new Thread(() -> {
            try {
                Properties props = System.getProperties();
                props.put("mail.imaps.ssl.trust", "*");

                EmailClient client = new EmailClient(HOST, USER, PASS);
                List<EmailMessage> emails = client.fetchLastUnreadAndMarkOthersRead();

                System.out.println("Fetched " + emails.size() + " emails.");
                emails.forEach(email -> System.out.println("Email subject: " + email.getSubject()));

                SwingUtilities.invokeLater(() -> {
                    listModel.clear();

                    if (emails.isEmpty()) {
                        EmailMessage placeholder = new EmailMessage(
                                "No Unread Emails",
                                "🎉 You're all caught up! No unread emails found.",
                                "System",
                                "System",
                                new Date(),
                                List.of()
                        );
                        listModel.addElement(placeholder);
                        emailList.setSelectedIndex(0);
                    } else {
                        for (EmailMessage msg : emails) {
                            listModel.addElement(msg);
                        }
                        emailList.setSelectedIndex(0);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "Failed to fetch emails:\n" + e.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();

        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                EmailMessage selected = emailList.getSelectedValue();
                if (selected != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String info = "Name: " + selected.getPersonalName() + "\n"
                            + "From: " + selected.getFrom() + "\n"
                            + "Date: " + sdf.format(selected.getSentDate()) + "\n"
                            + "Subject: " + selected.getSubject() + "\n"
                            + "Attachments: " + String.join(", ", selected.getAttachments()) + "\n\n"
                            + selected.getBody();
                    bodyArea.setText(info);
                    attachmentListModel.clear();
                    selected.getAttachments().forEach(attachmentListModel::addElement);

                } else {
                    bodyArea.setText("");
                }
            }
        });

        frame.setVisible(true);
    }
}
