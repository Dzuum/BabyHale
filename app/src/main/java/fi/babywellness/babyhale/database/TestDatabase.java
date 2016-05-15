package fi.babywellness.babyhale.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TestDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TestDatabase.db";

    public static final String DRUGS_TABLE_NAME = "drugs";
    public static final String DRUGS_VNR = "vnr";
    public static final String DRUGS_NAME = "name";
    public static final String DRUGS_EFFECTS = "effects";

    private Context context;

    public TestDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE  " + DRUGS_TABLE_NAME + " ("
                + DRUGS_VNR + " TEXT PRIMARY KEY, "
                + DRUGS_NAME + " TEXT UNIQUE COLLATE NOCASE, "
                + DRUGS_EFFECTS + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DRUGS_TABLE_NAME);
        onCreate(db);
    }

    public void insertNew(TestDatabaseEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DRUGS_VNR, entry.getVnr());
        values.put(DRUGS_NAME, entry.getName());
        values.put(DRUGS_EFFECTS, entry.getEffects());

        db.insert(DRUGS_TABLE_NAME, null, values);
        db.close();
    }

    public TestDatabaseEntry getWithNameOrVnr(String text) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (!doesTableExist(db, DRUGS_TABLE_NAME))
            return null;

        String selectQuery = "SELECT * FROM " + DRUGS_TABLE_NAME + " WHERE "
                + DRUGS_NAME + "=? OR " + DRUGS_VNR + "=?";
        Cursor cursor = db.rawQuery(selectQuery, new String[] { text, text });

        TestDatabaseEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new TestDatabaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            cursor.close();
        }

        db.close();

        return entry;
    }

    public List<TestDatabaseEntry> getAllEntries() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (!doesTableExist(db, DRUGS_TABLE_NAME))
            return null;

        List<TestDatabaseEntry> entryList = new ArrayList<TestDatabaseEntry>();
        String selectQuery = "SELECT  * FROM " + DRUGS_TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                TestDatabaseEntry contact = new TestDatabaseEntry();
                contact.setVnr(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setEffects(cursor.getString(2));

                entryList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return entryList;
    }

    public boolean doesTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME);
    }
}
