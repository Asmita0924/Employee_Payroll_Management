package com.payroll.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.payroll.dto.SalarySlipDto;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

public class PdfGeneratorUtil {

    public static byte[] generateSalarySlipPdf(SalarySlipDto slip) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Set up Fonts
            Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(41, 128, 185)); // Sleek Blue
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(44, 62, 80)); // Charcoal
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(39, 174, 96)); // Green

            // Title
            Paragraph title = new Paragraph("EMPLOYEE PAYROLL SYSTEM", mainTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("PAYSLIP FOR MONTH: " + slip.getSalaryMonth(), sectionTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(25);
            document.add(subtitle);

            // Employee Details Table
            Paragraph section1 = new Paragraph("Employee Information", sectionTitleFont);
            section1.setSpacingAfter(8);
            document.add(section1);

            PdfPTable empTable = new PdfPTable(2);
            empTable.setWidthPercentage(100);
            empTable.setSpacingAfter(20);

            addTableCell(empTable, "Employee Name:", labelFont);
            addTableCell(empTable, slip.getEmployeeName(), valueFont);
            addTableCell(empTable, "Email Address:", labelFont);
            addTableCell(empTable, slip.getEmail(), valueFont);
            addTableCell(empTable, "Department:", labelFont);
            addTableCell(empTable, slip.getDepartmentName(), valueFont);
            addTableCell(empTable, "Designation:", labelFont);
            addTableCell(empTable, slip.getDesignation(), valueFont);
            addTableCell(empTable, "Joining Date:", labelFont);
            addTableCell(empTable, slip.getJoiningDate().toString(), valueFont);

            document.add(empTable);

            // Salary Breakdown Table
            Paragraph section2 = new Paragraph("Earnings & Deductions Summary", sectionTitleFont);
            section2.setSpacingAfter(8);
            document.add(section2);

            PdfPTable salTable = new PdfPTable(2);
            salTable.setWidthPercentage(100);
            salTable.setSpacingAfter(20);

            // Earnings
            addTableCell(salTable, "Basic Salary:", labelFont);
            addTableCell(salTable, "$" + slip.getBasicSalary().toString(), valueFont);
            addTableCell(salTable, "House Rent Allowance (HRA):", labelFont);
            addTableCell(salTable, "$" + slip.getHra().toString(), valueFont);
            addTableCell(salTable, "Dearness Allowance (DA):", labelFont);
            addTableCell(salTable, "$" + slip.getDa().toString(), valueFont);
            addTableCell(salTable, "Bonus Awarded:", labelFont);
            addTableCell(salTable, "$" + slip.getBonus().toString(), valueFont);

            // Deductions
            addTableCell(salTable, "Leave / Absence Deductions:", labelFont);
            addTableCell(salTable, "-$" + slip.getDeduction().toString(), valueFont);
            addTableCell(salTable, "Tax Deductions (12%):", labelFont);
            addTableCell(salTable, "-$" + slip.getTax().toString(), valueFont);

            // Total Net Salary
            addTableCell(salTable, "Net Salary Paid:", totalFont);
            addTableCell(salTable, "$" + slip.getNetSalary().toString(), totalFont);

            document.add(salTable);

            // Footer notes
            Paragraph footer = new Paragraph("\n* This is a computer-generated salary slip and does not require a physical signature.", valueFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private static void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderColor(new Color(220, 220, 220));
        table.addCell(cell);
    }
}
