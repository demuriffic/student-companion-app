package com.example.studentapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.LocalTime;
import java.util.List;

public class SummaryView extends AppCompatActivity {
    TextView tv_greeting;
    TextView tv_savingsLabel;
    TextView tv_netSavings;
    LineChart lc_savingsChart;
    DatabaseHelper databaseHelper;
    RelativeLayout savingsChartContainer;
    // ewan ko bakit nagkakaerror message so ginawa ko na lang int yung value
    // but yea this is just to set something in a transparent color
    int transparent = 17170445;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summary_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_greeting = findViewById(R.id.tv_greeting);
        tv_savingsLabel = findViewById(R.id.tv_savingsLabel);
        tv_netSavings = findViewById(R.id.tv_netSavings);
        lc_savingsChart = findViewById(R.id.lc_savingsChart);
        databaseHelper = new DatabaseHelper(this);
        RelativeLayout savingsChartContainer = findViewById(R.id.savingsChartContainer);

        savingsChartContainer.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryView.this, SavingsTracker.class);
            startActivity(intent);
        });
        lc_savingsChart.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryView.this, SavingsTracker.class);
            startActivity(intent);
        });

        LocalTime time = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            time = LocalTime.now();
            int hour = time.getHour();
            if (hour >= 0 && hour < 12) {
                tv_greeting.setText(R.string.good_morning);
            } else if (hour >= 12 && hour < 17) {
                tv_greeting.setText(R.string.good_afternoon);
            } else {
                tv_greeting.setText(R.string.good_evening);
            }
        }

        // update the graph
        List<Entry> savingsEntries = databaseHelper.getSavingsGraph();
        updateSavingsGraph(savingsEntries);
    }

    private void updateSavingsGraph(List<Entry> savingsEntries) {
        if (savingsEntries == null || savingsEntries.isEmpty()) {
            lc_savingsChart.setNoDataText("No record for savings found. Tap to add savings.");
            tv_savingsLabel.setText("A graph of your savings will show up here.");
            tv_netSavings.setText("");
        } else {
            int lineColor = ContextCompat.getColor(this, R.color.chartLineColor);
            int bgColor = ContextCompat.getColor(this, R.color.chartBackground);
            int textColor = ContextCompat.getColor(this, R.color.chartTextColor);
            tv_savingsLabel.setText("SAVINGS OVERVIEW:\n(Tap the graph below to modify)");
            LineDataSet dataSet = new LineDataSet(savingsEntries, "Savings Over Time");
            dataSet.setColor(lineColor);
            dataSet.setValueTextColor(textColor);

            lc_savingsChart.setBackgroundColor(transparent);
            lc_savingsChart.getXAxis().setTextColor(bgColor);
            lc_savingsChart.getAxisLeft().setTextColor(transparent);
            lc_savingsChart.getAxisRight().setTextColor(transparent);
            lc_savingsChart.getXAxis().setGridColor(transparent);
            lc_savingsChart.getAxisLeft().setGridColor(transparent);
            lc_savingsChart.getAxisRight().setGridColor(transparent);
            lc_savingsChart.getLegend().setTextColor(textColor);

            // Set data to chart
            LineData lineData = new LineData(dataSet);
            lc_savingsChart.setData(lineData);

            // Customize chart
            Description description = new Description();
            description.setText("INDIVIDUAL SAVINGS");
            description.setTextColor(textColor);
            lc_savingsChart.setDescription(description);
            lc_savingsChart.invalidate(); // Refresh the chart

            // Get total, daily, and monthly savings from database
            double totalSavings = databaseHelper.getTotalFromSavingsTable();
            double dailySavings = databaseHelper.getDailySavings();
            double monthlySavings = databaseHelper.getMonthlySavings();
            tv_netSavings.setText(String.format("Daily Savings: P%.2f\nMonthly Savings: P%.2f\nNet Savings: P%.2f",
                    dailySavings, monthlySavings, totalSavings));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        List<Entry> savingsEntries = databaseHelper.getSavingsGraph();
        updateSavingsGraph(savingsEntries);
    }
}