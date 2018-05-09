package com.chan.example.lookatme.sqlite;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 채팅방에 관련된 DB
 */
public class DBHelper_chattingRoom extends SQLiteOpenHelper{

    /**
     * DBHelper_chattingRoom
     *  - DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받는다.
     */
    public DBHelper_chattingRoom(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * onCreate
     *  - DB를 새로 생성할 때 호출되는 함수.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE chattingRoom(" +
                        "chattingRoom_no INTEGER PRIMARY KEY AUTOINCREMENT, " + // 채팅방 고유 번호.
                        "chattingRoom_public_key TEXT, " + // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
                        "chattingRoom_accept_member_email TEXT, " + // 채팅방에 들어 있는 회원들의 이메일.
                        "chattingRoom_accept_member_name TEXT, " + // 채팅방에 들어 있는 회원들의 이름.
                        "chattingRoom_accept_member_image TEXT, " + // 채팅방에 들어 있는 회원들의 이미지.
                        "chattingRoom_name TEXT, " + // 채팅방 이름.
                        "chattingRoom_message_contents TEXT, " + // 채팅방 목록에서 보여질 마지막 채팅 내용.
                        "chattingRoom_message_time TEXT, " + // 채팅방 목록에서 보여질 마지막 채팅 메세지의 시간.
                        "chattingRoom_message_unread_count INTEGER, " + // 읽지 않은 메세지 수.
                        "chattingRoom_message_notification_status INTEGER " + // 알림 상태. 0이면 알림이 가고 1이면 해당 채팅방을 켜둔 상태라 알림이 가지 않는다..
                ");"
        );
    }

    /**
     * insert : 채팅방을 생성.
     * @param chattingRoom_public_key : 사용자들이 모두 공통적으로 가진 같은 이름의 방 key.
     * @param chattingRoom_accept_member_email : 채팅방에 들어 있는 회원들의 이메일.
     * @param chattingRoom_accept_member_name : 채팅방에 들어 있는 회원들의 이름.
     * @param chattingRoom_accept_member_image : 채팅방에 들어 있는 회원들의 이미지.
     * @param chattingRoom_name : 채팅방 이름.
     * @param chattingRoom_message_contents : 채팅방 목록에서 보여질 마지막 채팅 내용.
     * @param chattingRoom_message_time : 채팅방 목록에서 보여질 마지막 채팅 메세지의 시간.
     */
    public void insert(
            String chattingRoom_public_key, String chattingRoom_accept_member_email,String chattingRoom_accept_member_name ,
            String chattingRoom_accept_member_image, String chattingRoom_name, String chattingRoom_message_contents,
            String chattingRoom_message_time){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
                "INSERT INTO chattingRoom VALUES( " +
                        "null, " +
                        "'"+chattingRoom_public_key+"', " +
                        "'"+chattingRoom_accept_member_email+"', " +
                        "'"+chattingRoom_accept_member_name+"', " +
                        "'"+chattingRoom_accept_member_image+"', " +
                        "'"+chattingRoom_name+"', " +
                        "'"+chattingRoom_message_contents+"', " +
                        "'"+chattingRoom_message_time+"', " +
                        "0, " + // 읽지 않은 메세지 수.
                        "0" + // notification 알림 상태.
                ");"
        );
        db.close();
    }

    // selectChattingRoomList : 채팅방 목록
    public String selectChattingRoomList(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select chattingRoom_public_key," +
                        "chattingRoom_accept_member_email," +
                        "chattingRoom_accept_member_image," +
                        "chattingRoom_name," +
                        "chattingRoom_message_contents," +
                        "chattingRoom_message_time, " +
                        "chattingRoom_message_unread_count " +
                        "from chattingRoom order by chattingRoom_message_time desc;", null);
        JSONArray jsonArray = new JSONArray();
//        if(cursor!=null && cursor.getCount()>0){
            while (cursor.moveToNext()){
                try {
                    JSONObject jsonObject = new JSONObject();
                    int index = 0;
                    jsonObject.put("chattingRoom_public_key", cursor.getString(index++));
                    jsonObject.put("chattingRoom_accept_member_email", cursor.getString(index++));
                    jsonObject.put("chattingRoom_accept_member_image", cursor.getString(index++));
                    jsonObject.put("chattingRoom_name", cursor.getString(index++));
                    jsonObject.put("chattingRoom_message_contents", cursor.getString(index++));
                    jsonObject.put("chattingRoom_message_time", cursor.getString(index++));
                    jsonObject.put("chattingRoom_message_unread_count", cursor.getInt(index++));
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//        }
        String check = "[]";
        if(check.equals(jsonArray.toString())){
            cursor.close();
            db.close();
            return "no_data";
        }
        cursor.close();
        db.close();
        return jsonArray.toString();
    }

    // 채팅방 안으로 들어가서 채팅창이 나오면 해당 채팅방의 정보를 가져온다.
    // 채팅방에 사람이 나갔을 경우 기존 채팅방의 정보를 가져온다.
    public String selectChattingRoomInformation(String chattingRoom_public_key){
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "select chattingRoom_accept_member_email," +
                        "chattingRoom_accept_member_name," +
                        "chattingRoom_accept_member_image," +
                        "chattingRoom_name " +
                "from chattingRoom where chattingRoom_public_key='"+chattingRoom_public_key+"'";
        Cursor cursor = db.rawQuery(query,null);
        JSONObject jsonObject = new JSONObject();
        while (cursor.moveToNext()){
            try {
                int index = 0;
                jsonObject.put("chattingRoom_accept_member_email", cursor.getString(index++));
                jsonObject.put("chattingRoom_accept_member_name", cursor.getString(index++));
                jsonObject.put("chattingRoom_accept_member_image", cursor.getString(index++));
                jsonObject.put("chattingRoom_name", cursor.getString(index++));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        return jsonObject.toString();
    }

    /**
     * 채팅 메세지가 오면 채팅방 정보가 업데이트
     * @param chattingRoom_public_key : 사용자들이 모두 공통적으로 가진 방 key.
     * @param chattingRoom_message_contents : 채팅방 목록에서 보여질 마지막 채팅 내용.
     * @param chattingRoom_message_unread_count : 읽지 않은 채팅 메세지 수.
     * @param chattingRoom_message_time : 채팅방 목록에서 보여질 마지막 채팅 메세지의 시간.
     */
    public void updateChattingRoom_messageContents_messageTime(String chattingRoom_message_contents, String chattingRoom_message_time, int chattingRoom_message_unread_count, String chattingRoom_public_key){
        SQLiteDatabase db = getWritableDatabase();
        String query =
                "UPDATE chattingRoom SET " +
                        "chattingRoom_message_contents='"+chattingRoom_message_contents+"', " +
                        "chattingRoom_message_time='"+chattingRoom_message_time+"', " +
                        "chattingRoom_message_unread_count=(chattingRoom_message_unread_count+"+chattingRoom_message_unread_count+")" +
                    "WHERE " +
                        "chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }

    /**
     * 1. 상대방으로부터 채팅메세지가 왔는데 메세지 해당 채팅방이 켜져있으면 읽지 않은 메세지 수를 초기화해준다.
     *    서비스단에서 count + 1을 해줘서.(참조) db updateChattingRoom_messageContents_messageTime
     * 2. 해당 채팅방 화면을 키면 읽지 않은 메세지 수를 초기화해준다.
     */
    public void update_chattingRoom_message_unread_count_reset(String chattingRoom_public_key){
        SQLiteDatabase db = getWritableDatabase();
        String query =
                "UPDATE chattingRoom SET " +
                        "chattingRoom_message_unread_count=0 " +
                        "WHERE " +
                        "chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }

    /**
     * update_notification_status_0_notiOk : 알림 Ok, 알림 받는 상태로 변경
     * update_notification_status_1_notiNo : 알림 No, 알림 받지 않는 상태로 변경
     * select_notification_status : 현재 알림 받을 상태를 확인.
     */
    public void update_notification_status_0_notiOk(String chattingRoom_public_key){
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE chattingRoom SET chattingRoom_message_notification_status=0 WHERE chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }
    public void update_notification_status_1_notiNo(String chattingRoom_public_key){
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE chattingRoom SET chattingRoom_message_notification_status=1 WHERE chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }
    public int select_notification_status(String chattingRoom_public_key){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select chattingRoom_message_notification_status from chattingRoom " +
                        "where chattingRoom_public_key=\""+chattingRoom_public_key+"\""
                , null);
        int result = 0;
        while (cursor.moveToNext()){
            result = cursor.getInt(0);
        }
        return result;
    }

    // 채팅방 이름 변경.
    public void update_chattingRoom_name(String chattingRoom_public_key, String chattingRoom_name){
        SQLiteDatabase db = getWritableDatabase();
        String query =
                "update chattingRoom set " +
                        "chattingRoom_name='"+chattingRoom_name+"' " +
                    "where chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }

    // 채팅방에 사람이 초대되면 채팅방에 있는 기존 회원들의 방 정보 업데이트.
    // 채팅방에서 사람이 나갔을때 현재 채팅방에 남아있는 사람들의 DB 에 해당 채팅방에 대한 정보를 수정한다.
    public void update_chattingRoom_information(
            String chattingRoom_public_key, String chattingRoom_accept_member_email,
            String chattingRoom_accept_member_name, String chattingRoom_accept_member_image){
        SQLiteDatabase db = getWritableDatabase();
        String query =
                "update chattingRoom set " +
                    "chattingRoom_accept_member_email='"+chattingRoom_accept_member_email+"', " +
                    "chattingRoom_accept_member_name='"+chattingRoom_accept_member_name+"', " +
                    "chattingRoom_accept_member_image='"+chattingRoom_accept_member_image+"' " +
                "where chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }

    // 채팅방에서 사람이 나갔을때 나간 회원의 DB 에서 해당 채팅방을 지워준다.
    public void delete_chatting_room(String chattingRoom_public_key){
        SQLiteDatabase db = getWritableDatabase();
        String query = "delete from chattingRoom where chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }












    /**
     * onUpgrade
     *  - DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
