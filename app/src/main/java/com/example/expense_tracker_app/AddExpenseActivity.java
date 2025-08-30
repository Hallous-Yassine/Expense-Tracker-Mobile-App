package com.example.expense_tracker_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText amountEditText, dateEditText, descriptionEditText;
    private Spinner categorySpinner;
    private Button saveExpenseButton;
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense);

        // Initialize views
        amountEditText = findViewById(R.id.amountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        dateEditText = findViewById(R.id.dateEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveExpenseButton = findViewById(R.id.saveExpenseButton);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("user_email");
        if (userEmail == null) {
            Toast.makeText(this, "Error: User not identified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate category spinner
        String[] categories = {"Food", "Transport", "Entertainment", "Bills", "Shopping", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set up date picker
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Set click listener for save button
        saveExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = amountEditText.getText().toString().trim();
                String category = categorySpinner.getSelectedItem().toString();
                String date = dateEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

                // Validate inputs
                if (amountStr.isEmpty()) {
                    amountEditText.setError("Amount is required");
                    amountEditText.requestFocus();
                    return;
                }
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        amountEditText.setError("Amount must be greater than 0");
                        amountEditText.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    amountEditText.setError("Invalid amount format");
                    amountEditText.requestFocus();
                    return;
                }
                if (date.isEmpty()) {
                    dateEditText.setError("Date is required");
                    dateEditText.requestFocus();
                    return;
                }

                // Save expense to database
                boolean success = dbHelper.insertExpense(userEmail, amount, category, date, description.isEmpty() ? null : description);
                if (success) {
                    Toast.makeText(AddExpenseActivity.this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                    // Return to HomeActivity
                    finish();
                } else {
                    Toast.makeText(AddExpenseActivity.this, "Failed to save expense.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as MM/DD/YYYY
                    String date = String.format(Locale.US, "%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    dateEditText.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }
}