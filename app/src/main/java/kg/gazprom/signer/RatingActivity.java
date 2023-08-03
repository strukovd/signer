package kg.gazprom.signer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import kg.gazprom.signer.common.StorageManager;
import kg.gazprom.signer.utils.NetworkService;

public class RatingActivity extends AppCompatActivity {
    TextView viewFullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        viewFullname = (TextView)findViewById(R.id.ar_fullname);

        // Запросим данные оператора по ключу задачи
        AsyncTask<String, Void, String> AsyncGetOperator = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... arrOfUrl) {
                try {
                    String response = NetworkService.getOperator(StorageManager.issueKey);
                    Log.w("CUSTOM", "response: "+ response );
                    return response;
                } catch (Exception e) {
                    Log.e("CUSTOM", e.getMessage());
                    return "";
                }
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    StorageManager.operatorFullName = jsonResponse.getString("operatorFullName");
                    StorageManager.operatorUsername = jsonResponse.getString("operatorUsername");
                    viewFullname.setText( StorageManager.operatorFullName );
                } catch (JSONException e) {
                    Log.e("CUSTOM", e.getMessage() );
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }


    private void asyncSaveGrade() {
        new AsyncTask<String, Void, String>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                showPreloader();
//            }

            @Override
            protected String doInBackground(String... arrOfUrl) {
                try {
                    return NetworkService.saveGrade(StorageManager.documentId, StorageManager.issueKey, StorageManager.grade, StorageManager.operatorUsername, StorageManager.operatorFullName);
                } catch (Exception e) {
                    Log.e("CUSTOM", "Ошибка при сохранении оценки!", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if(result == null) {
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при сохранении оценки!", Toast.LENGTH_LONG).show();
                }
                else {
                    startActivity(new Intent(getApplicationContext(), EmojiActivity.class));
                }
            }
        }.execute();
    }

    public void onGradeClick(View view) {
        String grade = ((Button)view).getText().toString();
        if( grade != null && grade.matches("\\d{1,2}") ) {
            StorageManager.grade = Integer.parseInt(grade);

            try {
                // Отправляем оценку на сервер
                this.asyncSaveGrade();
            } catch (Exception e) {
                Log.e("CUSTOM", e.getMessage() );
                throw new RuntimeException(e);
            }
        }
    }
}
