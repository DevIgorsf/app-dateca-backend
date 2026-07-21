package com.dat.dateca.importacao.domain.ports;

import java.util.List;

public interface PdfTextExtractionPort {

    int countPages(byte[] pdfBytes);

    PageContent extractPage(byte[] pdfBytes, int pageNumber);

    byte[] rasterizePage(byte[] pdfBytes, int pageNumber, int dpi);

    List<ExtractedImage> extractEmbeddedImages(byte[] pdfBytes, int pageNumber);
}
