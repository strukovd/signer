package kg.gazprom.signer.facades;

import android.os.Build;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Cmd {
    public static String runCommand(final String command) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String res = null;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            res = reader.lines().collect(Collectors.joining("\n"));
            return res;
        }
        else {
            throw new Exception("Не удалось запустить консольную команду, поскольку необходима минимальная версия SDK: " + Build.VERSION_CODES.N);
        }
    }
}
