package com.chan.example.lookatme.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 채팅 메세지에 관련된 DB.
 */
public class DBHelper_chattingMessage extends SQLiteOpenHelper{

    public DBHelper_chattingMessage(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE chattingMessage(" +
                        "chattingMessage_no INTEGER PRIMARY KEY AUTOINCREMENT, " + // 채팅메세지 고유 번호.
                        "chattingRoom_public_key TEXT, " + // 사용자들이 모두 공통적으로 가진 같은 이름의 방 key. 채팅메시지가 어떤 채팅방 메세지인지 확인.
                        "chattingMessage_send_member_email TEXT, " + // 채팅 메세지 보내는 회원의 이메일.
                        "chattingMessage_send_member_image TEXT, " + // 채팅 메세지 보내는 회원의 이미지.
                        "chattingMessage_send_member_name TEXT, " + // 채팅 메세지 보내는 회원의 이름.
                        "chattingMessage_contents TEXT, " +  // 채팅 메세지 내용.
                        "chattingMessage_time TEXT, " +  // 채팅 메세지 내용.
                        "chattingMessage_type TEXT " + // 이미지 전송인지(image), 채팅메세지 전송인지(message), etc(채팅방 초대, 채팅방 나가기) 확인
                ");"
        );
    }

    /**
     * 채팅 메세지 insert.
     * @param chattingRoom_public_key : 사용자들이 모두 공통적으로 가진 같은 이름의 방 key. 채팅메시지가 어떤 채팅방 메세지인지 확인.
     * @param chattingMessage_send_member_email : 채팅 메세지 보내는 회원의 이메일.
     * @param chattingMessage_send_member_image : 채팅 메세지 보내는 회원의 이미지.
     * @param chattingMessage_send_member_name : 채팅 메세지 보내는 회원의 이름.
     * @param chattingMessage_contents : 채팅 메세지 내용.
     * @param chattingMessage_time : 채팅 메세지 내용.
     * @param chattingMessage_type : 이미지 전송인지(image), 채팅메세지 전송인지(message), etc(채팅방 초대, 채팅방 나가기) 확인
     */
    public void insert(
            String chattingRoom_public_key, String chattingMessage_send_member_email, String chattingMessage_send_member_image,
            String chattingMessage_send_member_name, String chattingMessage_contents, String chattingMessage_time,
            String chattingMessage_type){
        SQLiteDatabase db = getWritableDatabase();
        String query =
                "INSERT INTO chattingMessage VALUES(" +
                        "null, " +
                        "'"+chattingRoom_public_key+"', " +
                        "'"+chattingMessage_send_member_email+"', " +
                        "'"+chattingMessage_send_member_image+"', " +
                        "'"+chattingMessage_send_member_name+"', " +
                        "'"+chattingMessage_contents+"', " +
                        "'"+chattingMessage_time+"', " +
                        "'"+chattingMessage_type+"'" +
                ");";
        db.execSQL(query);
        db.close();
    }

    // 채팅 메세지 목록
    public String selectChattingMessageList(String chattingRoom_public_key){
        SQLiteDatabase db = getReadableDatabase();
        String query =
                "select " +
                    "chattingMessage_send_member_email," +
                    "chattingMessage_send_member_image," +
                    "chattingMessage_send_member_name," +
                    "chattingMessage_contents," +
                    "chattingMessage_time," +
                    "chattingMessage_type " +
                "from chattingMessage where chattingRoom_public_key='"+chattingRoom_public_key+"'";
        Cursor cursor = db.rawQuery(query, null);
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()){
            try {
                JSONObject jsonObject = new JSONObject();
                int index = 0;
                jsonObject.put("chattingMessage_send_member_email", cursor.getString(index++));
                jsonObject.put("chattingMessage_send_member_image", cursor.getString(index++));
                jsonObject.put("chattingMessage_send_member_name", cursor.getString(index++));
                jsonObject.put("chattingMessage_contents", cursor.getString(index++));
                jsonObject.put("chattingMessage_time", cursor.getString(index++));
                jsonObject.put("chattingMessage_type", cursor.getString(index++));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

    // 채팅방에서 사람이 나갔을때 나간 회원의 DB 에서 해당 채팅방에 대한 메세지 정보를 다 지워준다.
    public void delete_chatting_message(String chattingRoom_public_key){
        SQLiteDatabase db = getWritableDatabase();
        String query = "delete from chattingMessage where chattingRoom_public_key='"+chattingRoom_public_key+"'";
        db.execSQL(query);
        db.close();
    }

























}
