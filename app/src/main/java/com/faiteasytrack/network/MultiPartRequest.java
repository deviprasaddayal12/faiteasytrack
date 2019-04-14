package com.faiteasytrack.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;


public class MultiPartRequest extends Request<String> {
    public static final String TAG = MultiPartRequest.class.getCanonicalName();

    private Response.Listener<String> mListener;
    private HttpEntity mHttpEntity;
    private HashMap<String, String> params;
    private Context context;

    public MultiPartRequest(Context context, Response.ErrorListener errorListener, Response.Listener listener,
                            ArrayList<File> file, String path, int callType, HashMap<String, String> params) {
        super(Method.POST, path, errorListener);
        mListener = listener;
        this.context = context;
        this.params = params;
        switch (callType) {
            case Keys.CallType.REQUISITION_FILES_UPLOAD:
                mHttpEntity = buildRequisitionMultipartEntity(file);
                break;
            case Keys.CallType.DAILY_ISSUE_FILES_UPLOAD:
                mHttpEntity = buildDailyIssueMultipartEntity(file);
                break;
            case Keys.CallType.MRN_ADD_FILES_UPLOAD:
                mHttpEntity = buildMRNMultipartEntity(file);
                break;
        }
    }

    private HttpEntity buildRequisitionMultipartEntity(ArrayList<File> file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (int i = 0; i < file.size(); i++) {
            FileBody fileBody = new FileBody(file.get(i));
            builder.addPart(Keys.Query.KEY_FILES, fileBody);
        }
        builder.addTextBody(Keys.Query.KEY_REQUISITION_ID, params.get("id"));
        return builder.build();
    }

    private HttpEntity buildDailyIssueMultipartEntity(ArrayList<File> files) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        if (files.get(0) != null){
            FileBody fileBody1 = new FileBody(files.get(0));
            Log.i(TAG, "buildDailyIssueMultipartEntity: " + files.get(0).getAbsolutePath());
            builder.addPart(Keys.Query.KEY_FILES, fileBody1);
        }

        if (files.size() > 1 && files.get(1) != null){
            FileBody fileBody2 = new FileBody(files.get(1));
            Log.i(TAG, "buildDailyIssueMultipartEntity: " + files.get(1).getAbsolutePath());
            builder.addPart(Keys.Query.KEY_FILES, fileBody2);
        }

        builder.addTextBody(Keys.Query.KEY_REQUEST_TYPE, params.get("request_type"));
        builder.addTextBody(Keys.Query.KEY_DAILY_ISSUE_ID, params.get("id"));
        return builder.build();
    }

    private HttpEntity buildMRNMultipartEntity(ArrayList<File> file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        FileBody fileBody1 = new FileBody(file.get(0));
//        builder.addPart(Constants.Keys.Query.KEY_FILES, fileBody1);
        FileBody fileBody2 = new FileBody(file.get(file.size() - 1));
        builder.addPart(Keys.Query.KEY_FILE, fileBody2);
        builder.addTextBody(Keys.Query.KEY_REQUEST_TYPE, params.get("request_type"));
        builder.addTextBody(Keys.Query.KEY_MRN_ADD_ID, params.get("id"));
        return builder.build();
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            mHttpEntity.writeTo(bos);
            return bos.toByteArray();
        } catch (IOException | OutOfMemoryError e) {
            VolleyLog.e("" + e);
            return null;
        }

    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(new String(response.data, "UTF-8"),
                    getCacheEntry());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.success(new String(response.data),
                    getCacheEntry());
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }


    private String mimeName(String someFilepath) {
        String extension = someFilepath.substring(someFilepath.lastIndexOf("."));
        if (extension == null || extension.isEmpty()) {
            return "";
        }
        return extension;
    }

    public interface Keys {

        interface CallType {
            int REQUISITION_FILES_UPLOAD = 580;
            int DAILY_ISSUE_FILES_UPLOAD = 581;
            int MRN_ADD_FILES_UPLOAD = 582;
        }

        interface Query {
            String KEY_REQUEST_TYPE = "REQUEST_TYPE_SENT";
            String KEY_FILES = "files[]";
            String KEY_REQUISITION_ID = "requisition_id";
            String KEY_DAILY_ISSUE_ID = "issue_slip_id";
            String KEY_MRN_ADD_ID = "mrn_id";
            String KEY_FILE = "file";
        }

        interface VolleyRetryPolicy {
            int SOCKET_TIMEOUT = 1000 * 500;
            int RETRIES = 0;
        }
    }
}