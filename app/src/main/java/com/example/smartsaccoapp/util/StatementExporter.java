package com.example.smartsaccoapp.util;

import android.content.Context;
import android.widget.Toast;

import com.example.smartsaccoapp.data.db.Transaction;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatementExporter {

    public static void exportTransactionsToPdf(Context context, List<Transaction> transactions) {
        String fileName = "Sacco_Statement_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(null), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            document.add(new Paragraph("SMART SACCO SOCIETY")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            document.add(new Paragraph("Member Transaction Statement")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14));
            document.add(new Paragraph("Generated on: " + new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10));

            document.add(new Paragraph("\n"));

            // Table
            float[] columnWidths = {2, 3, 2, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            double total = 0;

            for (Transaction t : transactions) {
                table.addCell(new Cell().add(new Paragraph(sdf.format(new Date(t.timestamp)))));
                table.addCell(new Cell().add(new Paragraph(t.type)));
                table.addCell(new Cell().add(new Paragraph("UGX " + String.format("%,.0f", t.amount))));
                table.addCell(new Cell().add(new Paragraph("COMPLETED"))); // Simplified
                total += t.amount;
            }

            document.add(table);

            document.add(new Paragraph("\nTotal Amount: UGX " + String.format("%,.0f", total))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold());

            document.add(new Paragraph("\n\nThank you for saving with Smart Sacco.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setFontSize(10));

            document.close();
            Toast.makeText(context, "Statement saved: " + fileName, Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
