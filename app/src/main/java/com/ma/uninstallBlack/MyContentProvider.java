package com.ma.uninstallBlack;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ma.uninstallBlack.database.PersonDataBase;
import com.ma.uninstallBlack.util.Person;

public class MyContentProvider extends ContentProvider {
    public final static String AUTHORITY ="com.ma.blackuninstaller.switchprovider";
    private Person.PersonDao mPersonDao;

    public MyContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        return mPersonDao.deletePerson(new Person());
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        PersonDataBase personDatabase = Room.databaseBuilder(getContext(),PersonDataBase.class,"person").build();
        mPersonDao = personDatabase.mPD();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
       return mPersonDao.updatePerson(new Person());
    }
}