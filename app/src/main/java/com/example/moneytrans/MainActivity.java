package com.example.moneytrans;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneytrans.databinding.ActivityMainBinding;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private CurrencyDatabase database;
    private ConversionAdapter adapter;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, Double> currencyRates = new HashMap<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(getString(R.string.prefs_name), MODE_PRIVATE);
        database = CurrencyDatabase.getInstance(this);
        adapter = new ConversionAdapter();

        initializeRates();
        setupSpinners();
        setupRecycler();
        restorePreferences();
        loadHistory();

        binding.convertButton.setOnClickListener(v -> performConversion());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.currency_codes,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fromSpinner.setAdapter(spinnerAdapter);
        binding.toSpinner.setAdapter(spinnerAdapter);
    }

    private void setupRecycler() {
        binding.historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecycler.setAdapter(adapter);
    }

    private void initializeRates() {
        currencyRates.put("USD", 1.0);
        currencyRates.put("EUR", 0.92);
        currencyRates.put("GBP", 0.79);
        currencyRates.put("JPY", 150.0);
        currencyRates.put("CNY", 7.1);
        currencyRates.put("INR", 83.0);
        currencyRates.put("AUD", 1.51);
        currencyRates.put("CAD", 1.36);
    }

    private void performConversion() {
        String amountText = binding.amountInput.getText().toString();
        if (TextUtils.isEmpty(amountText)) {
            binding.amountInput.setError(getString(R.string.error_invalid_amount));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException exception) {
            binding.amountInput.setError(getString(R.string.error_invalid_amount));
            return;
        }

        String fromCurrency = binding.fromSpinner.getSelectedItem().toString();
        String toCurrency = binding.toSpinner.getSelectedItem().toString();

        double rate = calculateRate(fromCurrency, toCurrency);
        double resultAmount = amount * rate;
        String formattedResult = String.format(
                Locale.getDefault(),
                "%s %s",
                toCurrency,
                decimalFormat.format(resultAmount)
        );
        binding.resultText.setText(formattedResult);

        persistPreferencesIfNeeded(amountText, fromCurrency, toCurrency);
        saveConversion(fromCurrency, toCurrency, rate, amount, resultAmount);
    }

    private double calculateRate(String fromCurrency, String toCurrency) {
        Double from = currencyRates.get(fromCurrency);
        Double to = currencyRates.get(toCurrency);
        if (from == null || to == null) {
            Toast.makeText(this, "Unsupported currency", Toast.LENGTH_SHORT).show();
            return 1.0;
        }
        return to / from;
    }

    private void persistPreferencesIfNeeded(String amount, String fromCurrency, String toCurrency) {
        if (binding.persistToggle.isChecked()) {
            sharedPreferences.edit()
                    .putString("amount", amount)
                    .putString("from", fromCurrency)
                    .putString("to", toCurrency)
                    .apply();
        } else {
            sharedPreferences.edit().clear().apply();
        }
    }

    private void restorePreferences() {
        String amount = sharedPreferences.getString("amount", "");
        String from = sharedPreferences.getString("from", null);
        String to = sharedPreferences.getString("to", null);
        if (!TextUtils.isEmpty(amount)) {
            binding.amountInput.setText(amount);
            binding.persistToggle.setChecked(true);
        }
        if (from != null) {
            setSpinnerToCurrency(binding.fromSpinner, from);
        }
        if (to != null) {
            setSpinnerToCurrency(binding.toSpinner, to);
        }
    }

    private void setSpinnerToCurrency(android.widget.Spinner spinner, String currency) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (currency.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveConversion(String fromCurrency, String toCurrency, double rate, double amount, double resultAmount) {
        ConversionEntity entity = new ConversionEntity(
                fromCurrency,
                toCurrency,
                rate,
                amount,
                resultAmount,
                System.currentTimeMillis()
        );

        databaseExecutor.execute(() -> {
            database.conversionDao().insertConversion(entity);
            List<ConversionEntity> history = database.conversionDao().recentConversions();
            runOnUiThread(() -> adapter.submitList(history));
        });
    }

    private void loadHistory() {
        databaseExecutor.execute(() -> {
            List<ConversionEntity> history = database.conversionDao().recentConversions();
            runOnUiThread(() -> adapter.submitList(history));
        });
    }
}