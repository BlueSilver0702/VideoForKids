package com.cantecegradinita.utils;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelperFraction extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "VideoForKids.db";
	public static final String TABLE_NAME = "tbl_video";
	public static final String CAPTION = "c_caption";
	public static final String IMAGE = "c_img";
	public static final String NAME = "c_name";
	public static final String PAID = "c_paid";
	public static final String VIDEO = "c_video";
	public static final String DOWNLOAD = "c_download";
	public static final String THUMB_DOWNLOAD = "c_thumb";
	public static final String FAIL = "c_fail";
	public static final String IAP = "c_iap";
	public static final String ETC = "c_etc";
	
	public DBHelperFraction(Context context) {
	    super(context, DATABASE_NAME, null, 1);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(
			      "create table " + TABLE_NAME +
			      " (c_caption text, c_img text,c_name text,c_paid boolean,c_video text,c_download boolean,c_thumb boolean, c_fail boolean, c_iap boolean, c_etc boolean)"
			      );
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(db);
	  }
	
	   public boolean insertData (VideoDB video)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();

	      contentValues.put(CAPTION, video.caption);
	      contentValues.put(IMAGE, video.image);
	      contentValues.put(NAME, video.name);
	      contentValues.put(PAID, video.paid);
	      contentValues.put(VIDEO, video.video);
	      contentValues.put(DOWNLOAD, video.download);
	      contentValues.put(THUMB_DOWNLOAD, video.thumb);
	      contentValues.put(FAIL, video.fail);
	      contentValues.put(IAP, video.iap);
	      contentValues.put(ETC, video.etc);
	      
	      db.insert(TABLE_NAME, null, contentValues);
	      return true;
	   }
	   
	   public VideoDB getData(String name){
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+TABLE_NAME+" where c_name='"+name+"'", null );
	      if (res.getCount() == 0) return null;
	      VideoDB row = new VideoDB();
	      res.moveToFirst();
	      row.caption = res.getString(res.getColumnIndex(CAPTION));
    	  row.image = res.getString(res.getColumnIndex(IMAGE));
    	  row.name = res.getString(res.getColumnIndex(NAME));
    	  if(res.getString(res.getColumnIndex(PAID)).trim().equals("1"))
    	  		row.paid = true;
    	  	else
    	  		row.paid=false;
    	  row.video = res.getString(res.getColumnIndex(VIDEO));
    	  if(res.getString(res.getColumnIndex(DOWNLOAD)).trim().equals("1"))
    	  		row.download = true;
    	  	else
    	  		row.download=false;
    	  if(res.getString(res.getColumnIndex(THUMB_DOWNLOAD)).trim().equals("1"))
    	  		row.thumb = true;
    	  	else
    	  		row.thumb=false;
    	  if(res.getString(res.getColumnIndex(FAIL)).trim().equals("1"))
    	  		row.fail = true;
    	  	else
    	  		row.fail=false;
    	  if(res.getString(res.getColumnIndex(IAP)).trim().equals("1"))
  	  		 	row.iap = true;
  	  	  	else
  	  		  	row.iap=false;
    	  if(res.getString(res.getColumnIndex(ETC)).trim().equals("1"))
  	  		  	row.etc = true;
  	  	  	else
  	  		  	row.etc=false;
	      return row;
	   }
	   
	   public ArrayList<VideoDB> searchData(String search, boolean download){
		   ArrayList<VideoDB> array_list=new ArrayList<VideoDB>();
	      VideoDB row=null;
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res = null;
	      if (download)
	    	  res =  db.rawQuery( "select * from "+TABLE_NAME+" where c_download=1 and c_name like '%"+search+"%'", null );
	      else
	    	  res =  db.rawQuery( "select * from "+TABLE_NAME+" where c_name like '%"+search+"%'", null );
	      res.moveToFirst();
	      while(res.isAfterLast() == false){
	    	  row=new VideoDB();
	    	  row.caption = res.getString(res.getColumnIndex(CAPTION));
	    	  row.image = res.getString(res.getColumnIndex(IMAGE));
	    	  row.name = res.getString(res.getColumnIndex(NAME));
	    	  if(res.getString(res.getColumnIndex(PAID)).trim().equals("1"))
	    	  		row.paid = true;
	    	  	else
	    	  		row.paid=false;
	    	  row.video = res.getString(res.getColumnIndex(VIDEO));
	    	  if(res.getString(res.getColumnIndex(DOWNLOAD)).trim().equals("1"))
	    	  		row.download = true;
	    	  	else
	    	  		row.download=false;
	    	  if(res.getString(res.getColumnIndex(THUMB_DOWNLOAD)).trim().equals("1"))
	    	  		row.thumb = true;
	    	  	else
	    	  		row.thumb=false;
	    	  if(res.getString(res.getColumnIndex(FAIL)).trim().equals("1"))
	    	  		row.fail = true;
	    	  	else
	    	  		row.fail=false;
	    	  if(res.getString(res.getColumnIndex(IAP)).trim().equals("1"))
	    	  		row.iap = true;
	    	  	else
	    	  		row.iap=false;
	    	  if(res.getString(res.getColumnIndex(ETC)).trim().equals("1"))
	    	  		row.etc = true;
	    	  	else
	    	  		row.etc=false;
	    	  array_list.add(row);
	         res.moveToNext();
	      }
	      return array_list;
	   }
	   
	   public int numberOfRows(){
	      SQLiteDatabase db = this.getReadableDatabase();
	      int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
	      return numRows;
	   }
	   
	   
	   public boolean updateData (String name, boolean paid, boolean download, boolean thumb, boolean fail, boolean iap, boolean etc)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      contentValues.put(PAID, paid);
	      contentValues.put(DOWNLOAD, download);
	      contentValues.put(THUMB_DOWNLOAD, thumb);
	      contentValues.put(FAIL, fail);
	      contentValues.put(IAP, iap);
	      contentValues.put(ETC, etc);
	      
	      db.update(TABLE_NAME, contentValues, "c_name = ? ", new String[] { name } );
	      return true;
	   }


	   public Integer deleteData (String name)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      return db.delete(TABLE_NAME, 
	      "c_name = ? ", 
	      new String[] { name });
	   }
	   
	   public boolean emptyTable() 
	   {
		   SQLiteDatabase db = this.getWritableDatabase();
		   db.execSQL("DELETE FROM " + TABLE_NAME + ";");
		   return true;
	   }
	   	   
	   public ArrayList<VideoDB> getAllVideos(boolean downloaded)
	   {
	      ArrayList<VideoDB> array_list=new ArrayList<VideoDB>();
	      VideoDB row=null;
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res = null;
	      if (downloaded)
	    	  res =  db.rawQuery( "select * from "+TABLE_NAME+" where c_download=1", null );
	      else 
	    	  res =  db.rawQuery( "select * from "+TABLE_NAME+"", null );
	      res.moveToFirst();
	      while(res.isAfterLast() == false){
	    	  row=new VideoDB();
	    	  row.caption = res.getString(res.getColumnIndex(CAPTION));
	    	  row.image = res.getString(res.getColumnIndex(IMAGE));
	    	  row.name = res.getString(res.getColumnIndex(NAME));
	    	  if(res.getString(res.getColumnIndex(PAID)).trim().equals("1"))
	    	  		row.paid = true;
	    	  	else
	    	  		row.paid=false;
	    	  row.video = res.getString(res.getColumnIndex(VIDEO));
	    	  if(res.getString(res.getColumnIndex(DOWNLOAD)).trim().equals("1"))
	    	  		row.download = true;
	    	  	else
	    	  		row.download=false;
	    	  if(res.getString(res.getColumnIndex(THUMB_DOWNLOAD)).trim().equals("1"))
	    	  		row.thumb = true;
	    	  	else
	    	  		row.thumb=false;
	    	  if(res.getString(res.getColumnIndex(FAIL)).trim().equals("1"))
	    	  		row.fail = true;
	    	  	else
	    	  		row.fail=false;
	    	  if(res.getString(res.getColumnIndex(IAP)).trim().equals("1"))
	    	  		row.iap = true;
	    	  	else
	    	  		row.iap=false;
	    	  if(res.getString(res.getColumnIndex(ETC)).trim().equals("1"))
	    	  		row.etc = true;
	    	  	else
	    	  		row.etc=false;
	    	  array_list.add(row);
	         res.moveToNext();
	      }
	   return array_list;
	   }
}
