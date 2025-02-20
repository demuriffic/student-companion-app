package com.example.studentapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class SavingsTracker extends AppCompatActivity {

    // references to buttons and other controls on the layout
    GestureDetector gestureDetector;
    Button btn_add;
    EditText et_amount;
    EditText et_date;
    ListView lv_budgetList;
    TextView tv_totalSavings;
    ToggleButton tb_sortAmount;
    ToggleButton tb_sortDate;
    FloatingActionButton btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_savings_tracker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_add = findViewById(R.id.btn_add);
        et_amount = findViewById(R.id.et_amount);
        et_date = findViewById(R.id.et_date);
        lv_budgetList = findViewById(R.id.budgetList);
        tv_totalSavings = findViewById(R.id.tv_totalSavings);
        tb_sortAmount = findViewById(R.id.tb_sortAmount);
        tb_sortDate = findViewById(R.id.tb_sortDate);
        btn_back = findViewById(R.id.btn_back);

        boolean dateButtonStatus = false;
        boolean amountButtonStatus = false;

        DatabaseHelper databaseHelper = new DatabaseHelper(SavingsTracker.this);

        updateTotalSavings(databaseHelper);
        showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(SavingsTracker.this, SummaryView.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SavingsTracker.this, SummaryView.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                SavingsModel savingsModel = null;
                try {
                    savingsModel = new SavingsModel(-1, Float.parseFloat(et_amount.getText().toString()), et_date.getText().toString());
                    Toast.makeText(SavingsTracker.this, "Added data!", Toast.LENGTH_SHORT).show();

                } catch (Exception e){
                    Toast.makeText(SavingsTracker.this, "Error! Please check your input.", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean success = databaseHelper.addOneToSavingsTable(savingsModel);
                updateTotalSavings(databaseHelper);
                showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);
            }
        });

        et_date.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SavingsTracker.this,
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        // Format the selected date and set it to the EditText
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        et_date.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });
        lv_budgetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                SavingsModel longpressSavings = (SavingsModel) adapterView.getItemAtPosition(position);
                databaseHelper.deleteOneFromSavingsTable(longpressSavings);
                showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);
                Toast.makeText(SavingsTracker.this, "Deleted " + longpressSavings.toString(), Toast.LENGTH_SHORT).show();
                updateTotalSavings(databaseHelper);
                return true;
            }
        });
        lv_budgetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SavingsTracker.this);

                // Inflate the custom layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_modify_savings, null);

                builder.setView(dialogView);

                // Find references to the EditText fields
                EditText newAmount = dialogView.findViewById(R.id.et_newAmount);
                EditText newDate = dialogView.findViewById(R.id.et_newDate);
                Button submitButton = dialogView.findViewById(R.id.submitButton);

                // Create the dialog
                AlertDialog dialog = builder.create();

                // Get the ID of the data that is clicked
                SavingsModel savingsModel = (SavingsModel) adapterView.getItemAtPosition(position);
                int selectedId = savingsModel.getId();
                // Handle the date input box click (show calendar)
                newDate.setFocusable(false);  // Make the EditText non-editable
                newDate.setClickable(true);   // Allow the user to click it

                newDate.setOnClickListener(v -> {
                    // Get the current date
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    // Create and show the DatePickerDialog
                    DatePickerDialog datePickerDialog = new DatePickerDialog(SavingsTracker.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // Format the selected date and set it to the EditText
                            String formattedDate = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);  // Format: yyyy-mm-dd
                            newDate.setText(formattedDate);  // Set the selected date to the input box
                        }
                    }, year, month, day);
                    // Makes it so that the only dates the user can select is the present or past
                    datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                    // Show the DatePickerDialog
                    datePickerDialog.show();
                });

                // Handle button click
                submitButton.setOnClickListener(v1 -> {

                    String stringAmountToModify = newAmount.getText().toString();
                    float amountToModify = Float.parseFloat(stringAmountToModify);
                    String dateToModify = newDate.getText().toString();

                    // Update values on the database
                    databaseHelper.updateSavingsTable(selectedId, amountToModify, dateToModify);
                    Toast.makeText(SavingsTracker.this, "Updated entry!", Toast.LENGTH_SHORT).show();
                    // Dismiss the dialog
                    dialog.dismiss();
                    showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);
                    updateTotalSavings(databaseHelper);
                });

                dialog.show();
            }
        });
        tb_sortDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean isAmountChecked = tb_sortAmount.isChecked();
            showDataOnList(databaseHelper, isChecked, isAmountChecked);
        });
        tb_sortAmount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean isDateChecked = tb_sortDate.isChecked();
            showDataOnList(databaseHelper, isDateChecked, isChecked);
        });
    }
    private void showDataOnList(DatabaseHelper databaseHelper, boolean dateSort, boolean amountSort) {
        ArrayAdapter budgetArrayAdapter = new ArrayAdapter<SavingsModel>(SavingsTracker.this, android.R.layout.simple_list_item_1, databaseHelper.getAllFromSavingsTable(dateSort, amountSort));
        lv_budgetList.setAdapter(budgetArrayAdapter);
    }

    private void updateTotalSavings(DatabaseHelper databaseHelper) {
        double totalSavings = databaseHelper.getTotalFromSavingsTable();
        tv_totalSavings.setText(String.format("Total Savings: P%.2f", totalSavings));
    }
}