package kg.gazprom.signer.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import okhttp3.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import kg.gazprom.signer.DTO.ResponseInfo;
import kg.gazprom.signer.common.StorageConfig;

public class NetworkService {

    /**
     * Скачивает файл по указанной ссылке на устройство (во временный файл)
     * @param url
     * @param fileName
     * @return File
     */
    public static File uploadPDF(@NotNull String url, String fileName) throws Exception {
        String fName, fExt;
        if(fileName == null) {
            fName = "temp";
            fExt = ".pdf";
        }
        else {
            String[] splitFileName = fileName.split("\\.(?=[^.]*$)");
            fName= splitFileName[0];
            fExt = "."+( (splitFileName.length == 1)?splitFileName[0]:splitFileName[1] );
        }

        Log.w( "CUSTOM", "uploadPDF: Отправка запроса на: " + url );

        URL pdfUrl = new URL(url);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = pdfUrl.openStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        byte[] pdfBytes = outputStream.toByteArray();

        File tempFile = File.createTempFile( fName, fExt );
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        fileOutputStream.write(pdfBytes);
        fileOutputStream.close();

        return tempFile;
    }


    public static String getOperator(@NotNull String issueKey) throws Exception {
        final String urlString = String.format("%s/getOperator?issueKey=%s", StorageConfig.ADB_INTERACTOR_URL, issueKey);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            return response.toString();
        } else {
            connection.disconnect();
            throw new Exception("При попытке получить исполнителя, получен отрицательнй код ответа!");
        }
    }


    public static String saveAgreementStatus(String documentId, Boolean isAgree) throws Exception {
        Log.i("CUSTOM", "Вызван метод saveAgreementStatus");

        final String urlString = String.format("%s/saveAgreementStatus?documentId=%s&isAgree=%s", StorageConfig.ADB_INTERACTOR_URL, documentId, isAgree.toString());
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            return response.toString();
        } else {
            connection.disconnect();
            throw new Exception("При попытке сохранить статус соглашения получен отрицательнй код ответа!");
        }
    }

    public static String saveGrade(String documentId, String issueKey, int grade, String operator, String operatorDisplayName) throws Exception {
        Log.i("CUSTOM", "Вызван метод saveGrade");

        final String urlString = String.format("%s/saveGrade", StorageConfig.ADB_INTERACTOR_URL);

        // Заполняем body
        JSONObject bodyContent = new JSONObject();
        bodyContent.put("issueKey", issueKey);
        bodyContent.put("documentId", documentId);
        bodyContent.put("grade", grade);
        bodyContent.put("operator", operator);
        bodyContent.put("operatorDisplayName", operatorDisplayName);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // Отправляем данные в теле запроса
        try (
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                OutputStreamWriter osw = new OutputStreamWriter(wr, StandardCharsets.UTF_8) // Важно для UTF-8
        ) {
            osw.write(bodyContent.toString());
            osw.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("CUSTOM", "saveGrade: ", e );
        }


        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            return response.toString();
        } else {
            // Обработка ошибки
            StringBuilder errorResponse = new StringBuilder();
            try {
                // Пытаемся прочитать тело ответа, чтобы получить детали ошибки
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
            } catch (IOException e) {
                // Обработка ошибки чтения тела ответа, если произошла ошибка
                errorResponse.append("Error reading error response: ").append(e.getMessage());
            }

            connection.disconnect();
            String errorMessage = "Server returned HTTP response code: " + responseCode +
                    ", Error response: " + errorResponse.toString();

            throw new Exception(errorMessage);
//            connection.disconnect();
//            throw new Exception("При попытке сохранить оценку получен отрицательнй код ответа!");
        }
    }

    public static ResponseInfo sendBitmapAsPng(@NotNull String url, @NotNull String httpMethod, String documentId, Bitmap image) throws Exception {
        Log.i("CUSTOM", "Вызван метод sendBitmapAsPng");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 95, stream);

        ByteBuffer byteBuffer = ByteBuffer.allocate(image.getByteCount());
        image.copyPixelsToBuffer(byteBuffer);
        byte[] pngByteArray = stream.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType( MediaType.parse("multipart/form-data; charset=utf-8") )
                .addFormDataPart("documentId", documentId)
                .addFormDataPart("signature", "image.png", RequestBody.create(MediaType.parse("image/png"), pngByteArray))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .method(httpMethod, requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        int responseCode = response.code();
        String responseBody = response.body() != null ? response.body().string() : "";

        Log.w("CUSTOM", "responseCode: " + responseCode );

        return new ResponseInfo( responseCode >= 200 && responseCode < 300, responseBody);
    }


    public static String getAgreementId(String documentId) throws Exception {
        Log.i("CUSTOM", "Вызван метод getAgreementId");

        final String urlString = String.format("%s/getAgreementId?documentId=%s", StorageConfig.ADB_INTERACTOR_URL, documentId);
        Log.w("CUSTOM", "Отправка запроса на: " + urlString );
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            return response.toString();
        } else {
            connection.disconnect();
            throw new Exception("При попытке получить номер соглашения, получен отрицательнй код ответа!");
        }
    }


    private static String readResponseBody(InputStream inputStream) throws IOException { //TODO: вынести в общий модуль
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}
