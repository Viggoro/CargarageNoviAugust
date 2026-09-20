package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.model.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb ALTERNATE_ROW_COLOR = new DeviceRgb(236, 240, 241);
    
    public byte[] generateReceiptPdf(Receipt receipt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            addHeader(document, receipt);

            addBusinessInfo(document);

            addCustomerInfo(document, receipt);

            if (receipt.getRepair() != null) {
                addRepairDetails(document, receipt.getRepair());
            } else if (receipt.getInspection() != null) {
                addInspectionDetails(document, receipt.getInspection());
            }

            addTotals(document, receipt);

            addPaymentInfo(document, receipt);

            addFooter(document);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new BadRequestException("Failed to generate PDF: " + e.getMessage());
        }
    }
    
    private void addHeader(Document document, Receipt receipt) {
        Paragraph header = new Paragraph("RECEIPT")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(HEADER_COLOR)
                .setMarginBottom(10);
        document.add(header);
        
        Paragraph receiptNumber = new Paragraph("Receipt #: " + receipt.getId())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(receiptNumber);
    }
    
    private void addBusinessInfo(Document document) {
        Paragraph businessName = new Paragraph("Car Garage Service Center")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(5);
        document.add(businessName);
        
        Paragraph businessDetails = new Paragraph(
                "Address: Main Street 123, City\n" +
                "Phone: +31 20 123 4567\n" +
                "Email: info@cargarage.nl\n" +
                "VAT: NL123456789B01"
        ).setFontSize(10)
         .setMarginBottom(20);
        document.add(businessDetails);
    }
    
    private void addCustomerInfo(Document document, Receipt receipt) {
        Car car;
        if (receipt.getRepair() != null) {
            car = receipt.getRepair().getCar();
        } else if (receipt.getInspection() != null) {
            car = receipt.getInspection().getCar();
        } else {
            return;
        }
        
        Customer customer = car.getOwner();
        
        Paragraph customerTitle = new Paragraph("Customer Information")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(5);
        document.add(customerTitle);
        
        String customerInfo = String.format(
                "Name: %s %s\n" +
                "Phone: %s\n" +
                "Email: %s\n" +
                "Address: %s",
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "N/A",
                customer.getEmail() != null ? customer.getEmail() : "N/A",
                customer.getAddress() != null ? customer.getAddress() : "N/A"
        );
        
        Paragraph customerDetails = new Paragraph(customerInfo)
                .setFontSize(10)
                .setMarginBottom(10);
        document.add(customerDetails);
        
        Paragraph carTitle = new Paragraph("Vehicle Information")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(5);
        document.add(carTitle);
        
        String carInfo = String.format(
                "License Plate: %s\n" +
                "Brand: %s\n" +
                "Model: %s\n" +
                "Year: %s\n" +
                "VIN: %s",
                car.getLicensePlate(),
                car.getBrand(),
                car.getModel(),
                car.getYear() != null ? car.getYear() : "N/A",
                car.getVin() != null ? car.getVin() : "N/A"
        );
        
        Paragraph carDetails = new Paragraph(carInfo)
                .setFontSize(10)
                .setMarginBottom(20);
        document.add(carDetails);
        
        Paragraph dateInfo = new Paragraph("Date: " + receipt.getCreatedAt().format(DATE_FORMATTER))
                .setFontSize(10)
                .setMarginBottom(20);
        document.add(dateInfo);
    }
    
    private void addRepairDetails(Document document, Repair repair) {
        Paragraph title = new Paragraph("Repair Services")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(title);

        float[] columnWidths = {4, 1, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addTableHeader(table, "Description", "Qty", "Amount (€)");
        
        boolean alternateRow = false;

        if (repair.getActions() != null) {
            for (RepairAction action : repair.getActions()) {
                addTableRow(table, action.getName(), "1", 
                        String.format("%.2f", action.getPrice()), alternateRow);
                alternateRow = !alternateRow;
            }
        }

        if (repair.getCustomActions() != null) {
            for (CustomRepairAction customAction : repair.getCustomActions()) {
                addTableRow(table, customAction.getDescription(), "1", 
                        String.format("%.2f", customAction.getPrice()), alternateRow);
                alternateRow = !alternateRow;
            }
        }

        if (repair.getParts() != null) {
            for (RepairPart repairPart : repair.getParts()) {
                String description = repairPart.getPart().getName() + " (Part)";
                addTableRow(table, description, 
                        repairPart.getQuantity().toString(),
                        String.format("%.2f", repairPart.getSubtotal()), alternateRow);
                alternateRow = !alternateRow;
            }
        }
        
        document.add(table);
    }
    
    private void addInspectionDetails(Document document, Inspection inspection) {
        Paragraph title = new Paragraph("Inspection Service")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(title);

        float[] columnWidths = {5, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        table.addHeaderCell(createHeaderCell("Description"));
        table.addHeaderCell(createHeaderCell("Amount (€)"));

        table.addCell(new Cell().add(new Paragraph("Vehicle Inspection"))
                .setPadding(8));

        Receipt receipt = inspection.getReceipt();
        String amount = receipt != null ? String.format("%.2f", receipt.getTotal()) : "0.00";
        
        table.addCell(new Cell().add(new Paragraph(amount))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8));
        
        document.add(table);

        if (inspection.getFindings() != null && !inspection.getFindings().isEmpty()) {
            Paragraph findingsTitle = new Paragraph("Findings:")
                    .setFontSize(11)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(5);
            document.add(findingsTitle);
            
            Paragraph findings = new Paragraph(inspection.getFindings())
                    .setFontSize(10)
                    .setMarginBottom(10);
            document.add(findings);
        }
        
        if (inspection.getDefects() != null && !inspection.getDefects().isEmpty()) {
            Paragraph defectsTitle = new Paragraph("Defects Found:")
                    .setFontSize(11)
                    .setBold()
                    .setMarginBottom(5);
            document.add(defectsTitle);
            
            for (String defect : inspection.getDefects()) {
                Paragraph defectItem = new Paragraph("• " + defect)
                        .setFontSize(10)
                        .setMarginLeft(10);
                document.add(defectItem);
            }
        }
        
        if (inspection.getApproved() != null) {
            String approvalStatus = inspection.getApproved() ? "APPROVED" : "NOT APPROVED";
            Paragraph approval = new Paragraph("Status: " + approvalStatus)
                    .setFontSize(11)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(20)
                    .setFontColor(inspection.getApproved() ? ColorConstants.GREEN : ColorConstants.RED);
            document.add(approval);
        }
    }
    
    private void addTotals(Document document, Receipt receipt) {
        float[] columnWidths = {3, 1};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                .setMarginBottom(20);

        table.addCell(new Cell().add(new Paragraph("Subtotal:"))
                .setBorder(Border.NO_BORDER)
                .setPadding(5));
        table.addCell(new Cell().add(new Paragraph(String.format("€ %.2f", receipt.getTotal())))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPadding(5));

        table.addCell(new Cell().add(new Paragraph("VAT (21%):"))
                .setBorder(Border.NO_BORDER)
                .setPadding(5));
        table.addCell(new Cell().add(new Paragraph(String.format("€ %.2f", receipt.getVat())))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPadding(5));

        table.addCell(new Cell().add(new Paragraph("Total:"))
                .setBold()
                .setBorder(Border.NO_BORDER)
                .setPadding(5)
                .setFontSize(12));
        table.addCell(new Cell().add(new Paragraph(String.format("€ %.2f", 
                receipt.getTotal() + receipt.getVat())))
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPadding(5)
                .setFontSize(12));
        
        document.add(table);
    }
    
    private void addPaymentInfo(Document document, Receipt receipt) {
        if (receipt.getPayment() != null) {
            Payment payment = receipt.getPayment();
            
            Paragraph paymentTitle = new Paragraph("Payment Information")
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(5);
            document.add(paymentTitle);
            
            String statusText = "Status: " + payment.getStatus().toString();
            Paragraph status = new Paragraph(statusText)
                    .setFontSize(10)
                    .setFontColor(payment.getStatus().toString().equals("PAID") ? 
                            ColorConstants.GREEN : ColorConstants.ORANGE);
            document.add(status);
            
            if (payment.getMethod() != null) {
                Paragraph method = new Paragraph("Payment Method: " + payment.getMethod())
                        .setFontSize(10);
                document.add(method);
            }
            
            if (payment.getPaidAt() != null) {
                Paragraph paidAt = new Paragraph("Paid At: " + 
                        payment.getPaidAt().format(DATE_FORMATTER))
                        .setFontSize(10)
                        .setMarginBottom(20);
                document.add(paidAt);
            } else {
                document.add(new Paragraph().setMarginBottom(20));
            }
        }
    }
    
    private void addFooter(Document document) {
        Paragraph footer = new Paragraph(
                "\nThank you for your business!\n" +
                "For questions regarding this receipt, please contact us.\n" +
                "All prices include applicable taxes."
        ).setFontSize(9)
         .setTextAlignment(TextAlignment.CENTER)
         .setMarginTop(20)
         .setItalic();
        document.add(footer);
    }
    
    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(8);
    }
    
    private void addTableHeader(Table table, String... headers) {
        for (String header : headers) {
            table.addHeaderCell(createHeaderCell(header));
        }
    }
    
    private void addTableRow(Table table, String description, String quantity, 
                            String amount, boolean alternate) {
        DeviceRgb bgColor = alternate ? ALTERNATE_ROW_COLOR : new DeviceRgb(255, 255, 255);
        
        table.addCell(new Cell().add(new Paragraph(description))
                .setBackgroundColor(bgColor)
                .setPadding(8));
        table.addCell(new Cell().add(new Paragraph(quantity))
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8));
        table.addCell(new Cell().add(new Paragraph(amount))
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8));
    }
}
