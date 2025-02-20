package com.example.studentapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class ExpensesTracker extends AppCompatActivity {

    // buttons and controls sa layout
    Button bt_addExpenses;
    EditText et_expensesAmount;
    EditText et_expensesDate;
    TextView tv_totalExpenses;
    ToggleButton tb_sortExpensesDate;
    ToggleButton tb_sortExpensesAmount;
    ListView lv_expenses;
    FloatingActionButton btn_switchToSavingsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expenses_tracker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bt_addExpenses = findViewById(R.id.bt_addExpenses);
        et_expensesAmount = findViewById(R.id.et_expensesAmount);
        et_expensesDate = findViewById(R.id.et_expensesDate);
        tv_totalExpenses = findViewById(R.id.tv_totalExpenses);
        tb_sortExpensesDate = findViewById(R.id.tb_sortExpensesDate);
        tb_sortExpensesAmount = findViewById(R.id.tb_sortExpensesAmount);
        lv_expenses = findViewById(R.id.lv_expenses);
        btn_switchToSavingsTracker = findViewById(R.id.btn_switchToSavingsTracker);

        boolean dateButtonStatus = false;
        boolean amountButtonStatus = false;

        DatabaseHelper databaseHelper = new DatabaseHelper(ExpensesTracker.this);

        updateTotalExpenses(databaseHelper);
        showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);

        btn_switchToSavingsTracker.setOnClickListener(v -> {
            Intent intent = new Intent(ExpensesTracker.this, SavingsTracker.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Optional animation
        });
        bt_addExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                ExpensesModel expensesModel = null;
                try {
                    expensesModel = new ExpensesModel(-1, Float.parseFloat(et_expensesAmount.getText().toString()), et_expensesDate.getText().toString());
                    Toast.makeText(ExpensesTracker.this, "Added data!", Toast.LENGTH_SHORT).show();

                } catch (Exception e){
                    Toast.makeText(ExpensesTracker.this, "Error! Please check your input.", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean success = databaseHelper.addOneToExpensesTable(expensesModel);
                updateTotalExpenses(databaseHelper);
                showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);
            }
        });

        et_expensesDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ExpensesTracker.this,
                    (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                        // Format the selected date and set it to the EditText
                        String formattedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        et_expensesDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });
        lv_expenses.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                ExpensesModel longpressExpense = (ExpensesModel) adapterView.getItemAtPosition(position);
                databaseHelper.deleteOneFromExpensesTable(longpressExpense);
                showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);
                Toast.makeText(ExpensesTracker.this, "Deleted " + longpressExpense.toString(), Toast.LENGTH_SHORT).show();
                updateTotalExpenses(databaseHelper);
                return true;
            }
        });
        lv_expenses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ExpensesTracker.this);

                // Inflate the custom layout
                // NOTE THAT THIS REUSES THE SAME POP-UP FOR THE SAVINGS TRACKER
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
                ExpensesModel expensesModel = (ExpensesModel) adapterView.getItemAtPosition(position);
                int selectedId = expensesModel.getId();
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
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ExpensesTracker.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // Format the selected date and set it to the EditText
                            String formattedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;  // Format: yyyy-mm-dd
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
                    databaseHelper.updateExpensesTable(selectedId, amountToModify, dateToModify);
                    Toast.makeText(ExpensesTracker.this, "Updated entry!", Toast.LENGTH_SHORT).show();
                    // Dismiss the dialog
                    dialog.dismiss();
                    showDataOnList(databaseHelper, dateButtonStatus, amountButtonStatus);
                    updateTotalExpenses(databaseHelper);
                });

                dialog.show();
            }
        });
        tb_sortExpensesDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean isAmountChecked = tb_sortExpensesAmount.isChecked();
            showDataOnList(databaseHelper, isChecked, isAmountChecked);
        });
        tb_sortExpensesAmount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean isDateChecked = tb_sortExpensesDate.isChecked();
            showDataOnList(databaseHelper, isDateChecked, isChecked);
        });
    }
    private void showDataOnList(DatabaseHelper databaseHelper, boolean dateSort, boolean amountSort) {
        ArrayAdapter budgetArrayAdapter = new ArrayAdapter<ExpensesModel>(ExpensesTracker.this, android.R.layout.simple_list_item_1, databaseHelper.getAllFromExpensesTable(dateSort, amountSort));
        lv_expenses.setAdapter(budgetArrayAdapter);
    }

    private void updateTotalExpenses(DatabaseHelper databaseHelper) {
        double totalExpenses = databaseHelper.getTotalFromExpensesTable();
        tv_totalExpenses.setText(String.format("Total Expenses: P%.2f", totalExpenses));
    }
}