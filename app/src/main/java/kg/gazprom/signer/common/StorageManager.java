package kg.gazprom.signer.common;

import android.graphics.Bitmap;

import java.io.File;

public class StorageManager {
    public static Bitmap signBitmap = null;
    public static File pdfFileCache = null;
    public static File pdfFileWithSignatureCache = null;
    public static String documentId = null;
    public static String operatorUsername = null;
    public static String operatorFullName = null;


    public static Bitmap agreementSignBitmap = null;
    public static File agreementPdfFileCache = null;
    public static File agreementPdfFileWithSignatureCache = null;
    public static String agreementDocumentId = null;
    public static Boolean isAgree = null;


    public static String issueKey = null;
    public static int grade = 0;
}
