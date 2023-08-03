package kg.gazprom.signer;

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

import org.json.JSONObject;

import java.io.File;

import kg.gazprom.signer.common.StorageConfig;
import kg.gazprom.signer.common.StorageManager;
import kg.gazprom.signer.utils.NetworkService;

public class AgreementActivity extends AppCompatActivity {
    PDFView viewPDF;
    ProgressBar viewPreloader;
    TextView viewMsg;
    Button viewAgree;
    Button viewDisagree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        viewPDF = (PDFView)findViewById(R.id.aa_pdfCanvas);
        viewPreloader = (ProgressBar)findViewById(R.id.aa_pdfCanvasPreloader);
        viewMsg = (TextView)findViewById(R.id.aa_pdfCanvasErrorText);
        viewAgree = (Button)findViewById(R.id.aa_agree);
        viewDisagree = (Button)findViewById(R.id.aa_disagree);

        AsyncTask<String, Void, File> AsyncPDFAgreementDownload = new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... arrOfUrl) {
                // Загружаем PDF файл
                String url = arrOfUrl[0];
                try {
                    if(StorageManager.agreementPdfFileCache == null) {
                        Log.w("CUSTOM", "vo:" + url );
                        StorageManager.agreementPdfFileCache = NetworkService.uploadPDF(url, "tempAgreementDoc.pdf");
                    }
                    return StorageManager.agreementPdfFileCache;
                } catch (Exception e) {
                    Log.e("CUSTOM", "Ошибка при загрузке файла!", e);
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
                    viewPreloader.setVisibility(View.GONE);
                    viewPDF.setVisibility(View.VISIBLE);
                    viewAgree.setEnabled(true);
                }
            }
        };

        // Получим documentId файла соглашения (для текущей задачи)
        AsyncTask<String, Void, String> AsyncGetAgreementId = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... arrOfParams) {
                String url = arrOfParams[0];
                try {
                    if(StorageManager.agreementDocumentId == null) {
                        String response = NetworkService.getAgreementId(StorageManager.documentId);
                        StorageManager.agreementDocumentId = String.valueOf(new JSONObject(response).getInt("result"));
                    }
                    return StorageManager.agreementDocumentId;
                } catch (Exception e) {
                    Log.e("CUSTOM", e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String agreementId) {
                super.onPostExecute(agreementId);

                if(agreementId == null) {
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при загрузке файла!", Toast.LENGTH_LONG).show();
                    showErrorMsg("Не удалось загрузить документ!");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Получил ID соглашения!" + agreementId, Toast.LENGTH_LONG).show();
                    AsyncPDFAgreementDownload.execute( String.format("%s/getFile?documentId=%s", StorageConfig.ADB_INTERACTOR_URL, agreementId) );
                }
            }
        }.execute( String.format("%s/getFile?documentId=%s&agreement=true", StorageConfig.ADB_INTERACTOR_URL, StorageManager.documentId) );
    }


    private void showErrorMsg(String msg) {
        viewPreloader.setVisibility(View.GONE);
        viewPDF.setVisibility(View.GONE);

        viewMsg.setText(msg);
        viewMsg.setVisibility(View.VISIBLE);
    }

    private void showPreloader() {
        viewPDF.setVisibility(View.GONE);
        viewMsg.setVisibility(View.GONE);
        viewPreloader.setVisibility(View.VISIBLE);
    }

    private void showPDF() {
        viewPDF.setVisibility(View.VISIBLE);
        viewMsg.setVisibility(View.GONE);
        viewPreloader.setVisibility(View.GONE);
    }


    private void saveAgreementStatus() {
        // Сохранить статус соглашения
        new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showPreloader();
            }

            @Override
            protected String doInBackground(String... arrOfUrl) {
                try {
                    return NetworkService.saveAgreementStatus(StorageManager.documentId, StorageManager.isAgree);
                } catch (Exception e) {
                    Log.e("CUSTOM", "Ошибка при сохранении статуса соглашения!", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if(result == null) {
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при сохранении статуса соглашения!", Toast.LENGTH_LONG).show();
                    showErrorMsg("Не удалось сохранить статус соглашения!");
                }
                else {
                    showPDF();
                    startActivity(new Intent(getApplicationContext(), AgreementSigningActivity.class));
                }
            }
        }.execute();
    }


    public void onAgreeClick(View view) {
        StorageManager.isAgree = true;
        this.saveAgreementStatus();
    }

    public void onDisagreeClick(View view) {
        StorageManager.isAgree = false;
        this.saveAgreementStatus();
    }
}
