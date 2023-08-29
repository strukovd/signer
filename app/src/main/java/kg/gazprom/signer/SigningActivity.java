package kg.gazprom.signer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
// import android.view.Menu;
// import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.github.gcacace.signaturepad.views.SignaturePad;

import kg.gazprom.signer.DTO.ResponseInfo;
import kg.gazprom.signer.common.StorageConfig;
import kg.gazprom.signer.common.StorageManager;
import kg.gazprom.signer.utils.NetworkService;
import kg.gazprom.signer.views.DrawingView;

public class SigningActivity extends AppCompatActivity implements LifecycleOwner {
    DrawingView viewSignCanvas;
    SignaturePad viewSignPad;


     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add("Сброс").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         return super.onCreateOptionsMenu(menu);
     }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                viewSignPad.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);

        viewSignPad = findViewById(R.id.as_signaturePad);
    }

    public void onSignButtonClick(View view) throws Exception {
        StorageManager.signBitmap = extractSignature();

        AsyncTask<String, Boolean, ResponseInfo> sendSignOnServer = new AsyncTask<String, Boolean, ResponseInfo>() {
            @Override
            protected ResponseInfo doInBackground(String... strings) {
                try {
                    // Сохраняю картинку подписи
                    ResponseInfo signatureIsSaved = NetworkService.sendBitmapAsPng(
                            String.format("%s/saveSign", StorageConfig.ADB_INTERACTOR_URL),
                            "POST",
                            StorageManager.documentId,
                            StorageManager.signBitmap
                    );
                    return signatureIsSaved;
                } catch (Exception e) {
                    Log.e("CUSTOM", e.getMessage() );
                    return new ResponseInfo(false, e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(ResponseInfo signatureIsSaved) {
                super.onPostExecute(signatureIsSaved);

                // Если картинка успешно сохранена
                if(signatureIsSaved.success) {
                    StorageManager.pdfFileWithSignatureCache = null; // Сбрасываем закэшированный документ, что бы обновилась подпись
                    startActivity(new Intent(getApplicationContext(), PDFConfirmationActivity.class));
                }
                else {
                    Toast.makeText(getBaseContext(), signatureIsSaved.msg, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private Bitmap extractSignature() {
        if(viewSignPad != null) {
            viewSignPad.setDrawingCacheEnabled(true);
            Bitmap drawingCache = Bitmap.createBitmap(viewSignPad.getDrawingCache());
            viewSignPad.setDrawingCacheEnabled(false);

            return cropEmptySpace(drawingCache);
        }

        return null;
    }

    private Bitmap cropEmptySpace(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int left = 0;
        int top = 0;
        int right = width - 1;
        int bottom = height - 1;

        // Обрезаем верхние пустые строки
        while (top < bottom && isRowEmpty(image, top)) {
            top++;
        }

        // Обрезаем нижние пустые строки
        while (bottom > top && isRowEmpty(image, bottom)) {
            bottom--;
        }

        // Обрезаем левые пустые столбцы
        while (left < right && isColumnEmpty(image, left, top, bottom)) {
            left++;
        }

        // Обрезаем правые пустые столбцы
        while (right > left && isColumnEmpty(image, right, top, bottom)) {
            right--;
        }

        // Создаем обрезанное изображение
        return Bitmap.createBitmap(image, left, top, right - left + 1, bottom - top + 1);
    }

    private boolean isRowEmpty(Bitmap image, int row) {
        int width = image.getWidth();
        for (int i = 0; i < width; i++) {
            if (image.getPixel(i, row) != Color.TRANSPARENT) {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnEmpty(Bitmap image, int column, int top, int bottom) {
        for (int i = top; i <= bottom; i++) {
            if (image.getPixel(column, i) != Color.TRANSPARENT) {
                return false;
            }
        }
        return true;
    }
}
