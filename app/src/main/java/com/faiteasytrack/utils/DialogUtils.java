package com.faiteasytrack.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.faiteasytrack.R;

public class DialogUtils {

    private static final String TAG = "DialogUtils";

    public static void showLogoutDialog(Context context, final Runnable logoutRunnable, final Runnable cancelRunnable){
        try {
            if (context != null) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
                dialog.setTitle("Logout?");
                dialog.setMessage("Logging out will erase all your saved preferences and data!");
                dialog.setCancelable(true);
                dialog.setPositiveButton("Logout", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (logoutRunnable != null) {
                            logoutRunnable.run();
                        }
                    }
                });
                if (cancelRunnable != null){
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            cancelRunnable.run();
                        }
                    });

                }
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public static AlertDialog showFileSourceChooserDialog(Context context, final String[] sourceTitles,
                                                          final onListDialogClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        builder.setTitle("Choose source:");
        builder.setItems(sourceTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (listener != null)
                    listener.onItemSelected(i, sourceTitles[i]);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(true);
        return builder.create();
    }

    public interface onListDialogClickListener {
        void onItemSelected(int position, String itemTitle);
    }
}
