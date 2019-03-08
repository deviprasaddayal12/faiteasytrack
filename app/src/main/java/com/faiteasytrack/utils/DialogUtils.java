package com.faiteasytrack.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.faiteasytrack.R;

public class DialogUtils {

    private static final String TAG = "DialogUtils";

    public static void showSorryAlert(Context context, String message, final Runnable handler) {
        showSorryAlert(context, message, "OK", null, handler, null);
    }

    public static void showSorryAlert(Context context, String message, String pText, String nText,
                                      final Runnable okHandler, final Runnable cancelHandler) {
        try {
            if (context != null) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
                dialog.setTitle("Sorry!");
                dialog.setMessage(message);
                dialog.setCancelable(false);
                dialog.setPositiveButton(pText, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (okHandler != null) {
                            okHandler.run();
                        }
                    }
                });
                if (nText != null){
                    dialog.setNegativeButton(nText, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (cancelHandler != null) {
                                cancelHandler.run();
                            }
                        }
                    });

                }
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showCheersAlert(Context context, String message, final Runnable handler) {
        showCheersAlert(context, message, "OK", null, handler, null);
    }

    public static void showCheersAlert(Context context, String message, String pText, String nText,
                                      final Runnable okHandler, final Runnable cancelHandler) {
        try {
            if (context != null) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
                dialog.setTitle("Cheers!");
                dialog.setMessage(message);
                dialog.setCancelable(false);
                dialog.setPositiveButton(pText, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (okHandler != null) {
                            okHandler.run();
                        }
                    }
                });
                if (nText != null){
                    dialog.setNegativeButton(nText, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (cancelHandler != null) {
                                cancelHandler.run();
                            }
                        }
                    });

                }
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDoYouKnowAlert(Context context, String message, final Runnable handler) {
        try {
            if (context != null) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
                dialog.setTitle("Do You Know?");
                dialog.setMessage(message);
                dialog.setCancelable(false);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (handler != null) {
                            handler.run();
                        }
                    }
                });
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
