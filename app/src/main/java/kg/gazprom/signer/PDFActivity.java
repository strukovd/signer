package kg.gazprom.signer;

import kg.gazprom.signer.common.StorageConfig;
import kg.gazprom.signer.common.StorageManager;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.io.File;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import kg.gazprom.signer.utils.NetworkService;


public class PDFActivity extends AppCompatActivity {
    String URL_TO_PDF = "%s/getFile?documentId=%s&issueKey=%s";
    PDFView viewPDF;
    ProgressBar viewPreloader;
    TextView viewMsg;
    Button viewButtonNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        viewPDF = (PDFView)findViewById(R.id.ap_pdfCanvas);
        viewPreloader = (ProgressBar)findViewById(R.id.ap_pdfCanvasPreloader);
        viewMsg = (TextView)findViewById(R.id.ap_pdfCanvasErrorText);
        viewButtonNext = ((Button)findViewById(R.id.ap_next));
        viewPDF.setVisibility(View.GONE);

        final String documentId = getIntent().getExtras().getString("documentId");
        final String issueKey = getIntent().getExtras().getString("issueKey");
        if(documentId == null) { showErrorMsg("Не получен параметр documentId"); return; }
        if(issueKey == null) { showErrorMsg("Не получен параметр issueKey"); return; }

        StorageManager.documentId = documentId;
        StorageManager.issueKey = issueKey;

        // Скачиваем файл, и отображаем его на экране
        AsyncTask<String, Void, File> AsyncPDFDownload = new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... arrOfUrl) {
                // Загружаем PDF файл
                String url = arrOfUrl[0];
                try {
                    if(StorageManager.pdfFileCache == null) {
                        StorageManager.pdfFileCache = NetworkService.uploadPDF(url, "tempDoc.pdf");
                    }
                    return StorageManager.pdfFileCache;
                } catch (Exception e) {
                    Log.e("CUSTOM", "Ошибка при загрузке файла!");
                    Log.e("CUSTOM", e.toString());
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File pdfFile) {
                super.onPostExecute(pdfFile);

                if(pdfFile == null) {
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при загрузке файла!", Toast.LENGTH_LONG).show();
                    showErrorMsg("Не удалось загрузить документ!");
                }
                else {
                    viewPDF.fromFile(pdfFile).load();
                    viewPDF.setVisibility(View.VISIBLE);
                    viewButtonNext.setEnabled(true);
                }
            }
        }.execute( String.format(URL_TO_PDF, StorageConfig.ADB_INTERACTOR_URL, documentId, issueKey) );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Определение пунктов меню, на событие ж.ц. - создание элементов меню
        menu.add("❮").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); // TODO: Добавить отбработчик
        menu.add("1").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); // TODO: Добавить отбработчик
        menu.add("❯").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); // TODO: Добавить отбработчик
        menu.add("Настройки"); // TODO: Добавить отбработчик

        return super.onCreateOptionsMenu(menu);
    }

    private void showErrorMsg(String msg) {
        viewPreloader.setVisibility(View.GONE);
        viewPDF.setVisibility(View.GONE);

        viewMsg.setText(msg);
        viewMsg.setVisibility(View.VISIBLE);
    }

    public void showSigningScreen(View view) {
        startActivity(new Intent(this, SigningActivity.class));
    }
}
