package ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PDFWriter {

    public static boolean generateInvoice(
            File file,
            String invoiceNo,
            String date,
            String generatorName,
            List<Object[]> cartItems, // Object[] = {Product, quantity, totalRowPrice}
            double subtotal,
            double tax,
            double grandTotal) {
        
        try {
            // Build the PDF content stream
            StringBuilder content = new StringBuilder();
            
            // Header Details
            content.append("BT\n/F2 18 Tf\n50 780 Td\n(INVENTORY ERP SYSTEM) Tj\nET\n");
            content.append("BT\n/F1 10 Tf\n50 765 Td\n(INVOICE / BILL OF SALE) Tj\nET\n");
            
            // Invoice Metadata
            content.append("BT\n/F1 10 Tf\n400 780 Td\n(Invoice No: " + invoiceNo + ") Tj\nET\n");
            content.append("BT\n/F1 10 Tf\n400 765 Td\n(Date: " + date + ") Tj\nET\n");
            content.append("BT\n/F1 10 Tf\n400 750 Td\n(Biller: " + generatorName + ") Tj\nET\n");
            
            // Horizontal line
            content.append("1 w\n50 740 m\n545 740 l\nS\n");
            
            // Table Header
            content.append("BT\n/F2 10 Tf\n50 725 Td\n(Item Name) Tj\nET\n");
            content.append("BT\n/F2 10 Tf\n230 725 Td\n(Category) Tj\nET\n");
            content.append("BT\n/F2 10 Tf\n340 725 Td\n(Unit Price) Tj\nET\n");
            content.append("BT\n/F2 10 Tf\n430 725 Td\n(Qty) Tj\nET\n");
            content.append("BT\n/F2 10 Tf\n500 725 Td\n(Total) Tj\nET\n");
            
            // Table Divider line
            content.append("0.5 w\n50 715 m\n545 715 l\nS\n");
            
            int y = 700;
            for (Object[] item : cartItems) {
                model.Product p = (model.Product) item[0];
                int qty = (int) item[1];
                double total = (double) item[2];
                
                content.append("BT\n/F1 10 Tf\n50 " + y + " Td\n(" + escapePDFText(p.getName()) + ") Tj\nET\n");
                content.append("BT\n/F1 10 Tf\n230 " + y + " Td\n(" + escapePDFText(p.getCategory()) + ") Tj\nET\n");
                content.append("BT\n/F1 10 Tf\n340 " + y + " Td\n(Rs. " + String.format("%.2f", p.getPrice()) + ") Tj\nET\n");
                content.append("BT\n/F1 10 Tf\n430 " + y + " Td\n(" + qty + ") Tj\nET\n");
                content.append("BT\n/F1 10 Tf\n500 " + y + " Td\n(Rs. " + String.format("%.2f", total) + ") Tj\nET\n");
                
                y -= 18;
                if (y < 100) {
                    break; // Keep to 1 page for simple invoices
                }
            }
            
            // Divider line
            content.append("0.5 w\n50 " + (y + 5) + " m\n545 " + (y + 5) + " l\nS\n");
            
            // Totals
            y -= 15;
            content.append("BT\n/F1 10 Tf\n400 " + y + " Td\n(Subtotal: Rs. " + String.format("%.2f", subtotal) + ") Tj\nET\n");
            y -= 15;
            content.append("BT\n/F1 10 Tf\n400 " + y + " Td\n(Tax (18%): Rs. " + String.format("%.2f", tax) + ") Tj\nET\n");
            y -= 20;
            content.append("BT\n/F2 12 Tf\n400 " + y + " Td\n(Grand Total: Rs. " + String.format("%.2f", grandTotal) + ") Tj\nET\n");
            
            // Footer
            content.append("BT\n/F1 9 Tf\n50 80 Td\n(Terms & Conditions: Goods once sold will not be taken back.) Tj\nET\n");
            content.append("BT\n/F2 10 Tf\n50 60 Td\n(Thank you for shopping with us!) Tj\nET\n");

            // Convert string to bytes
            byte[] streamBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);
            
            // Assemble the PDF Objects
            List<byte[]> pdfObjects = new ArrayList<>();
            List<Integer> offsets = new ArrayList<>();
            
            // PDF Header
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
            
            // Helper to add object
            addObject(pdfObjects, "<< /Type /Catalog /Pages 2 0 R >>"); // Obj 1: Catalog
            addObject(pdfObjects, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>"); // Obj 2: Pages
            addObject(pdfObjects, "<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /MediaBox [0 0 595 842] /Contents 6 0 R >>"); // Obj 3: Page
            addObject(pdfObjects, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"); // Obj 4: Regular Font
            addObject(pdfObjects, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>"); // Obj 5: Bold Font
            
            // Obj 6: Content Stream
            String streamHeader = "<< /Length " + streamBytes.length + " >>\nstream\n";
            String streamFooter = "\nendstream";
            byte[] streamHeaderBytes = streamHeader.getBytes(StandardCharsets.ISO_8859_1);
            byte[] streamFooterBytes = streamFooter.getBytes(StandardCharsets.ISO_8859_1);
            
            byte[] obj6Bytes = new byte[streamHeaderBytes.length + streamBytes.length + streamFooterBytes.length];
            System.arraycopy(streamHeaderBytes, 0, obj6Bytes, 0, streamHeaderBytes.length);
            System.arraycopy(streamBytes, 0, obj6Bytes, streamHeaderBytes.length, streamBytes.length);
            System.arraycopy(streamFooterBytes, 0, obj6Bytes, streamHeaderBytes.length + streamBytes.length, streamFooterBytes.length);
            
            pdfObjects.add(obj6Bytes);
            
            // Write objects and track offsets
            int currentOffset = bos.size();
            for (int i = 0; i < pdfObjects.size(); i++) {
                offsets.add(currentOffset);
                String objLabel = (i + 1) + " 0 obj\n";
                bos.write(objLabel.getBytes(StandardCharsets.ISO_8859_1));
                bos.write(pdfObjects.get(i));
                bos.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
                currentOffset = bos.size();
            }
            
            // Write cross-reference table (xref)
            int xrefOffset = bos.size();
            bos.write("xref\n".getBytes(StandardCharsets.ISO_8859_1));
            bos.write(("0 " + (pdfObjects.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
            bos.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 0; i < offsets.size(); i++) {
                String offsetStr = String.format("%010d", offsets.get(i)) + " 00000 n \n";
                bos.write(offsetStr.getBytes(StandardCharsets.ISO_8859_1));
            }
            
            // Write trailer
            bos.write("trailer\n".getBytes(StandardCharsets.ISO_8859_1));
            bos.write(("<< /Size " + (pdfObjects.size() + 1) + " /Root 1 0 R >>\n").getBytes(StandardCharsets.ISO_8859_1));
            bos.write("startxref\n".getBytes(StandardCharsets.ISO_8859_1));
            bos.write((xrefOffset + "\n").getBytes(StandardCharsets.ISO_8859_1));
            bos.write("%%EOF\n".getBytes(StandardCharsets.ISO_8859_1));
            
            // Save to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bos.writeTo(fos);
            }
            System.out.println("Invoice PDF generated successfully: " + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to generate PDF Invoice!");
            e.printStackTrace();
            return false;
        }
    }
    
    private static void addObject(List<byte[]> list, String str) {
        list.add(str.getBytes(StandardCharsets.ISO_8859_1));
    }
    
    private static String escapePDFText(String text) {
        if (text == null) return "";
        return text.replace("(", "\\(").replace(")", "\\)");
    }
}
