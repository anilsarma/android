package com.example.asarma.njrails;


import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.BufferedReader;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by asarma on 11/2/2017.
 */

class SQLHelper extends SQLiteOpenHelper {
    AssetManager amgr;
    SQLHelper(Context context) {
        super(context, "main.db", null, 1);
        amgr = context.getAssets();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL("create table todos (_id integer primary key autoincrement, title text, priority integer)");
      //  RailHelper.create_tables(db);

    }

    public void update_tables(SQLiteDatabase db) {
        String tables[] = {"trips", "stops", "routes", "calendar_dates", "stop_times", "shapes"};
        for (int i = 0; i < tables.length; i++) {
            System.out.println("workiing " + tables[i]);
            ArrayList<HashMap<String, Object>> df = read_csv( tables[i] + ".txt");
            insert(db, tables[i], df);
            System.out.println("done " + tables[i]);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public  ArrayList<HashMap<String, Object>> read_csv(String filename )
    {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String cols[] = null;
        ArrayList<HashMap<String, Object>> result = new   ArrayList<HashMap<String, Object>>();
        try {
            br = new BufferedReader(new InputStreamReader(amgr.open(filename)) );
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] tokens = line.split(cvsSplitBy);
                if (cols == null ) {
                    cols = tokens;
                    continue;
                }
                HashMap<String, Object> data=new HashMap<String, Object>();
                for (int i = 0; i < tokens.length; i++) {
                    Object v = tokens[i];
                    try {
                        v = Integer.parseInt(tokens[i] );
                    }
                    catch (NumberFormatException e) {
                        try {
                            v = Double.parseDouble(tokens[i]);
                        }
                        catch (NumberFormatException e1)
                        {

                        }
                    }
                    //System.out.println(v + " type=" + v.getClass());
                    data.put( cols[i], v);
                }
                result.add( data );

                //System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    public static void update_user_pref( SQLiteDatabase db, String name, String value, Date dt )
    {
        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        HashMap<String, Object> rec  = new HashMap<>();
        rec.put("name", name);
        rec.put("value", value);
        rec.put("date", Utils.formatToLocalDateTime(dt));
        data.add(rec);
        query( db, "delete from user_pref where name like '%" + name + "%'" );
        insert(db, "user_pref", data);
    }
    public static String get_user_pref_value(SQLiteDatabase db, String name, String default_value ) {
        try {
            ArrayList<HashMap<String, Object>> data = query(db, "select * from user_pref where name like '%" + name + "%'");
            if (data.isEmpty()) {
                return default_value;
                //return null;
            }
            return (String) data.get(0).get("value");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return default_value;
    }
    public static void insert(SQLiteDatabase db, String table_name, ArrayList<HashMap<String, Object>> df )
    {

        if (df.isEmpty()) {
            return;
        }
        String cols = "";
        ArrayList<String> columns = new ArrayList<String>();
        String values = "";
        for(Iterator<String> iterator = df.get(0).keySet().iterator(); iterator.hasNext(); ) {
            String key =  iterator.next();
            if ( !cols.isEmpty()) {
                cols +=",";
                values +=",";
            }
            cols += key;
            values += "?";
            columns.add(key);
        }
        String sql = "INSERT INTO " + table_name  + "(" + cols + ") VALUES(" + values + ")";
        try {
            SQLiteStatement pstmt = db.compileStatement(sql);
            //conn.setAutoCommit(false);
            int count = 0;
            db.beginTransaction();
            for(Iterator<HashMap<String, Object>> iterator = df.iterator(); iterator.hasNext(); ) {
                HashMap<String, Object> data = iterator.next();
                for (int i = 0; i < columns.size(); i++) {
                    String key = columns.get(i);
                    Object v = data.get(key);
                    //System.err.println("Data -" +  key + " " + v + " type=" + v.getClass());
                    if ( v instanceof  Integer) {
                        pstmt.bindLong(i+1, (Integer)v);
                    } else if ( v instanceof  Double) {
                        pstmt.bindDouble(i+1, (Double)v);
                    } else {
                        pstmt.bindString(i+1, (String)v);
                    }
                }
                pstmt.execute();
            }
            db.setTransactionSuccessful();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    static public int check_table(SQLiteDatabase db, String table)
    {
        String sql = "select count(*) from " + table;
        Cursor c = db.rawQuery(sql, null);
        return Utils.parseCursor(c).size();
    }

    static public HashMap<String, Object> get_station(SQLiteDatabase db, String name)
    {
        String sql = "select * from stops where stop_name like '%" + name + "%'";
        Cursor c = db.rawQuery(sql, null);
        ArrayList<HashMap<String, Object>> result = Utils.parseCursor(c);
        if (result.isEmpty()){
            return new HashMap<>();
        }
        return result.get(0);
    }


    static public ArrayList<HashMap<String, Object>> query(SQLiteDatabase db, String sql)
    {
        //String sql = "select * from routes";
        ArrayList<HashMap<String, Object>> curser = Utils.parseCursor(db.rawQuery(sql, null));
        return curser;
    }

    static public String[] get_values(SQLiteDatabase db, String sql, String key )
    {
        ArrayList<HashMap<String, Object>> data = SQLHelper.query(db, sql);
        System.out.println(data);
        ArrayList<String> njtr = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String d = (String)data.get(i).get(key).toString();
            njtr.add(d);
        }
        return njtr.toArray(new String[]{});
    }
    static public Cursor getRoutes(SQLiteDatabase db, String from, String to, int date )
    {
        int start_stop_id = (int)get_station(db,from).get("stop_id");
        int stop_stop_id = (int)get_station(db, to).get("stop_id");
        //and departure_time > '18:10:00' and departure_time < '20:00:00'
        //date = 20171102;
System.out.println("output " + date + " stop:" + start_stop_id + " end:"+ stop_stop_id);
        String sql_start = "select * from stop_times st, trips t where stop_id in (  {start_station}) and  st.trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = {start_station} and st.arrival_time < st2.arrival_time and st2.stop_id={stop_station})and st.trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '{travel_date}') ) and st.trip_id = t.trip_id order by departure_time";
        // for travel time.
        String sql_destination = "select trip_id, shape_dist_traveled, arrival_time as destination_time, stop_id as destination_stop from (select * from stop_times where trip_id in (select st.trip_id from stop_times st, trips t where stop_id in (  {start_station})  and  st.trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = {start_station} and st.arrival_time < st2.arrival_time and st2.stop_id={stop_station})and st.trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '{travel_date}') ) and st.trip_id = t.trip_id order by departure_time) and stop_id ={stop_station}  order by stop_sequence asc)   group by trip_id";
        //String sql_destination = "select trip_id, shape_dist_traveled, arrival_time as destination_time, stop_id as destination_stop from (select * from stop_times where trip_id in (select st.trip_id from stop_times st, trips t where stop_id in (  {start_station})  and  st.trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = {start_station} and st.arrival_time < st2.arrival_time and st2.stop_id={stop_station})and st.trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '{travel_date}') ) and st.trip_id = t.trip_id   order by departure_time)  order by stop_sequence asc) group by trip_id";
        //String sql_destination = "select trip_id, shape_dist_traveled, arrival_time as destination_time, stop_id as destination_stop from stops where stop_id = {stop_station}";
        String sql_routes = "select * from routes ";
        String sql = "select * from (" + sql_start + ") as st, (" + sql_destination + ") as dt, routes as rt";
        sql += " where dt.trip_id = st.trip_id and st.route_id=rt.route_id";

        // all stops in a line.
        // select * from stop_times where trip_id = 2547 order by stop_sequence;

        sql = sql.replace("{start_station}", "" + start_stop_id);
        sql = sql.replace("{stop_station}", "" + stop_stop_id);
        sql = sql.replace("{travel_date}", "" + date);

        Cursor curser = db.rawQuery(sql, null);
        return curser;
    }
}