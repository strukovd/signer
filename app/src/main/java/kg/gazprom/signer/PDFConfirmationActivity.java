package kg.gazprom.signer;

import kg.gazprom.signer.common.StorageConfig;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import kg.gazprom.signer.common.StorageManager;
import kg.gazprom.signer.utils.NetworkService;

public class PDFConfirmationActivity extends AppCompatActivity {
    final String URL_TO_PDF = "%s/getFile?documentId=%s&issueKey=%s&withSignature=true";
    PDFView viewPDF;
    ProgressBar viewPreloader;
    TextView viewMsg;
    Button viewButtonDone;
    Button viewButtonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_confirmation);

        viewPDF = (PDFView)findViewById(R.id.apc_pdfCanvas);
        viewPreloader = (ProgressBar)findViewById(R.id.apc_pdfCanvasPreloader);
        viewMsg = (TextView)findViewById(R.id.apc_pdfCanvasErrorText);
        viewButtonDone = ((Button)findViewById(R.id.apc_done));
        viewButtonCancel = ((Button)findViewById(R.id.apc_cancel));
        viewPDF.setVisibility(View.GONE);

        // Скачиваем файл, и отображаем его на экране
        AsyncTask<String, Void, File> AsyncPDFDownload = new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... arrOfUrl) {
                // Загружаем PDF файл (выполняется в отдельном потоке)
                String url = arrOfUrl[0];
                try {
                    if(StorageManager.pdfFileWithSignatureCache == null) {
                        StorageManager.pdfFileWithSignatureCache = NetworkService.uploadPDF(url, "tempDocWithSignature.pdf");
                    }
                    return StorageManager.pdfFileWithSignatureCache;
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
                    showErrorMsg("Не удалось загрузить документ с подписью!");
                }
                else {
                    viewPDF.fromFile(pdfFile).load();
                    viewPDF.setVisibility(View.VISIBLE);
                    viewButtonDone.setEnabled(true);
                }
            }
        }.execute( String.format(URL_TO_PDF, StorageConfig.ADB_INTERACTOR_URL, StorageManager.documentId, StorageManager.issueKey) );
    }

    private void showErrorMsg(String msg) {
        viewPreloader.setVisibility(View.GONE);
        viewPDF.setVisibility(View.GONE);

        viewMsg.setText(msg);
        viewMsg.setVisibility(View.VISIBLE);
    }

    public void onCancelClick(View view) {
        finish();
    }

    public void onDoneClick(View view) {
        String projectKey = StorageManager.issueKey.split("-")[0];
        if( projectKey.equals("NGV4") ) {
            startActivity(new Intent(this, AgreementActivity.class));
        }
        else {
            startActivity(new Intent(this, RatingActivity.class));
        }
    }
}