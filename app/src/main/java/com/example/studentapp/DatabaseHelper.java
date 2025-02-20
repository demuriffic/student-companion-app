package com.example.studentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    // savings table + attributes
    private static final String SAVINGS_TABLE = "SAVINGS_TABLE";
    private static final String COLUMN_SAVINGS_DATE = "DATE";
    private static final String COLUMN_SAVINGS_AMOUNT = "AMOUNT";
    private static final String COLUMN_SAVINGS_ID = "ID";

    // expenses table + attributes
    private static final String EXPENSES_TABLE = "EXPENSES_TABLE";
    private static final String COLUMN_EXPENSES_ID = "ID";
    private static final String COLUMN_EXPENSES_DATE = "DATE";
    private static final String COLUMN_EXPENSES_AMOUNT = "AMOUNT";

    public DatabaseHelper(@Nullable Context context) {
        super(context, new File(context.getFilesDir(), "Student_Companion.db").getAbsolutePath(), null, 2);
    }

    // get the current date
    public String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        return currentDate;
    }

    // get the current month
    public String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);  // Get the current year
        int month = calendar.get(Calendar.MONTH) + 1;  // Get the current month (0-based, so add 1)

        // Format as YYYY-MM
        return String.format("%04d-%02d", year, month);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table for savings
        String createSavingsTableStatement = "CREATE TABLE " + SAVINGS_TABLE + " (" + COLUMN_SAVINGS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_SAVINGS_AMOUNT + " REAL, " + COLUMN_SAVINGS_DATE + " TEXT)";
        db.execSQL(createSavingsTableStatement);

        // Create table for expenses
        String createExpensesTableStatement = "CREATE TABLE " + EXPENSES_TABLE + " (" + COLUMN_EXPENSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_EXPENSES_AMOUNT + " REAL, " + COLUMN_EXPENSES_DATE + " TEXT)";
        db.execSQL(createExpensesTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Create table for expenses
            String createExpensesTableStatement = "CREATE TABLE " + EXPENSES_TABLE + " (" + COLUMN_EXPENSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_EXPENSES_AMOUNT + " REAL, " + COLUMN_EXPENSES_DATE + " TEXT)";
            db.execSQL(createExpensesTableStatement);
        }
    }

    // Functions for the savings table
    public boolean addOneToSavingsTable(SavingsModel savingsModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_SAVINGS_AMOUNT, savingsModel.getAmount());
        cv.put(COLUMN_SAVINGS_DATE, savingsModel.getDate());

        long insert = db.insert(SAVINGS_TABLE, null, cv);
        if (insert == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean updateSavingsTable(int id, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SAVINGS_AMOUNT, amount);
        cv.put(COLUMN_SAVINGS_DATE, date);

        int result = db.update("SAVINGS_TABLE", cv, "ID = ?", new String[]{String.valueOf(id)});
        db.close();

        return result > 0;
    }
    public boolean deleteOneFromSavingsTable(SavingsModel savingsModel) {
        // if found return true, else return false
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + SAVINGS_TABLE + " WHERE " + COLUMN_SAVINGS_ID + " = " + savingsModel.getId();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }
    public List<SavingsModel> getAllFromSavingsTable(boolean sortDate, boolean sortAmount) {
        List<SavingsModel> returnList = new ArrayList<>();

        // Check the status of the buttons
        String dateOrder = sortDate ? " DESC" : " ASC";
        String amountOrder = sortAmount ? " ASC" : " DESC";
        String queryString = "SELECT * FROM " + SAVINGS_TABLE + " ORDER BY "+ COLUMN_SAVINGS_DATE + dateOrder + ", " + COLUMN_SAVINGS_AMOUNT + amountOrder;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            // loop through the results
            do {
                int ID = cursor.getInt(0);
                float amount = cursor.getFloat(1);
                String date = cursor.getString(2);

                SavingsModel newEntry = new SavingsModel(ID, amount, date);
                returnList.add(newEntry);
            } while (cursor.moveToNext());
        } else {
            // fail!
        }
        // close
        cursor.close();
        db.close();
        return returnList;
    }
    public List<Entry> getSavingsGraph() {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_SAVINGS_AMOUNT + " FROM SAVINGS_TABLE ORDER BY " + COLUMN_SAVINGS_DATE + " ASC", null);

        int index = 0; // X-axis counter
        if (cursor.moveToFirst()) {
            do {
                float amount = cursor.getFloat(0);
                entries.add(new Entry(index, amount)); // (X, Y) -> (Index, Amount)
                index++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    public double getTotalFromSavingsTable() {
        double total = 0.0;
        String queryString = "SELECT SUM(" + COLUMN_SAVINGS_AMOUNT + ") AS TotalSavings FROM " + SAVINGS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalSavings"));
        }
        cursor.close();
        db.close();
        return total;
    }

    public double getDailySavings() {
        double daily = 0.0;
        String today = getTodayDate();
        String queryString = "SELECT SUM(" + COLUMN_SAVINGS_AMOUNT + ") AS DailySavings FROM " + SAVINGS_TABLE
                + " WHERE " + COLUMN_SAVINGS_DATE + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[]{today});

        if (cursor.moveToFirst()) {
            daily = cursor.getDouble(cursor.getColumnIndexOrThrow("DailySavings"));
        }
        cursor.close();
        db.close();
        return daily;
    }

    public double getMonthlySavings() {
        double daily = 0.0;
        String month = getCurrentMonth();
        String queryString = "SELECT SUM(" + COLUMN_SAVINGS_AMOUNT + ") AS MonthlySavings FROM " + SAVINGS_TABLE
                + " WHERE strftime('%Y-%m', " + COLUMN_SAVINGS_DATE + ") = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, new String[]{month});

        if (cursor.moveToFirst()) {
            daily = cursor.getDouble(cursor.getColumnIndexOrThrow("MonthlySavings"));
        }
        cursor.close();
        db.close();
        return daily;
    }

    // Functions for the expenses table
    public boolean addOneToExpensesTable(ExpensesModel expensesModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_EXPENSES_AMOUNT, expensesModel.getAmount());
        cv.put(COLUMN_EXPENSES_DATE, expensesModel.getDate());

        long insert = db.insert(EXPENSES_TABLE, null, cv);
        if (insert == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean updateExpensesTable(int id, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EXPENSES_AMOUNT, amount);
        cv.put(COLUMN_EXPENSES_DATE, date);

        int result = db.update("EXPENSES_TABLE", cv, "ID = ?", new String[]{String.valueOf(id)});
        db.close();

        return result > 0;
    }
    public boolean deleteOneFromExpensesTable(ExpensesModel expensesModel) {
        // if found return true, else return false
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + EXPENSES_TABLE + " WHERE " + COLUMN_EXPENSES_ID + " = " + expensesModel.getId();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }
    public List<ExpensesModel> getAllFromExpensesTable(boolean sortDate, boolean sortAmount) {
        List<ExpensesModel> returnList = new ArrayList<>();

        // Check the status of the buttons
        String dateOrder = sortDate ? " DESC" : " ASC";
        String amountOrder = sortAmount ? " ASC" : " DESC";
        String queryString = "SELECT * FROM " + EXPENSES_TABLE + " ORDER BY "+ COLUMN_EXPENSES_DATE + dateOrder + ", " + COLUMN_EXPENSES_AMOUNT + amountOrder;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            // loop through the results
            do {
                int ID = cursor.getInt(0);
                float amount = cursor.getFloat(1);
                String date = cursor.getString(2);

                ExpensesModel newEntry = new ExpensesModel(ID, amount, date);
                returnList.add(newEntry);
            } while (cursor.moveToNext());
        } else {
            // fail!
        }
        // close
        cursor.close();
        db.close();
        return returnList;
    }

    public double getTotalFromExpensesTable() {
        double total = 0.0;
        String queryString = "SELECT SUM(" + COLUMN_EXPENSES_AMOUNT + ") AS TotalExpenses FROM " + EXPENSES_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalExpenses"));
        }
        cursor.close();
        db.close();
        return total;
    }
}
