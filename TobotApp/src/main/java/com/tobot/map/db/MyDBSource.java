package com.tobot.map.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.tobot.map.entity.RouteBean;
import com.tobot.slam.data.LocationBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author houdeming
 * @date 2018/6/28
 */
public class MyDBSource {
    private static MyDBSource sDBSource;
    private MySQLiteHelper mHelper;

    private MyDBSource(Context context) {
        mHelper = new MySQLiteHelper(context);
    }

    public static MyDBSource getInstance(Context context) {
        if (sDBSource == null) {
            synchronized (MyDBSource.class) {
                if (sDBSource == null) {
                    sDBSource = new MyDBSource(context.getApplicationContext());
                }
            }
        }

        return sDBSource;
    }

    public synchronized void insertLocation(LocationBean bean) {
        if (bean != null) {
            SQLiteDatabase database = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.MAP_NAME, bean.getMapName());
            values.put(MySQLiteHelper.LOCATION_NUMBER, bean.getLocationNumber());
            values.put(MySQLiteHelper.LOCATION_NAME_CHINA, bean.getLocationNameChina());
            values.put(MySQLiteHelper.LOCATION_NAME_ENGLISH, bean.getLocationNameEnglish());
            values.put(MySQLiteHelper.CONTENT, bean.getContent());
            values.put(MySQLiteHelper.X, bean.getX());
            values.put(MySQLiteHelper.Y, bean.getY());
            values.put(MySQLiteHelper.YAW, bean.getYaw());
            values.put(MySQLiteHelper.TYPE, bean.getType());
            values.put(MySQLiteHelper.SENSOR_STATUS, bean.getSensorStatus());
            values.put(MySQLiteHelper.START_X, bean.getStartX());
            values.put(MySQLiteHelper.START_Y, bean.getStartY());
            values.put(MySQLiteHelper.END_X, bean.getEndX());
            values.put(MySQLiteHelper.END_Y, bean.getEndY());
            database.insert(MySQLiteHelper.TABLE_LOCATION, null, values);
            mHelper.close();
        }
    }

    public synchronized void insertLocationList(List<LocationBean> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        SQLiteDatabase database = mHelper.getWritableDatabase();
        String sql = "insert into " + MySQLiteHelper.TABLE_LOCATION + "("
                + MySQLiteHelper.MAP_NAME + ","
                + MySQLiteHelper.LOCATION_NUMBER + ","
                + MySQLiteHelper.LOCATION_NAME_CHINA + ","
                + MySQLiteHelper.LOCATION_NAME_ENGLISH + ","
                + MySQLiteHelper.CONTENT + ","
                + MySQLiteHelper.X + ","
                + MySQLiteHelper.Y + ","
                + MySQLiteHelper.YAW + ","
                + MySQLiteHelper.TYPE + ","
                + MySQLiteHelper.SENSOR_STATUS + ","
                + MySQLiteHelper.START_X + ","
                + MySQLiteHelper.START_Y + ","
                + MySQLiteHelper.END_X + ","
                + MySQLiteHelper.END_Y + ") "
                + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        try {
            for (LocationBean bean : dataList) {
                String mapName = bean.getMapName();
                // 为空的时候要做空处理，不然当空的时候读到的数据会是上一次的数据
                statement.bindString(1, !TextUtils.isEmpty(mapName) ? mapName : "");
                String number = bean.getLocationNumber();
                statement.bindString(2, !TextUtils.isEmpty(number) ? number : "");
                String nameChina = bean.getLocationNameChina();
                statement.bindString(3, !TextUtils.isEmpty(nameChina) ? nameChina : "");
                String nameEnglish = bean.getLocationNameEnglish();
                statement.bindString(4, !TextUtils.isEmpty(nameEnglish) ? nameEnglish : "");
                String content = bean.getContent();
                statement.bindString(5, !TextUtils.isEmpty(content) ? content : "");
                statement.bindDouble(6, bean.getX());
                statement.bindDouble(7, bean.getY());
                statement.bindDouble(8, bean.getYaw());
                statement.bindLong(9, bean.getType());
                statement.bindLong(10, bean.getSensorStatus());
                statement.bindDouble(11, bean.getStartX());
                statement.bindDouble(12, bean.getStartY());
                statement.bindDouble(13, bean.getEndX());
                statement.bindDouble(14, bean.getEndY());
                statement.executeInsert();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            mHelper.close();
        }
    }

    public synchronized void deleteLocation(String locationNumber) {
        if (TextUtils.isEmpty(locationNumber)) {
            return;
        }

        SQLiteDatabase database = mHelper.getWritableDatabase();
        // 如果数据库中有该数据返回1，否则返回0
        database.delete(MySQLiteHelper.TABLE_LOCATION, MySQLiteHelper.LOCATION_NUMBER + "=?", new String[]{locationNumber});
        mHelper.close();
    }

    public synchronized void deleteAllLocation() {
        deleteAll(MySQLiteHelper.TABLE_LOCATION);
    }

    public synchronized void updateLocation(String locationNumber, LocationBean bean) {
        if (bean != null) {
            SQLiteDatabase database = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.MAP_NAME, bean.getMapName());
            values.put(MySQLiteHelper.LOCATION_NUMBER, bean.getLocationNumber());
            values.put(MySQLiteHelper.LOCATION_NAME_CHINA, bean.getLocationNameChina());
            values.put(MySQLiteHelper.LOCATION_NAME_ENGLISH, bean.getLocationNameEnglish());
            values.put(MySQLiteHelper.CONTENT, bean.getContent());
            values.put(MySQLiteHelper.X, bean.getX());
            values.put(MySQLiteHelper.Y, bean.getY());
            values.put(MySQLiteHelper.YAW, bean.getYaw());
            values.put(MySQLiteHelper.TYPE, bean.getType());
            values.put(MySQLiteHelper.SENSOR_STATUS, bean.getSensorStatus());
            values.put(MySQLiteHelper.START_X, bean.getStartX());
            values.put(MySQLiteHelper.START_Y, bean.getStartY());
            values.put(MySQLiteHelper.END_X, bean.getEndX());
            values.put(MySQLiteHelper.END_Y, bean.getEndY());
            String whereClause = MySQLiteHelper.LOCATION_NUMBER + "=?";
            String[] whereArgs = {locationNumber};
            database.update(MySQLiteHelper.TABLE_LOCATION, values, whereClause, whereArgs);
            mHelper.close();
        }
    }

    public synchronized LocationBean queryLocation(String locationNumber) {
        return queryLocation(MySQLiteHelper.LOCATION_NUMBER, locationNumber);
    }

    public synchronized LocationBean queryLocationByChineseName(String locationName) {
        return queryLocation(MySQLiteHelper.LOCATION_NAME_CHINA, locationName);
    }

    public synchronized LocationBean queryLocationByEnglishName(String locationName) {
        return queryLocation(MySQLiteHelper.LOCATION_NAME_ENGLISH, locationName);
    }

    public synchronized List<LocationBean> queryLocationList() {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        List<LocationBean> dataList = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION, getColumns(), null, null, null, null, null);
        cursor.moveToFirst();
        LocationBean bean = new LocationBean();
        while (!cursor.isAfterLast()) {
            bean = bean.clone();
            bean.setMapName(cursor.getString(1));
            bean.setLocationNumber(cursor.getString(2));
            bean.setLocationNameChina(cursor.getString(3));
            bean.setLocationNameEnglish(cursor.getString(4));
            bean.setContent(cursor.getString(5));
            bean.setX(cursor.getFloat(6));
            bean.setY(cursor.getFloat(7));
            bean.setYaw(cursor.getFloat(8));
            bean.setType(cursor.getInt(9));
            bean.setSensorStatus(cursor.getInt(10));
            bean.setStartX(cursor.getFloat(11));
            bean.setStartY(cursor.getFloat(12));
            bean.setEndX(cursor.getFloat(13));
            bean.setEndY(cursor.getFloat(14));
            dataList.add(bean);
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();
        return dataList;
    }

    public synchronized List<LocationBean> queryLocationList(int type) {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        List<LocationBean> dataList = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION, getColumns(), MySQLiteHelper.TYPE + "=?",
                new String[]{String.valueOf(type)}, null, null, null);
        cursor.moveToFirst();
        LocationBean bean = new LocationBean();
        while (!cursor.isAfterLast()) {
            bean = bean.clone();
            bean.setMapName(cursor.getString(1));
            bean.setLocationNumber(cursor.getString(2));
            bean.setLocationNameChina(cursor.getString(3));
            bean.setLocationNameEnglish(cursor.getString(4));
            bean.setContent(cursor.getString(5));
            bean.setX(cursor.getFloat(6));
            bean.setY(cursor.getFloat(7));
            bean.setYaw(cursor.getFloat(8));
            bean.setType(cursor.getInt(9));
            bean.setSensorStatus(cursor.getInt(10));
            bean.setStartX(cursor.getFloat(11));
            bean.setStartY(cursor.getFloat(12));
            bean.setEndX(cursor.getFloat(13));
            bean.setEndY(cursor.getFloat(14));
            dataList.add(bean);
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();
        return dataList;
    }

    private LocationBean queryLocation(String selection, String selectionArg) {
        if (TextUtils.isEmpty(selectionArg)) {
            return null;
        }

        LocationBean bean = null;
        SQLiteDatabase database = mHelper.getWritableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION, null, selection + "=?",
                new String[]{selectionArg}, null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            bean = new LocationBean();
            bean.setMapName(cursor.getString(1));
            bean.setLocationNumber(cursor.getString(2));
            bean.setLocationNameChina(cursor.getString(3));
            bean.setLocationNameEnglish(cursor.getString(4));
            bean.setContent(cursor.getString(5));
            bean.setX(cursor.getFloat(6));
            bean.setY(cursor.getFloat(7));
            bean.setYaw(cursor.getFloat(8));
            bean.setType(cursor.getInt(9));
            bean.setSensorStatus(cursor.getInt(10));
            bean.setStartX(cursor.getFloat(11));
            bean.setStartY(cursor.getFloat(12));
            bean.setEndX(cursor.getFloat(13));
            bean.setEndY(cursor.getFloat(14));
        }
        cursor.close();
        mHelper.close();
        return bean;
    }

    private String[] getColumns() {
        return new String[]{
                MySQLiteHelper.ID,
                MySQLiteHelper.MAP_NAME,
                MySQLiteHelper.LOCATION_NUMBER,
                MySQLiteHelper.LOCATION_NAME_CHINA,
                MySQLiteHelper.LOCATION_NAME_ENGLISH,
                MySQLiteHelper.CONTENT,
                MySQLiteHelper.X,
                MySQLiteHelper.Y,
                MySQLiteHelper.YAW,
                MySQLiteHelper.TYPE,
                MySQLiteHelper.SENSOR_STATUS,
                MySQLiteHelper.START_X,
                MySQLiteHelper.START_Y,
                MySQLiteHelper.END_X,
                MySQLiteHelper.END_Y};
    }

    public synchronized void insertIp(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            SQLiteDatabase database = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.IP, ip);
            database.insert(MySQLiteHelper.TABLE_IP, null, values);
            mHelper.close();
        }
    }

    public synchronized void deleteAllIp() {
        deleteAll(MySQLiteHelper.TABLE_IP);
    }

    public synchronized List<String> queryIpList() {
        List<String> dataList = new ArrayList<>();
        SQLiteDatabase database = mHelper.getWritableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_IP, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dataList.add(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();
        return dataList;
    }

    public synchronized String queryIp(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            String content = "";
            SQLiteDatabase database = mHelper.getWritableDatabase();
            String whereClause = MySQLiteHelper.IP + "=?";
            Cursor cursor = database.query(MySQLiteHelper.TABLE_IP, null, whereClause,
                    new String[]{ip}, null, null, null);
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                content = cursor.getString(1);
            }
            cursor.close();
            mHelper.close();
            return content;
        }

        return ip;
    }

    public synchronized void insertRoute(RouteBean bean) {
        if (bean != null) {
            SQLiteDatabase database = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.ROUTE_NAME, bean.getRouteName());
            values.put(MySQLiteHelper.TYPE, bean.getType());
            values.put(MySQLiteHelper.CONTENT, bean.getContent());
            database.insert(MySQLiteHelper.TABLE_ROUTE, null, values);
            mHelper.close();
        }
    }

    public synchronized void deleteAllRoute() {
        deleteAll(MySQLiteHelper.TABLE_ROUTE);
    }

    public synchronized void deleteRoute(String routeName) {
        deleteRoute(routeName, MySQLiteHelper.TABLE_ROUTE);
    }

    private void deleteRoute(String routeName, String tab) {
        if (TextUtils.isEmpty(routeName)) {
            return;
        }

        SQLiteDatabase database = mHelper.getWritableDatabase();
        String whereClause = MySQLiteHelper.ROUTE_NAME + "=?";
        String[] whereArgs = {routeName};
        database.delete(tab, whereClause, whereArgs);
        mHelper.close();
    }

    public synchronized void updateRoute(String routeName, RouteBean bean) {
        if (bean != null) {
            SQLiteDatabase database = mHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.ROUTE_NAME, bean.getRouteName());
            values.put(MySQLiteHelper.TYPE, bean.getType());
            values.put(MySQLiteHelper.CONTENT, bean.getContent());
            String whereClause = MySQLiteHelper.ROUTE_NAME + "=?";
            String[] whereArgs = {routeName};
            database.update(MySQLiteHelper.TABLE_ROUTE, values, whereClause, whereArgs);
            mHelper.close();
        }
    }

    public synchronized List<RouteBean> queryRouteList() {
        List<RouteBean> beanList = new ArrayList<>();
        SQLiteDatabase database = mHelper.getWritableDatabase();
        String orderBy = MySQLiteHelper.ID + " desc";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ROUTE, null, null,
                null, null, null, orderBy);
        cursor.moveToFirst();
        RouteBean bean = new RouteBean();
        while (!cursor.isAfterLast()) {
            bean = bean.clone();
            bean.setRouteName(cursor.getString(1));
            bean.setType(cursor.getInt(2));
            bean.setContent(cursor.getString(3));
            beanList.add(bean);
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();
        return beanList;
    }

    public synchronized RouteBean queryRoute(String routeName) {
        if (TextUtils.isEmpty(routeName)) {
            return null;
        }

        RouteBean bean = null;
        SQLiteDatabase database = mHelper.getWritableDatabase();
        String whereClause = MySQLiteHelper.ROUTE_NAME + "=?";
        String[] selectionArgs = {routeName};
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ROUTE, null, whereClause,
                selectionArgs, null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            bean = new RouteBean();
            bean.setRouteName(cursor.getString(1));
            bean.setType(cursor.getInt(2));
            bean.setContent(cursor.getString(3));
        }
        cursor.close();
        mHelper.close();
        return bean;
    }

    public synchronized void insertRouteDetail(String routeName, List<LocationBean> data) {
        if (data != null && !data.isEmpty()) {
            SQLiteDatabase database = mHelper.getWritableDatabase();
            String sql = "insert into " + MySQLiteHelper.TABLE_ROUTE_DETAIL + "("
                    + MySQLiteHelper.ROUTE_NAME + ","
                    + MySQLiteHelper.MAP_NAME + ","
                    + MySQLiteHelper.LOCATION_NUMBER + ","
                    + MySQLiteHelper.LOCATION_NAME_CHINA + ","
                    + MySQLiteHelper.LOCATION_NAME_ENGLISH + ","
                    + MySQLiteHelper.CONTENT + ","
                    + MySQLiteHelper.X + ","
                    + MySQLiteHelper.Y + ","
                    + MySQLiteHelper.YAW + ","
                    + MySQLiteHelper.TYPE + ","
                    + MySQLiteHelper.SENSOR_STATUS + ","
                    + MySQLiteHelper.START_X + ","
                    + MySQLiteHelper.START_Y + ","
                    + MySQLiteHelper.END_X + ","
                    + MySQLiteHelper.END_Y + ") "
                    + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            SQLiteStatement statement = database.compileStatement(sql);
            database.beginTransaction();
            try {
                for (LocationBean bean : data) {
                    statement.bindString(1, routeName);
                    String mapName = bean.getMapName();
                    // 为空的时候要做空处理，不然当空的时候读到的数据会是上一次的数据
                    statement.bindString(2, !TextUtils.isEmpty(mapName) ? mapName : "");
                    String number = bean.getLocationNumber();
                    statement.bindString(3, !TextUtils.isEmpty(number) ? number : "");
                    String nameChina = bean.getLocationNameChina();
                    statement.bindString(4, !TextUtils.isEmpty(nameChina) ? nameChina : "");
                    String nameEnglish = bean.getLocationNameEnglish();
                    statement.bindString(5, !TextUtils.isEmpty(nameEnglish) ? nameEnglish : "");
                    String content = bean.getContent();
                    statement.bindString(6, !TextUtils.isEmpty(content) ? content : "");
                    statement.bindDouble(7, bean.getX());
                    statement.bindDouble(8, bean.getY());
                    statement.bindDouble(9, bean.getYaw());
                    statement.bindLong(10, bean.getType());
                    statement.bindLong(11, bean.getSensorStatus());
                    statement.bindDouble(12, bean.getStartX());
                    statement.bindDouble(13, bean.getStartY());
                    statement.bindDouble(14, bean.getEndX());
                    statement.bindDouble(15, bean.getEndY());
                    statement.executeInsert();
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                database.endTransaction();
                mHelper.close();
            }
        }
    }

    public synchronized void deleteAllRouteDetail() {
        deleteAll(MySQLiteHelper.TABLE_ROUTE_DETAIL);
    }

    public synchronized void deleteRouteDetail(String routeName) {
        deleteRoute(routeName, MySQLiteHelper.TABLE_ROUTE_DETAIL);
    }

    public synchronized List<LocationBean> queryRouteDetailList(String routeName) {
        List<LocationBean> beanList = new ArrayList<>();
        if (TextUtils.isEmpty(routeName)) {
            return beanList;
        }

        SQLiteDatabase database = mHelper.getWritableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ROUTE_DETAIL, null, MySQLiteHelper.ROUTE_NAME + "=?",
                new String[]{routeName}, null, null, null);
        cursor.moveToFirst();
        LocationBean bean = new LocationBean();
        while (!cursor.isAfterLast()) {
            bean = bean.clone();
            bean.setMapName(cursor.getString(2));
            bean.setLocationNumber(cursor.getString(3));
            bean.setLocationNameChina(cursor.getString(4));
            bean.setLocationNameEnglish(cursor.getString(5));
            bean.setContent(cursor.getString(6));
            bean.setX(cursor.getFloat(7));
            bean.setY(cursor.getFloat(8));
            bean.setYaw(cursor.getFloat(9));
            bean.setType(cursor.getInt(10));
            bean.setSensorStatus(cursor.getInt(11));
            bean.setStartX(cursor.getFloat(12));
            bean.setStartY(cursor.getFloat(13));
            bean.setEndX(cursor.getFloat(14));
            bean.setEndY(cursor.getFloat(15));
            beanList.add(bean);
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();
        return beanList;
    }

    public synchronized void clearRouteId() {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        resetId(database, MySQLiteHelper.TABLE_ROUTE);
        resetId(database, MySQLiteHelper.TABLE_ROUTE_DETAIL);
        mHelper.close();
    }

    private void deleteAll(String tab) {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        database.delete(tab, null, null);
        resetId(database, tab);
        mHelper.close();
    }

    private void resetId(SQLiteDatabase database, String tableName) {
        try {
            // sqlite_sequence 系统自生成的序列表，用来记录id 增长
            String sql = "UPDATE sqlite_sequence SET seq = 0 WHERE name =" + "'" + tableName + "'";
            database.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
