package com.example.skaiciuotuvas;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private EditText loanAmount, loanTermYears, loanTermMonths, interestRate;
    private Spinner paymentType;
    private TextView monthlyPaymentResult, totalInterestResult, totalPaymentResult;
    private RecyclerView paymentsList;
    private Button calculateBtn, showChartBtn, applyFilterBtn, clearFilterBtn;
    private LineChart paymentChart;

    // Filtravimo valdikliai
    private EditText filterFromMonth, filterToMonth;
    private LinearLayout filterLayout;

    // Atidėjimo valdikliai
    private CheckBox enableDeferment;
    private EditText defermentMonths, defermentRate;
    private Button selectDefermentDate;
    private LinearLayout defermentLayout;
    private Date defermentStartDate;

    private List<Payment> allPayments = new ArrayList<>();
    private List<Payment> filteredPayments = new ArrayList<>();
    private PaymentAdapter adapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinner();
        setupRecyclerView();
        setupListeners();
    }

    private void initializeViews() {

        loanAmount = findViewById(R.id.loanAmount);
        loanTermYears = findViewById(R.id.loanTermYears);
        loanTermMonths = findViewById(R.id.loanTermMonths);
        interestRate = findViewById(R.id.interestRate);
        paymentType = findViewById(R.id.paymentType);
        monthlyPaymentResult = findViewById(R.id.monthlyPaymentResult);
        paymentsList = findViewById(R.id.paymentsList);
        calculateBtn = findViewById(R.id.calculateBtn);


        totalInterestResult = findViewById(R.id.totalInterestResult);
        totalPaymentResult = findViewById(R.id.totalPaymentResult);
        showChartBtn = findViewById(R.id.showChartBtn);
        paymentChart = findViewById(R.id.paymentChart);


        filterLayout = findViewById(R.id.filterLayout);
        filterFromMonth = findViewById(R.id.filterFromMonth);
        filterToMonth = findViewById(R.id.filterToMonth);
        applyFilterBtn = findViewById(R.id.applyFilterBtn);
        clearFilterBtn = findViewById(R.id.clearFilterBtn);


        enableDeferment = findViewById(R.id.enableDeferment);
        defermentLayout = findViewById(R.id.defermentLayout);
        defermentMonths = findViewById(R.id.defermentMonths);
        defermentRate = findViewById(R.id.defermentRate);
        selectDefermentDate = findViewById(R.id.selectDefermentDate);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.payment_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentType.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        paymentsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(filteredPayments);
        paymentsList.setAdapter(adapter);
    }

    private void setupListeners() {
        calculateBtn.setOnClickListener(v -> calculateLoan());
        showChartBtn.setOnClickListener(v -> toggleChart());
        applyFilterBtn.setOnClickListener(v -> applyFilter());
        clearFilterBtn.setOnClickListener(v -> clearFilter());

        enableDeferment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            defermentLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        selectDefermentDate.setOnClickListener(v -> showDatePicker());
    }

    private void calculateLoan() {
        try {
            double amount = Double.parseDouble(loanAmount.getText().toString());
            int years = Integer.parseInt(loanTermYears.getText().toString());
            int months = Integer.parseInt(loanTermMonths.getText().toString());
            double rate = Double.parseDouble(interestRate.getText().toString());

            int totalMonths = years * 12 + months;
            double monthlyRate = rate / 12 / 100;

            allPayments.clear();
            String selectedType = paymentType.getSelectedItem().toString();

            // Calculate base payments
            if (selectedType.equals(getString(R.string.annuity))) {
                calculateAnnuity(amount, totalMonths, monthlyRate, allPayments);
            } else {
                calculateLinear(amount, totalMonths, monthlyRate, allPayments);
            }

            // Apply deferment if enabled
            if (enableDeferment.isChecked()) {
                applyDeferment();
            }

            // Update filtered payments and display
            filteredPayments.clear();
            filteredPayments.addAll(allPayments);
            adapter.notifyDataSetChanged();

            updateSummary();
            setupChart();

        } catch (Exception e) {
            Toast.makeText(this, "Patikrinkite įvestus duomenis", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateAnnuity(double amount, int months, double rate, List<Payment> payments) {
        double monthlyPayment = (amount * rate) / (1 - Math.pow(1 + rate, -months));
        monthlyPaymentResult.setText(String.format("Mėnesinė įmoka: %.2f €", monthlyPayment));

        double balance = amount;
        for (int i = 1; i <= months; i++) {
            double interest = balance * rate;
            double principal = monthlyPayment - interest;
            balance = Math.max(0, balance - principal);
            payments.add(new Payment(i, monthlyPayment, principal, interest, balance));
        }
    }

    private void calculateLinear(double amount, int months, double rate, List<Payment> payments) {
        double principal = amount / months;
        double balance = amount;

        for (int i = 1; i <= months; i++) {
            double interest = balance * rate;
            double monthlyPayment = principal + interest;
            balance = Math.max(0, balance - principal);
            payments.add(new Payment(i, monthlyPayment, principal, interest, balance));
        }

        if (!payments.isEmpty()) {
            monthlyPaymentResult.setText(String.format("Pirmas mokėjimas: %.2f €", payments.get(0).monthlyPayment));
        }
    }

    private void applyDeferment() {
        try {
            int deferMonths = Integer.parseInt(defermentMonths.getText().toString());
            double deferRate = Double.parseDouble(defermentRate.getText().toString()) / 12 / 100;

            // Find the balance at deferment start (assuming it starts from month 1 for simplicity)
            // In real implementation, you'd calculate based on the selected date
            double balanceAtDeferment = allPayments.get(0).balance + allPayments.get(0).principal;

            // Calculate deferred interest
            double deferredInterest = balanceAtDeferment * deferRate * deferMonths;

            // Add deferred interest to remaining payments
            for (Payment payment : allPayments) {
                payment.balance += deferredInterest / allPayments.size();
                payment.interest += deferredInterest / allPayments.size();
                payment.monthlyPayment += deferredInterest / allPayments.size();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Patikrinkite atidėjimo duomenis", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    defermentStartDate = calendar.getTime();
                    selectDefermentDate.setText("Atidėjimo data: " + dateFormat.format(defermentStartDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void applyFilter() {
        try {
            int fromMonth = Integer.parseInt(filterFromMonth.getText().toString());
            int toMonth = Integer.parseInt(filterToMonth.getText().toString());

            filteredPayments.clear();
            for (Payment payment : allPayments) {
                if (payment.month >= fromMonth && payment.month <= toMonth) {
                    filteredPayments.add(payment);
                }
            }
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Patikrinkite filtro duomenis", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilter() {
        filteredPayments.clear();
        filteredPayments.addAll(allPayments);
        adapter.notifyDataSetChanged();
        filterFromMonth.setText("");
        filterToMonth.setText("");
    }

    private void updateSummary() {
        double totalInterest = 0;
        double totalPayment = 0;

        for (Payment payment : allPayments) {
            totalInterest += payment.interest;
            totalPayment += payment.monthlyPayment;
        }

        totalInterestResult.setText(String.format("Bendros palūkanos: %.2f €", totalInterest));
        totalPaymentResult.setText(String.format("Bendra mokėjimo suma: %.2f €", totalPayment));
    }

    private void setupChart() {
        List<Entry> principalEntries = new ArrayList<>();
        List<Entry> interestEntries = new ArrayList<>();
        List<Entry> balanceEntries = new ArrayList<>();

        String selectedType = paymentType.getSelectedItem().toString();

        for (Payment payment : allPayments) {
            if (selectedType.equals(getString(R.string.annuity))) {
                // Anuiteto grafikui
                principalEntries.add(new Entry(payment.month, (float) payment.principal));
                interestEntries.add(new Entry(payment.month, (float) payment.interest));
            } else {
                // Linijinio grafikui
                principalEntries.add(new Entry(payment.month, (float) (payment.monthlyPayment - payment.interest)));
                interestEntries.add(new Entry(payment.month, (float) payment.interest));
            }
            balanceEntries.add(new Entry(payment.month, (float) payment.balance));
        }

        LineDataSet principalDataSet = new LineDataSet(principalEntries, "Pagrindas");
        principalDataSet.setColor(Color.BLUE);
        principalDataSet.setLineWidth(2f);

        LineDataSet interestDataSet = new LineDataSet(interestEntries, "Palūkanos");
        interestDataSet.setColor(Color.RED);
        interestDataSet.setLineWidth(2f);

        LineDataSet balanceDataSet = new LineDataSet(balanceEntries, "Likutis");
        balanceDataSet.setColor(Color.GREEN);
        balanceDataSet.setLineWidth(2f);

        LineData lineData = new LineData(principalDataSet, interestDataSet, balanceDataSet);
        paymentChart.setData(lineData);

        XAxis xAxis = paymentChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        paymentChart.getDescription().setText("Mokėjimų grafikas");
        paymentChart.invalidate();
    }

    private void toggleChart() {
        if (paymentChart.getVisibility() == View.VISIBLE) {
            paymentChart.setVisibility(View.GONE);
            showChartBtn.setText("Rodyti grafiką");
        } else {
            paymentChart.setVisibility(View.VISIBLE);
            showChartBtn.setText("Slėpti grafiką");
        }
    }
}