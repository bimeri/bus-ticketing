package net.gowaka.gowaka.domain.service;

import org.springframework.stereotype.Service;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

@Service
public class HtmlToPdfGenarator {

	private static final String UTF_8 = "UTF-8";

	public File createPdf(String processedHtml, String filename) throws Exception {

		String xHtml = convertToXhtml(processedHtml);

		  FileOutputStream os = null;
		final File outputFile;
	        try {
	            outputFile = File.createTempFile(filename, ".pdf");
	            os = new FileOutputStream(outputFile);

	            ITextRenderer renderer = new ITextRenderer();
	            renderer.setDocumentFromString(xHtml);
	            renderer.layout();
				renderer.createPDF(os, false);
	            renderer.finishPDF();
	        }
	        finally {
	            if (os != null) {
	                try {
	                    os.close();
	                } catch (IOException e) { /*ignore*/ }
	            }
	        }
	        return outputFile;
	}

	private String convertToXhtml(String html) throws UnsupportedEncodingException {
		Tidy tidy = new Tidy();
		tidy.setInputEncoding(UTF_8);
		tidy.setOutputEncoding(UTF_8);
		tidy.setXHTML(true);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		tidy.parseDOM(inputStream, outputStream);
		return outputStream.toString(UTF_8);
	}

}
