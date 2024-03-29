package com.tobot.map.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;

/**
 * @author houdeming
 * @date 2018/3/15
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String ID = "id";
    public static final String MAP_NAME = "mapName";
    public static final String LOCATION_NUMBER = "locationNumber";
    public static final String LOCATION_NAME_CHINA = "locationNameChina";
    public static final String LOCATION_NAME_ENGLISH = "locationNameEnglish";
    public static final String CONTENT = "content";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String YAW = "yaw";
    public static final String TYPE = "type";
    public static final String SENSOR_STATUS = "sensorStatus";
    public static final String START_X = "startX";
    public static final String START_Y = "startY";
    public static final String END_X = "endX";
    public static final String END_Y = "endY";
    public static final String IP = "ip";
    public static final String ROUTE_NAME = "routeName";
    public static final String CODE = "code";
    public static final String TIME = "time";
    public static final String COUNT = "count";

    private static final String DATABASE_NAME = "map.db";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_IP = "ip";
    public static final String TABLE_LOCATION = "location";
    public static final String TABLE_ROUTE = "route";
    public static final String TABLE_ROUTE_DETAIL = "routeDetail";
    public static final String TABLE_RECORD_WARNING = "recordWarning";
    public static final String TABLE_RECORD_INFO = "recordInfo";

    private static final String IP_CREATE = "create table " + TABLE_IP +
            "("
            + ID + " integer primary key autoincrement,"
            + IP + " text"
            + " );";

    private static final String LOCATION_CREATE = "create table " + TABLE_LOCATION +
            "("
            + ID + " integer primary key autoincrement,"
            + MAP_NAME + " text,"
            + LOCATION_NUMBER + " text,"
            + LOCATION_NAME_CHINA + " text,"
            + LOCATION_NAME_ENGLISH + " text,"
            + CONTENT + " text,"
            + X + " real,"
            + Y + " real,"
            + YAW + " real,"
            + TYPE + " integer,"
            + SENSOR_STATUS + " integer,"
            + START_X + " real,"
            + START_Y + " real,"
            + END_X + " real,"
            + END_Y + " real"
            + " );";

    private static final String ROUTE_CREATE = "create table " + TABLE_ROUTE +
            "("
            + ID + " integer primary key autoincrement,"
            + ROUTE_NAME + " text not null,"
            + TYPE + " integer,"
            + CONTENT + " text"
            + " );";

    private static final String ROUTE_DETAIL_CREATE = "create table " + TABLE_ROUTE_DETAIL +
            "("
            + ID + " integer primary key autoincrement,"
            + ROUTE_NAME + " text not null,"
            + MAP_NAME + " text,"
            + LOCATION_NUMBER + " text,"
            + LOCATION_NAME_CHINA + " text,"
            + LOCATION_NAME_ENGLISH + " text,"
            + CONTENT + " text,"
            + X + " real,"
            + Y + " real,"
            + YAW + " real,"
            + TYPE + " integer,"
            + SENSOR_STATUS + " integer,"
            + START_X + " real,"
            + START_Y + " real,"
            + END_X + " real,"
            + END_Y + " real"
            + " );";

    private static final String RECORD_WARNING_CREATE = "create table " + TABLE_RECORD_WARNING +
            "("
            + ID + " integer primary key autoincrement,"
            + TYPE + " integer,"
            + CODE + " integer,"
            + COUNT + " integer,"
            + TIME + " text,"
            + CONTENT + " text"
            + " );";

    private static final String RECORD_INFO_CREATE = "create table " + TABLE_RECORD_INFO +
            "("
            + ID + " integer primary key autoincrement,"
            + TYPE + " integer,"
            + CODE + " integer,"
            + TIME + " text,"
            + CONTENT + " text"
            + " );";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(IP_CREATE);
        db.execSQL(LOCATION_CREATE);
        // 默认版本从1开始
        onUpgrade(db, 1, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库升级
        Logger.i(BaseConstant.TAG, "oldVersion=" + oldVersion + ",newVersion=" + newVersion);
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 1:
                    addTaskTab(db);
                    break;
                case 2:
                    addRecordTab(db);
                    break;
                default:
                    break;
            }
        }
    }

    private void addTaskTab(SQLiteDatabase db) {
        db.execSQL(ROUTE_CREATE);
        db.execSQL(ROUTE_DETAIL_CREATE);
    }

    private void addRecordTab(SQLiteDatabase db) {
        db.execSQL(RECORD_WARNING_CREATE);
        db.execSQL(RECORD_INFO_CREATE);
    }
}
