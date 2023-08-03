package kg.gazprom.signer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import kg.gazprom.signer.common.StorageManager;

public class EmojiActivity extends AppCompatActivity {
    ImageView viewEmoji;
    TextView viewMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji);
        getSupportActionBar().hide();

        viewEmoji = (ImageView)findViewById(R.id.ae_emoji);
        viewMsg = (TextView)findViewById(R.id.ae_msg);

        if(StorageManager.grade > 4) {
            viewEmoji.setImageResource(R.drawable.smiley_icon);
            viewMsg.setText("Спасибо за оценку");
        }
        else {
            viewEmoji.setImageResource(R.drawable.sad_icon);
            viewMsg.setText("Простите, я буду лучше работать");
        }
    }
}
