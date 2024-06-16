package F1Stats.app;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExporter {

    public void exportChartToPDF(JFreeChart chart, String filePath) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
            EncoderUtil.writeBufferedImage(chart.createBufferedImage(500, 300), ImageFormat.PNG, chartOutputStream);
            Image chartImage = Image.getInstance(chartOutputStream.toByteArray());
            document.add(chartImage);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
