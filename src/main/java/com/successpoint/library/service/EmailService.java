package com.successpoint.library.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.successpoint.library.entity.Student;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Async
    public void sendInvoiceEmail(Student student) {
        try {
            // 1. GENERATE PDF (Unchanged)
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, pdfStream);
            
            document.open();

            // --- COLOR PALETTE ---
            Color indigoBrand = new Color(79, 70, 229);
            Color inkBlueStamp = new Color(28, 58, 148); 
            Color lightGray = new Color(243, 244, 246);

            // --- FONTS ---
            Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, indigoBrand);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(55, 65, 81));
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font wishFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, indigoBrand);
            Font stampFontSmall = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, inkBlueStamp);
            Font stampFontBig = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, inkBlueStamp);

            // 1. HEADER
            Paragraph title = new Paragraph("SUCCESS POINT LIBRARY", mainTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("OFFICIAL FEE RECEIPT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.GRAY));
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(25f);
            document.add(sub);

            // 2. DATA TABLE
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f});

            addColoredRow(table, "STUDENT NAME", student.getName().toUpperCase(), labelFont, valueFont, lightGray);
            addColoredRow(table, "MOBILE NUMBER", student.getMobileNumber(), labelFont, valueFont, Color.WHITE);
            addColoredRow(table, "JOINING DATE", student.getJoiningDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), labelFont, valueFont, lightGray);
            addColoredRow(table, "VALID UNTIL", student.getDueDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), labelFont, valueFont, Color.WHITE);
            addColoredRow(table, "TOTAL PAID", "INR " + student.getFeesPaid() + ".00", labelFont, valueFont, lightGray);

            document.add(table);

            // 3. WISHES SECTION
            document.add(new Paragraph("\n\n"));
            Paragraph wishes = new Paragraph("Thank you for choosing Success Point Library.\nWe wish you a very bright future and success in all your endeavors!", wishFont);
            wishes.setAlignment(Element.ALIGN_CENTER);
            document.add(wishes);

            // 4. THE INK STAMP (Simulated Circular Stamp)
            document.add(new Paragraph("\n\n"));
            PdfPTable stampContainer = new PdfPTable(1);
            stampContainer.setWidthPercentage(30);
            stampContainer.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell stampCell = new PdfPCell();
            stampCell.setBorderColor(inkBlueStamp);
            stampCell.setBorderWidth(2.5f);
            stampCell.setPadding(10f);
            stampCell.setBackgroundColor(new Color(240, 244, 255)); 
            
            Paragraph s1 = new Paragraph("SUCCESS POINT", stampFontSmall);
            s1.setAlignment(Element.ALIGN_CENTER);
            stampCell.addElement(s1);

            Paragraph s2 = new Paragraph("OFFICE STAMP", stampFontBig);
            s2.setAlignment(Element.ALIGN_CENTER);
            stampCell.addElement(s2);

            Paragraph s3 = new Paragraph(student.getJoiningDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), stampFontSmall);
            s3.setAlignment(Element.ALIGN_CENTER);
            stampCell.addElement(s3);

            Paragraph s4 = new Paragraph("VERIFIED", stampFontBig);
            s4.setAlignment(Element.ALIGN_CENTER);
            stampCell.addElement(s4);

            stampContainer.addCell(stampCell);
            document.add(stampContainer);

            document.close();

            // ==========================================
            // 5. BREVO API LOGIC (Replaces JavaMailSender)
            // ==========================================
            
            // Convert PDF to Base64 String
            byte[] pdfBytes = pdfStream.toByteArray();
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
            String fileName = "Receipt_" + student.getName().replace(" ", "_") + ".pdf";

            String textContent = "Dear " + student.getName() + ",\n\n"
                    + "Thank you for being a part of Success Point Library. We are committed to providing you with the best environment for your studies.\n\n"
                    + "We wish you a very bright and successful future ahead!\n\n"
                    + "Best Regards,\nSuccess Point Administration";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Build the JSON Payload
            Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Success Point Library", "email", senderEmail),
                "to", List.of(Map.of("email", student.getEmail())),
                "subject", "Official Membership Receipt - Success Point Library",
                "textContent", textContent,
                "attachment", List.of(
                    Map.of(
                        "content", base64Pdf,
                        "name", fileName
                    )
                )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(BREVO_API_URL, request, String.class);
            System.out.println("Professional Stamp PDF sent successfully to " + student.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendPendingFeesEmail(Student student) {
        try {
            String subject = "Fee Status Pending - Success Point Library";
            String textContent = "Dear " + student.getName() + ",\n\n"
                    + "Your library membership is approved, but your fee status is currently PENDING.\n"
                    + "Submitted plan: " + student.getFeesPeriodMonths() + " month(s)\n"
                    + "Submitted fee: INR " + student.getFeesPaid() + "\n\n"
                    + "Please clear your fees and contact the admin office to complete payment verification.\n\n"
                    + "Best Regards,\nSuccess Point Administration";

            sendSimpleEmail(student.getEmail(), subject, textContent);
            System.out.println("Pending fee email sent successfully to " + student.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send pending fee email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendSimpleEmail(String toEmail, String subject, String textContent) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Success Point Library", "email", senderEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "textContent", textContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(BREVO_API_URL, request, String.class);
    }

    private void addColoredRow(PdfPTable table, String label, String value, Font lFont, Font vFont, Color bg) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, lFont));
        c1.setBackgroundColor(bg);
        c1.setPadding(10f);
        c1.setBorderColor(new Color(229, 231, 235));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, vFont));
        c2.setBackgroundColor(bg);
        c2.setPadding(10f);
        c2.setBorderColor(new Color(229, 231, 235));
        table.addCell(c2);
    }
}