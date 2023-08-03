package kg.gazprom.signer.facades;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class PDFFacade {
    public static Bitmap extractPdfPageAsBitmap(String pathToPdfFile, int pageNum) throws IOException {
        Log.d("CUSTOM", "pathToPdfFile: " + pathToPdfFile);
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(
                new File(pathToPdfFile),
                ParcelFileDescriptor.MODE_READ_ONLY
        );

        //min. API Level 21
        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
//        final int pageCount = pdfRenderer.getPageCount();

        PdfRenderer.Page firstPagePDF = pdfRenderer.openPage(pageNum-1);
        Bitmap bitmap = Bitmap.createBitmap(
                firstPagePDF.getWidth(),
                firstPagePDF.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        // Сохраняю снимок первой страницы
        firstPagePDF.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        firstPagePDF.close();
        pdfRenderer.close();
        fileDescriptor.close();

        return bitmap;
    }
}
