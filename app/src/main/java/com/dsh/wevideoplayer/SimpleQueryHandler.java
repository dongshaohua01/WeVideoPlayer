package com.dsh.wevideoplayer;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.CursorAdapter;

public class SimpleQueryHandler extends AsyncQueryHandler{

	public SimpleQueryHandler(ContentResolver cr) {
		super(cr);
	}
	
	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		super.onQueryComplete(token, cookie, cursor);
		
//		CursorUtil.printCursor(cursor);
		
		if(cookie!=null && cookie instanceof CursorAdapter){
			CursorAdapter adapter = (CursorAdapter) cookie;
			adapter.changeCursor(cursor);//  相当于notifyDatesetChange
		}
	}

}
