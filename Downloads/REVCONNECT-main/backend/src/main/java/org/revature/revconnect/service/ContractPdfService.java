package org.revature.revconnect.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.revature.revconnect.exception.ResourceNotFoundException;
import org.revature.revconnect.model.Collaboration;
import org.revature.revconnect.model.CollaborationContract;
import org.revature.revconnect.repository.CollaborationContractRepository;
import org.revature.revconnect.repository.CollaborationRepository;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractPdfService {

    private final CollaborationRepository collaborationRepo;
    private final CollaborationContractRepository contractRepo;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(30, 41, 59));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(100, 116, 139));
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(99, 102, 241));
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(71, 85, 105));
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(30, 41, 59));
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(51, 65, 85));
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(148, 163, 184));
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public byte[] generateContractPdf(Long collaborationId, Long userId) {
        Collaboration collab = collaborationRepo.findById(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));

        if (!collab.getBusiness().getId().equals(userId) && !collab.getCreator().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this collaboration");
        }

        CollaborationContract c = contractRepo.findTopByCollaborationIdOrderByCreatedAtDesc(collaborationId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Title
            Paragraph title = new Paragraph("COLLABORATION AGREEMENT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("RevConnect Paid Partnership Contract", SUBTITLE_FONT);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(5);
            doc.add(sub);

            String signedText = c.getAcceptedAt() != null
                    ? "Signed on " + c.getAcceptedAt().format(FMT)
                    : "Pending Signature";
            Paragraph cid = new Paragraph("Contract #RC-" + c.getId() + "  |  " + signedText, SMALL_FONT);
            cid.setAlignment(Element.ALIGN_CENTER);
            cid.setSpacingAfter(25);
            doc.add(cid);

            addLine(doc);

            // 1. Parties
            addSection(doc, "1. PARTIES");
            PdfPTable parties = new PdfPTable(2);
            parties.setWidthPercentage(100);
            parties.setSpacingAfter(15);
            parties.addCell(partyCell("BUSINESS", collab.getBusiness().getName(),
                    "@" + collab.getBusiness().getUsername(), c.getBrandName()));
            parties.addCell(partyCell("CREATOR", collab.getCreator().getName(),
                    "@" + collab.getCreator().getUsername(), null));
            doc.add(parties);

            // 2. Partnership Details
            addSection(doc, "2. PARTNERSHIP DETAILS");
            if (c.getBrandName() != null) addRow(doc, "Brand Name", c.getBrandName());
            if (c.getBrandWebsite() != null) addRow(doc, "Brand Website", c.getBrandWebsite());
            addRow(doc, "Duration", c.getDurationDays() + " days");
            if (c.getStartDate() != null) {
                addRow(doc, "Start Date", c.getStartDate().format(FMT));
                addRow(doc, "End Date", c.getEndDate().format(FMT));
            }
            addRow(doc, "Status", c.getAcceptedByCreator() ? "ACTIVE (Signed)" : "PENDING");
            doc.add(Chunk.NEWLINE);

            // 3. Payment Terms
            addSection(doc, "3. PAYMENT TERMS");
            PdfPTable payTable = new PdfPTable(2);
            payTable.setWidthPercentage(100);
            payTable.setSpacingAfter(10);
            payTable.setWidths(new float[]{1, 1});

            addTableRow(payTable, "Fixed Fee", formatCurrency(c.getFixedFee()));
            addTableRow(payTable, "Rate Per View", formatCurrency(c.getRatePerView())
                    + " per " + c.getViewMilestone() + " views");
            addTableRow(payTable, "Effective Rate", formatCurrency(
                    c.getViewMilestone() != null && c.getViewMilestone() > 0
                            ? c.getRatePerView() / c.getViewMilestone()
                            : 0.0) + " per view");
            addTableRow(payTable, "Payment Schedule", c.getPaymentSchedule());
            doc.add(payTable);

            Paragraph payNote = new Paragraph(
                    "Payment will be calculated based on verified post views. The business agrees to pay the creator " +
                    "the fixed fee upon contract activation, plus the per-view rate based on actual post impressions. " +
                    "Payments will be processed " + (c.getPaymentSchedule() != null ? c.getPaymentSchedule().toLowerCase() : "monthly") +
                    " via RevConnect Wallet.", BODY_FONT);
            payNote.setSpacingAfter(15);
            doc.add(payNote);

            // 4. Promotion Rules
            addSection(doc, "4. PROMOTION RULES");
            Paragraph rules = new Paragraph(c.getPromotionRules() != null ? c.getPromotionRules() : "Standard promotion rules apply.", BODY_FONT);
            rules.setSpacingAfter(15);
            doc.add(rules);

            // 5. Content Guidelines
            if (c.getContentGuidelines() != null && !c.getContentGuidelines().isBlank()) {
                addSection(doc, "5. CONTENT GUIDELINES");
                Paragraph guidelines = new Paragraph(c.getContentGuidelines(), BODY_FONT);
                guidelines.setSpacingAfter(15);
                doc.add(guidelines);
            }

            // 6. Terms & Conditions
            addSection(doc, "6. TERMS & CONDITIONS");
            String[] terms = {
                    "1. The Creator agrees to disclose the paid partnership on all promoted content by displaying the \"Paid Partnership with " + (c.getBrandName() != null ? c.getBrandName() : collab.getBusiness().getName()) + "\" label.",
                    "2. The Creator shall not promote competing brands during the contract duration without prior written consent from the Business.",
                    "3. All promoted content must comply with RevConnect's community guidelines and applicable advertising regulations.",
                    "4. The Business reserves the right to request removal or modification of content that does not meet the agreed content guidelines.",
                    "5. Either party may terminate this agreement with 7 days written notice. Outstanding payments for completed work will still be honored.",
                    "6. View counts are calculated using RevConnect's analytics system. Both parties agree to accept RevConnect's reported metrics.",
                    "7. The Creator retains full ownership of their original content. The Business is granted a license to reshare promoted posts for the contract duration.",
                    "8. Payment disputes must be raised within 14 days of the payment cycle. RevConnect will mediate disputes as a neutral third party.",
                    "9. This agreement is governed by applicable local laws. Any unresolved disputes will be handled through RevConnect's dispute resolution process.",
                    "10. Both parties confirm they have read, understood, and agreed to all terms outlined in this contract."
            };
            for (String term : terms) {
                Paragraph tp = new Paragraph(term, BODY_FONT);
                tp.setSpacingAfter(5);
                doc.add(tp);
            }
            doc.add(Chunk.NEWLINE);

            // 7. Signatures
            addLine(doc);
            addSection(doc, "7. DIGITAL SIGNATURES");

            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setSpacingAfter(10);

            PdfPCell bizSig = new PdfPCell();
            bizSig.setBorder(0);
            bizSig.setPadding(10);
            bizSig.addElement(new Paragraph("Business Representative", LABEL_FONT));
            bizSig.addElement(new Paragraph(collab.getBusiness().getName(), VALUE_FONT));
            bizSig.addElement(new Paragraph("Signed digitally on " +
                    (c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : "N/A"), SMALL_FONT));

            PdfPCell creatorSig = new PdfPCell();
            creatorSig.setBorder(0);
            creatorSig.setPadding(10);
            creatorSig.addElement(new Paragraph("Creator", LABEL_FONT));
            if (c.getAcceptedByCreator()) {
                creatorSig.addElement(new Paragraph(collab.getCreator().getName(), VALUE_FONT));
                creatorSig.addElement(new Paragraph("Signed digitally on " +
                        (c.getAcceptedAt() != null ? c.getAcceptedAt().format(FMT) : "N/A"), SMALL_FONT));
            } else {
                creatorSig.addElement(new Paragraph("[ Awaiting Signature ]",
                        new Font(Font.HELVETICA, 10, Font.ITALIC, new Color(234, 179, 8))));
            }

            sigTable.addCell(bizSig);
            sigTable.addCell(creatorSig);
            doc.add(sigTable);

            // Footer
            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph(
                    "This document was generated by RevConnect. It constitutes a legally binding digital agreement between the parties listed above.",
                    SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating contract PDF for collaboration {}", collaborationId, e);
            throw new RuntimeException("Failed to generate contract PDF", e);
        }
    }

    private void addSection(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, SECTION_FONT);
        p.setSpacingBefore(10);
        p.setSpacingAfter(8);
        doc.add(p);
    }

    private void addRow(Document doc, String label, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", LABEL_FONT));
        p.add(new Chunk(value, VALUE_FONT));
        p.setSpacingAfter(3);
        doc.add(p);
    }

    private void addLine(Document doc) throws DocumentException {
        Paragraph line = new Paragraph("________________________________________________________________________________",
                new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(226, 232, 240)));
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingAfter(10);
        doc.add(line);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell lc = new PdfPCell(new Phrase(label, LABEL_FONT));
        lc.setBorder(0);
        lc.setPadding(6);
        lc.setBackgroundColor(new Color(248, 250, 252));

        PdfPCell vc = new PdfPCell(new Phrase(value, VALUE_FONT));
        vc.setBorder(0);
        vc.setPadding(6);
        vc.setBackgroundColor(new Color(248, 250, 252));

        table.addCell(lc);
        table.addCell(vc);
    }

    private PdfPCell partyCell(String role, String name, String username, String brandName) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(10);
        cell.setBackgroundColor(new Color(248, 250, 252));

        cell.addElement(new Paragraph(role, new Font(Font.HELVETICA, 9, Font.BOLD, new Color(99, 102, 241))));
        cell.addElement(new Paragraph(name, new Font(Font.HELVETICA, 12, Font.BOLD, new Color(30, 41, 59))));
        cell.addElement(new Paragraph(username, SMALL_FONT));
        if (brandName != null && !brandName.isBlank()) {
            cell.addElement(new Paragraph("Brand: " + brandName, LABEL_FONT));
        }
        return cell;
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "\u20B90.00";
        return String.format("\u20B9%.2f", amount);
    }
}
