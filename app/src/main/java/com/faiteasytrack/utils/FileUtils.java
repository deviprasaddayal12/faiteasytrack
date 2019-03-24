package com.faiteasytrack.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.core.content.ContextCompat;
import id.zelory.compressor.Compressor;

/**
 * Created by AND on 6/27/2018.
 */

public class FileUtils {
    public static final String TAG = "FileUtils";
    private static final int THUMBNAIL_SIZE = 64;

    public static String getFileRealName(String filepath) {
        String[] fileNameParts = filepath.split("/");
        return fileNameParts[fileNameParts.length - 1];
    }

    /**
     * This method is called to create one, if file does not exist
     *
     * @param directory name for the folder to be created
     */
    private static File mkDir(File directory) {
        if (!directory.exists())
            directory.mkdirs();
        return directory;
    }

    public static File getCompressedFile(Context context, String filePath) {
        File imgFile = new File(filePath);
        File compressedFile = imgFile;
        try {
            long length = imgFile.length();
            length = length / 1024;
            Log.d("Image length ", length + "");
            if (length > 100) {
                compressedFile = Compressor.getDefault(context).compressToFile(imgFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showSorryAlert(context, "File Compression Error:\n" + e.getLocalizedMessage(), null);
        }
        Log.i("FileUtils", "" + imgFile.getAbsolutePath());
        return compressedFile;
    }

    public static Bitmap getThumbnail(Context context, Uri uri) throws FileNotFoundException, IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(inputStream, null, onlyBoundsOptions);

        if (inputStream != null)
            inputStream.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOptions);

        if (inputStream != null)
            inputStream.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    public static Bitmap createImageThumbnail(Context context, Uri uri) {
        try {
            File compressedFile = getCompressedFile(context, uri.getPath());

            FileInputStream fis = new FileInputStream(compressedFile.getAbsolutePath());
            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);

            Float width = (float) imageBitmap.getWidth();
            Float height = (float) imageBitmap.getHeight();
            Float ratio = width / height;
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (THUMBNAIL_SIZE * ratio), THUMBNAIL_SIZE, false);

            return imageBitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void createImageThumbnail(Context context, Uri uri, File file) {
        try {
            Bitmap imageBitmap = createImageThumbnail(context, uri);

            saveThumbnail(file, imageBitmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void createVideoThumbnail(Context context, Uri uri, File file) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
        saveThumbnail(file, bitmap);
    }

    public static void createDocumentThumbnail(Context context, Uri uri, File file) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);
        try {
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            pdfiumCore.closeDocument(pdfDocument);

            saveThumbnail(file, bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveThumbnail(File file, Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTempFileSuffix(String uri) {
        String[] splitUri = uri.split("\\.");
        Log.i("<<<<<suffix", "" + "." + splitUri[splitUri.length - 1]);
        return "." + splitUri[splitUri.length - 1];
    }

    public static String getPlaybackDuration(int milliseconds) {
        String min, sec;
        int seconds = milliseconds / 1000;
        int mins = seconds / 60, secs = seconds % 60;
        if (mins < 10)
            min = "0" + mins;
        else
            min = "" + mins;

        if (secs < 10)
            sec = "0" + secs;
        else
            sec = "" + secs;

        return min + ":" + sec;
    }

    public static String getFileSize(long size) {
        long oneMB = 1024 * 1024, oneKB = 1024, oneMBThousandth = 1000 / oneMB, oneKBThousandth = 1000 / oneKB;
        if (size / oneMB > 0) {
            return size / oneMB + "." + (oneMBThousandth * (size % oneMB)) + " MB";
        } else
            return size / oneKB + "." + (oneKBThousandth * (size % oneKB)) + " KB";
    }

    public static String getInternalStoragePath(final Context context, final Uri uri) throws IOException {
        //check here to KITKAT or new version
        final boolean isKitKat = /*Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT*/ true;

        // DocumentProvider
        if (/*isKitKat &&*/ DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }

            //DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isSDCardAvailable(Context context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        return storages.length > 1 && storages[0] != null && storages[1] != null;
    }

    private static String getSDCardRotPath(Context context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        File storageInSDCard = storages[1];
        File removableSDCardAsRootFile = storageInSDCard.getParentFile().getParentFile().getParentFile().getParentFile();
        return removableSDCardAsRootFile.getAbsolutePath();
    }

    public static String getExternalStoragePath(Context context, Uri uri) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            String sdcardPath = getSDCardRotPath(context);
            return sdcardPath.concat("/").concat(split[1]);
        } else
            return null;
    }

    public static String getMimeType(String uri) {
        String[] fileNameParts = new File(uri).getName().split("\\.");
        switch (fileNameParts[fileNameParts.length - 1]) {
            // text extensions
            case "txt":
                return FileExtension.txt;
            // image extensions
            case "bmp":
                return FileExtension.bmp;
            case "cgm":
                return FileExtension.cgm;
            case "gif":
                return FileExtension.gif;
            case "jpeg":
                return FileExtension.jpeg;
            case "jpg":
                return FileExtension.jpg;
            case "mdi":
                return FileExtension.mdi;
            case "psd":
                return FileExtension.psd;
            case "png":
                return FileExtension.png;
            case "svg":
                return FileExtension.svg;
            // audio extensions
            case "adp":
                return FileExtension.adp;
            case "aac":
                return FileExtension.aac;
            case "mpga":
                return FileExtension.mpga;
            case "mp4a":
                return FileExtension.mp4a;
            case "oga":
                return FileExtension.oga;
            case "wav":
                return FileExtension.wav;
            case "mp3":
                return FileExtension.mp3;
            // video extensions
            case "3gp":
                return FileExtension.a3gp;
            case "3g2":
                return FileExtension.a3g2;
            case "avi":
                return FileExtension.avi;
            case "xlv":
                return FileExtension.xlv;
            case "m4v":
                return FileExtension.m4v;
            case "mp4":
                return FileExtension.mp4;
            // documents extension
            case "rar":
                return FileExtension.rar;
            case "zip":
                return FileExtension.zip;
            case "pdf":
                return FileExtension.pdf;
            case "dvi":
                return FileExtension.dvi;
            case "karbon":
                return FileExtension.karbon;
            case "mdb":
                return FileExtension.mdb;
            case "xls":
                return FileExtension.xls;
            case "pptx":
                return FileExtension.pptx;
            case "docx":
                return FileExtension.docx;
            case "ppt":
                return FileExtension.ppt;
            case "doc":
                return FileExtension.doc;
            case "oxt":
                return FileExtension.oxt;
            default:
                return "file/*";
        }
    }

    public interface FileExtension {
        // Text Extensions
        String txt = "text/plain";

        // Image Extensions
        String bmp = "image/bmp";
        String cgm = "image/cgm";
        String gif = "image/gif";
        String jpeg = "image/jpeg";
        String jpg = "image/jpg";
        String mdi = "image/vnd.ms-modi";
        String psd = "image/vnd.adobe.photoshop";
        String png = "image/png";
        String svg = "image/svg";

        // Audio Extensions
        String adp = "audio/adpcm";
        String aac = "audio/x-aac";
        String mpga = "audio/mpeg";
        String mp4a = "audio/mp4";
        String oga = "audio/ogg";
        String wav = "audio/x-wav";
        String mp3 = "audio/mp3";

        // Video Extensions
        String a3gp = "video/3gpp";
        String a3g2 = "video/3gpp2";
        String avi = "video/x-msvideo";
        String xlv = "video/x-flv";
        String m4v = "video/x-m4v";
        String mp4 = "video/mp4";

        // Document Extensions
        String pdf = "application/pdf";
        String dvi = "application/x-dvi";
        String karbon = "application/vnd.kdee.karbon";
        String mdb = "application/x-msaccess";
        String xls = "application/vnd.ms-excel";
        String pptx = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        String docx = "application/vnd.openxmlformats-officedocument.spreadsheet.template";
        String ppt = "application/vnd.ms-powerpoint";
        String doc = "application/vnd.ms-word";
        String oxt = "application/vnd.openofficeorg.extension";
        String rar = "application/x-rar-compressed";
        String zip = "application/zip";

        // For all
        String all = "file/*";
    }

    public static String getFileCategory(String uri) {
        String mimeType = getMimeType(uri);
        return mimeType.split("/")[0];
    }

    public interface FileCategory {
        String application = "application";
        String audio = "audio";
        String image = "image";
        String text = "text";
        String video = "video";
    }
}
