package com.example.expense_tracker_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private TextView welcomeText, totalExpensesText;
    private Button addExpenseButton;
    private RecyclerView expenseList;
    private ExpenseAdapter expenseAdapter;
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        totalExpensesText = findViewById(R.id.totalExpensesText);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        expenseList = findViewById(R.id.expenseList);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("user_email");
        if (userEmail == null) {
            Toast.makeText(this, "Error: User not identified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set welcome message
        String username = dbHelper.getUsernameByEmail(userEmail);
        welcomeText.setText("Welcome, " + (username.isEmpty() ? "User" : username) + "!");

        // Set up RecyclerView
        expenseList.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter();
        expenseList.setAdapter(expenseAdapter);

        // Load expenses
        updateExpenses();

        // Set click listener for add expense button
        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddExpenseActivity.class);
                intent.putExtra("user_email", userEmail);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh expenses when returning to the activity
        updateExpenses();
    }

    private void updateExpenses() {
        // Update total expenses
        double totalExpenses = dbHelper.getTotalExpensesByUser(userEmail);
        DecimalFormat df = new DecimalFormat("$#,##0.00");
        totalExpensesText.setText(df.format(totalExpenses));

        // Update RecyclerView
        List<DBHelper.Expense> expenses = dbHelper.getExpensesByUser(userEmail);
        expenseAdapter.setExpenses(expenses);
    }

    // RecyclerView Adapter
    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
        private List<DBHelper.Expense> expenses;

        public ExpenseAdapter() {
            this.expenses = new java.util.ArrayList<>();
        }

        public void setExpenses(List<DBHelper.Expense> expenses) {
            this.expenses = expenses;
            notifyDataSetChanged();
        }

        @Override
        public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ExpenseViewHolder holder, int position) {
            DBHelper.Expense expense = expenses.get(position);
            DecimalFormat df = new DecimalFormat("$#,##0.00");
            holder.amountText.setText(df.format(expense.getAmount()));
            holder.categoryText.setText(expense.getCategory());
            holder.dateText.setText(expense.getDate());
            holder.descriptionText.setText(expense.getDescription() != null ? expense.getDescription() : "");
        }

        @Override
        public int getItemCount() {
            return expenses.size();
        }

        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            TextView amountText, categoryText, dateText, descriptionText;

            public ExpenseViewHolder(View itemView) {
                super(itemView);
                amountText = itemView.findViewById(R.id.expenseAmount);
                categoryText = itemView.findViewById(R.id.expenseCategory);
                dateText = itemView.findViewById(R.id.expenseDate);
                descriptionText = itemView.findViewById(R.id.expenseDescription);
            }
        }
    }
}