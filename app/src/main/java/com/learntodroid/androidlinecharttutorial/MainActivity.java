package com.learntodroid.androidlinecharttutorial;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    // dashboard: https://rapidapi.com/developer/dashboard
    // api: https://rapidapi.com/apidojo/api/yh-finance/

    public static final String BASE_URL = "https://wft-geo-db.p.rapidapi.com/";
    private YahooFinanceAPI yahooFinanceAPI;

    private LineChart lineChart;
    private TextInputLayout stockTickerTextInputLayout;
    private RadioGroup periodRadioGroup, intervalRadioGroup;
    private CheckBox highCheckBox, lowCheckBox, closeCheckBox;

    ArrayList<Entry> pricesClose = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // be careful with these 2 lines
        // https://stackoverflow.com/a/9289190/8166854
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        lineChart = findViewById(R.id.activity_main_linechart);

        stockTickerTextInputLayout = findViewById(R.id.activity_main_stockticker);
        periodRadioGroup = findViewById(R.id.activity_main_period_radiogroup);
        intervalRadioGroup = findViewById(R.id.activity_main_priceinterval);

        highCheckBox = findViewById(R.id.activity_main_high);
        lowCheckBox = findViewById(R.id.activity_main_low);
        closeCheckBox = findViewById(R.id.activity_main_close);

        configureLineChart();
        //setupApi();
        findViewById(R.id.activity_main_getprices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStockData();
            }
        });
    }

    private void configureLineChart() {
        Description desc = new Description();
        desc.setText("Stock Price History");
        desc.setTextSize(28);
        lineChart.setDescription(desc);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {
                long millis = (long) value * 1000L;
                return mFormat.format(new Date(millis));
            }
        });
    }

    private void setupApi() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
/*
        yahooFinanceAPI = new retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YahooFinanceAPI.class);*/
    }

    private void getStockData() {

        OkHttpClient client = new OkHttpClient();
        //String API_URL = "https://yh-finance.p.rapidapi.com/stock/v3/get-historical-data?symbol=AMRN&region=US";
        String API_URL = "https://yh-finance.p.rapidapi.com/stock/v3/get-historical-data?symbol=XDWD.DE";
        // async
        final Request request = new Request.Builder()
                .url(API_URL)
                .get()
                .addHeader("x-rapidapi-host", "yh-finance.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "efb484ef36mshd43c66dd6ab3c90p1ace71jsn2b0ce240b488")
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; MotoE2(4G-LTE) Build/MPI24.65-39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.81 Mobile Safari/537.36")
                .build();
        final okhttp3.Call call = client.newCall(request);

        //Thread.sleep(3000);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("OK");
                System.out.println("Response header remaining requests: " + response.header("x-ratelimit-requests-remaining"));
                System.out.println("Response message: " + response.message());
                System.out.println("Response code: " + response.code());
                ResponseBody responseBody = response.body();
                String responseBodyString = responseBody.string();
                System.out.println("Response body: " + responseBody.toString());
                System.out.println("Response body: " + responseBodyString);
                // {"prices":[{"date":1644589800,"open":3.619999885559082,"high":3.7100000381469727,"low":3.509999990463257,"close":3.5399999618530273,"volume":2456300,"adjclose":3.5399999618530273},{"date":1644503400,"open":3.609999895095825,"high":3.819999933242798,"low":3.569999933242798,"close":3.630000114440918,"volume":2487400,"adjclose":3.630000114440918},{"date":1644417000,"open":3.559999942779541,"high":3.740000009536743,"low":3.559999942779541,"close":3.690000057220459,"volume":2089300,"adjclose":3.690000057220459},{"date":1644330600,"open":3.559999942779541,"high":3.559999942779541,"low":3.440000057220459,"close":3.5299999713897705,"volume":2026500,"adjclose":3.5299999713897705},{"date":1644244200,"open":3.6500000953674316,"high":3.7200000286102295,"low":3.559999942779541,"close":3.5799999237060547,"volume":2020000,"adjclose":3.5799999237060547},{"date":1643985000,"open":3.6600000858306885,"high":3.700000047683716,"low":3.569999933242798,"close":3.6600000858306885,"volume":2461100,"adjclose":3.6600000858306885},{"date":1643898600,"open":3.559999942779541,"high":3.75,"low":3.549999952316284,"close":3.640000104904175,"volume":4262200,"adjclose":3.640000104904175},{"date":1643812200,"open":3.7899999618530273,"high":3.7899999618530273,"low":3.5899999141693115,"close":3.5999999046325684,"volume":3963800,"adjclose":3.5999999046325684},{"date":1643725800,"open":3.6500000953674316,"high":3.7899999618530273,"low":3.619999885559082,"close":3.7300000190734863,"volume":8393800,"adjclose":3.7300000190734863},{"date":1643639400,"open":3.430000066757202,"high":3.640000104904175,"low":3.4100000858306885,"close":3.619999885559082,"volume":13600900,"adjclose":3.619999885559082},{"date":1643380200,"open":3.3299999237060547,"high":3.369999885559082,"low":3.25,"close":3.359999895095825,"volume":5879400,"adjclose":3.359999895095825},{"date":1643293800,"open":3.4800000190734863,"high":3.4800000190734863,"low":3.2799999713897705,"close":3.299999952316284,"volume":7750100,"adjclose":3.299999952316284},{"date":1643207400,"open":3.3399999141693115,"high":3.700000047683716,"low":3.319999933242798,"close":3.430000066757202,"volume":16808800,"adjclose":3.430000066757202},{"date":1643121000,"open":3.1700000762939453,"high":3.390000104904175,"low":3.1500000953674316,"close":3.259999990463257,"volume":14145900,"adjclose":3.259999990463257},{"date":1643034600,"open":3,"high":3.049999952316284,"low":2.7899999618530273,"close":2.990000009536743,"volume":6025000,"adjclose":2.990000009536743},{"date":1642775400,"open":3.2200000286102295,"high":3.2300000190734863,"low":3.0399999618530273,"close":3.069999933242798,"volume":3741100,"adjclose":3.069999933242798},{"date":1642689000,"open":3.319999933242798,"high":3.3499999046325684,"low":3.180000066757202,"close":3.2100000381469727,"volume":2389900,"adjclose":3.2100000381469727},{"date":1642602600,"open":3.369999885559082,"high":3.4000000953674316,"low":3.259999990463257,"close":3.299999952316284,"volume":3265400,"adjclose":3.299999952316284},{"date":1642516200,"open":3.5,"high":3.5899999141693115,"low":3.3499999046325684,"close":3.359999895095825,"volume":6644900,"adjclose":3.359999895095825},{"date":1642170600,"open":3.4200000762939453,"high":3.5199999809265137,"low":3.390000104904175,"close":3.5199999809265137,"volume":2675800,"adjclose":3.5199999809265137},{"date":1642084200,"open":3.440000057220459,"high":3.5199999809265137,"low":3.380000114440918,"close":3.4700000286102295,"volume":4212300,"adjclose":3.4700000286102295},{"date":1641997800,"open":3.4600000381469727,"high":3.4600000381469727,"low":3.369999885559082,"close":3.4100000858306885,"volume":1438200,"adjclose":3.4100000858306885},{"date":1641911400,"open":3.3299999237060547,"high":3.5399999618530273,"low":3.2699999809265137,"close":3.450000047683716,"volume":3619300,"adjclose":3.450000047683716},{"date":1641825000,"open":3.2699999809265137,"high":3.3299999237060547,"low":3.2100000381469727,"close":3.3299999237060547,"volume":2963900,"adjclose":3.3299999237060547},{"date":1641565800,"open":3.180000066757
                System.out.println("Response:\n" + responseBodyString + "\nEND OF DATA");
                // replace null with "null" or 0
                //String responseBodyStringNull = responseBodyString.replaceAll("null", "\"null\"");
                String responseBodyStringNull = responseBodyString.replaceAll("null", "\"0\"");
                System.out.println("*** PARSING ***");
                parsePrices(responseBodyStringNull);
                System.out.println("*** PARSING END ***");

                // now building the arraylist for the chart
                Comparator<Entry> comparator = new Comparator<Entry>() {
                    @Override
                    public int compare(Entry o1, Entry o2) {
                        return Float.compare(o1.getX(), o2.getX());
                    }
                };
                pricesClose.sort(comparator);
                //setLineChartData(pricesHigh, pricesLow, pricesClose);
                setLineChartData(pricesClose);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("fail");
            }
        });


        // syncr
        /*
        Request request = new Request.Builder()
                .url("https://yh-finance.p.rapidapi.com/stock/v3/get-historical-data?symbol=AMRN&region=US")
                .get()
                .addHeader("x-rapidapi-host", "yh-finance.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "efb484ef36mshd43c66dd6ab3c90p1ace71jsn2b0ce240b488")
                .build();
        okhttp3.Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    // uses GSON
    // import com.google.gson.JsonArray;
    // import com.google.gson.JsonObject;
    // import com.google.gson.JsonParser;
    // https://devqa.io/how-to-parse-json-in-java/
    public void parsePrices(String json) {
        // JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        // https://stackoverflow.com/questions/60771386/jsonparser-is-deprecated
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        //String pageName = jsonObject.getAsJsonObject("pageInfo").get("pageName").getAsString();
        //System.out.println(pageName);

        JsonArray arr = jsonObject.getAsJsonArray("prices");
        for (int i = 0; i < arr.size(); i++) {
            String date = arr.get(i).getAsJsonObject().get("date").getAsString();
            String close = arr.get(i).getAsJsonObject().get("close").getAsString();
            // convert unix timestamp to date
            long dateL = Long.parseLong(date);
            LocalDateTime dateTimeLocal =
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(dateL),
                            TimeZone.getDefault().toZoneId());
            System.out.println("date: " + date +
                    " dateLocal: " + dateTimeLocal +
                    " close: " + close);
            Float dateFloat = Float.parseFloat(date);
            Float priceCloseFloat = Float.parseFloat(close);
            if (priceCloseFloat != 0) {
            pricesClose.add(new Entry(dateFloat, priceCloseFloat));}
            //stockName.setText(post_id);
        }

    }

    private void getStockDataOrg() {
        long endTime = System.currentTimeMillis() / 1000;
        long startTime = 0;
        switch(periodRadioGroup.getCheckedRadioButtonId()) {
            case R.id.activity_main_period1d:
                startTime = endTime - (60 * 60 * 24);
                break;
            case R.id.activity_main_period30d:
                startTime = endTime - (60 * 60 * 24 * 30);
                break;
            case R.id.activity_main_period90d:
                startTime = endTime - (60 * 60 * 24 * 90);
                break;
            case R.id.activity_main_period12m:
                startTime = endTime - (60 * 60 * 24 * 365);
                break;
        }

        String frequency = "";
        switch (intervalRadioGroup.getCheckedRadioButtonId()) {
            case R.id.activity_main_interval1d:
                frequency = "1d";
                break;
            case R.id.activity_main_interval1w:
                frequency = "1w";
                break;
            case R.id.activity_main_interval1m:
                frequency = "1m";
                break;
        }
/*
        yahooFinanceAPI.getHistoricalData(
                stockTickerTextInputLayout.getEditText().getText().toString()


        ).enqueue(new Callback<HistoricalDataResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<HistoricalDataResponse> call, Response<HistoricalDataResponse> response) {
                ArrayList<Entry> pricesHigh = new ArrayList<>();
                ArrayList<Entry> pricesLow = new ArrayList<>();
                ArrayList<Entry> pricesClose = new ArrayList<>();

                if (response.body() != null) {
                    for (int i = 0; i < response.body().prices.size(); i++) {
                        float x = response.body().prices.get(i).date;
                        float y = response.body().prices.get(i).high;
                        if (y != 0f) {
                            pricesHigh.add(new Entry(x, response.body().prices.get(i).high));
                            pricesLow.add(new Entry(x, response.body().prices.get(i).low));
                            pricesClose.add(new Entry(x, response.body().prices.get(i).close));
                        }
                    }
                    Comparator<Entry> comparator = new Comparator<Entry>() {
                        @Override
                        public int compare(Entry o1, Entry o2) {
                            return Float.compare(o1.getX(), o2.getX());
                        }
                    };

                    pricesHigh.sort(comparator);
                    pricesLow.sort(comparator);
                    pricesClose.sort(comparator);

                    setLineChartData(pricesHigh, pricesLow, pricesClose);
                }
            }

            @Override
            public void onFailure(Call<HistoricalDataResponse> call, Throwable t) {

            }
        });*/
    }
    /*
yahooFinanceAPI.getHistoricalData(
                frequency,
                "history",
                String.valueOf(startTime),
                String.valueOf(endTime),
                stockTickerTextInputLayout.getEditText().getText().toString()
        ).enqueue(new Callback<HistoricalDataResponse>() {
     */

    private void setLineChartData(ArrayList<Entry> pricesClose) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
/*
        if (highCheckBox.isChecked()) {
            LineDataSet highLineDataSet = new LineDataSet(pricesHigh, stockTickerTextInputLayout.getEditText().getText().toString() + " Price (High)");
            highLineDataSet.setDrawCircles(true);
            highLineDataSet.setCircleRadius(4);
            highLineDataSet.setDrawValues(false);
            highLineDataSet.setLineWidth(3);
            highLineDataSet.setColor(Color.GREEN);
            highLineDataSet.setCircleColor(Color.GREEN);
            dataSets.add(highLineDataSet);
        }

        if (lowCheckBox.isChecked()) {
            LineDataSet lowLineDataSet = new LineDataSet(pricesLow, stockTickerTextInputLayout.getEditText().getText().toString() + " Price (Low)");
            lowLineDataSet.setDrawCircles(true);
            lowLineDataSet.setCircleRadius(4);
            lowLineDataSet.setDrawValues(false);
            lowLineDataSet.setLineWidth(3);
            lowLineDataSet.setColor(Color.RED);
            lowLineDataSet.setCircleColor(Color.RED);
            dataSets.add(lowLineDataSet);
        }
*/
        if (closeCheckBox.isChecked()) {
            LineDataSet closeLineDataSet = new LineDataSet(pricesClose, stockTickerTextInputLayout.getEditText().getText().toString() + " Price (Close)");
            //closeLineDataSet.setDrawCircles(true);
            closeLineDataSet.setDrawCircles(false);
            closeLineDataSet.setCircleRadius(4);
            closeLineDataSet.setDrawValues(false);
            closeLineDataSet.setLineWidth(3);
            closeLineDataSet.setColor(Color.rgb(255, 165, 0));
            closeLineDataSet.setCircleColor(Color.rgb(255, 165, 0));
            dataSets.add(closeLineDataSet);
        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void setLineChartDataOrg(ArrayList<Entry> pricesHigh, ArrayList<Entry> pricesLow, ArrayList<Entry> pricesClose) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        if (highCheckBox.isChecked()) {
            LineDataSet highLineDataSet = new LineDataSet(pricesHigh, stockTickerTextInputLayout.getEditText().getText().toString() + " Price (High)");
            highLineDataSet.setDrawCircles(true);
            highLineDataSet.setCircleRadius(4);
            highLineDataSet.setDrawValues(false);
            highLineDataSet.setLineWidth(3);
            highLineDataSet.setColor(Color.GREEN);
            highLineDataSet.setCircleColor(Color.GREEN);
            dataSets.add(highLineDataSet);
        }

        if (lowCheckBox.isChecked()) {
            LineDataSet lowLineDataSet = new LineDataSet(pricesLow, stockTickerTextInputLayout.getEditText().getText().toString() + " Price (Low)");
            lowLineDataSet.setDrawCircles(true);
            lowLineDataSet.setCircleRadius(4);
            lowLineDataSet.setDrawValues(false);
            lowLineDataSet.setLineWidth(3);
            lowLineDataSet.setColor(Color.RED);
            lowLineDataSet.setCircleColor(Color.RED);
            dataSets.add(lowLineDataSet);
        }

        if (closeCheckBox.isChecked()) {
            LineDataSet closeLineDataSet = new LineDataSet(pricesClose, stockTickerTextInputLayout.getEditText().getText().toString() + " Price (Close)");
            closeLineDataSet.setDrawCircles(true);
            closeLineDataSet.setCircleRadius(4);
            closeLineDataSet.setDrawValues(false);
            closeLineDataSet.setLineWidth(3);
            closeLineDataSet.setColor(Color.rgb(255, 165, 0));
            closeLineDataSet.setCircleColor(Color.rgb(255, 165, 0));
            dataSets.add(closeLineDataSet);
        }

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}