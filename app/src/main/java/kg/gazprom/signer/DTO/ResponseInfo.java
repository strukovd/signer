package kg.gazprom.signer.DTO;
import androidx.annotation.NonNull;

public class ResponseInfo {
    public ResponseInfo(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean success;
    public String msg;

    @NonNull
    @Override
    public String toString() {
        return String.format("{\"success\": \"%s\", \"msg\": \"%s\"}", Boolean.toString(success), msg);
    }
}
