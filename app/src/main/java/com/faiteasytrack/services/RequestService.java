package com.faiteasytrack.services;

public class RequestService {

//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//
//        try{
//            String notification_title= remoteMessage.getNotification().getTitle();
//            String notification_message=remoteMessage.getNotification().getBody();
//            String click_action=remoteMessage.getNotification().getClickAction();
//
//            String from_user_id=remoteMessage.getData().get("from_user_id");
//            //Log.e("from_user_id in FMS is:",from_user_id);
//
//            //----BUILDING NOTIFICATION LAYOUT----
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(notification_title)
//                    .setContentText(notification_message)
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//            //--CLICK ACTION IS PROVIDED---
//            Intent resultIntent = new Intent(click_action);
//            resultIntent.putExtra("user_id",from_user_id);
//
//            PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(resultPendingIntent);
//
//
//            int mNotificationId=(int)System.currentTimeMillis();
//            NotificationManager mNotifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//            mNotifyManager.notify(mNotificationId,mBuilder.build());
//
//            // Log.e("from_user_id 5 is:",from_user_id);
//        }catch(Exception e){
//            Log.e("Exception is ",e.toString());
//        }
//    }

}
