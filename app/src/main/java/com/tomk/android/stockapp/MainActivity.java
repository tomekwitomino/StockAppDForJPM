package com.tomk.android.stockapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tomk.android.stockapp.WebAccess.StockDataAccess;
import com.tomk.android.stockapp.WebAccess.StockListItem;
import com.tomk.android.stockapp.charting.GraphChart;
import com.tomk.android.stockapp.models.Repository.DataRepository;
import com.tomk.android.stockapp.models.Repository.RepositoryItem;
import com.tomk.android.stockapp.models.StockDbAdapter;
import com.tomk.android.stockapp.models.StockResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Tom Kowszun
 */

public class MainActivity extends AppCompatActivity {

    // maximum data points accepted. if more appear, we cannot handle it at this point
    public static final int DATA_POINTS_NUM = 40;

    // reg, complex
    public static final String REQUEST_TYPE_VAL = "reg";

    // 1min, 5min, 15min, 30min, 60min
    public static final String INTERVAL_VAL = "60min";
    // compact, full
    public static final String OUTPUT_SIZE_VAL = "compact";
    // open
    public static final String SERIES_TYPE_VAL = "seriesType";

    // key from the alpha vantage
    public static final String API_KEY_OBTAINED_VAL = "apiKeyObtained";

    private StockDbAdapter stockDbAdapter;
    private StockListRVadapter stockListRVadapter;
    private CompanyOverviewRVadapter companyOverviewRVadapter;

    private Button barGraphButton;
    private Button lineGraphButton;
    private Button areaGraphButton;
    private Button candleStickGraphButton;
    private android.support.v7.widget.AppCompatTextView refreshed;

    private static final String TAG = "MainActivity";
    private static final String DEFAULT_STOCK = "IBM";
    public static final String STOCK_NOT_FOUND = "Stock Not Found";
    public static final String ERROR_CONNECTING = "Error Connecting";
    public static final String NO_WIFI = "No WiFi";
    public static final String NO_ERRORS = "No Errors";

    private Display display;

    private GraphChart graphChart;
    public static int graphDisplayType = GraphChart.MINOR_GRAPH;

    private RecyclerView recyclerViewStockList;
    private RecyclerView recyclerViewCompanyOverview;
    public boolean blocking = false;
    private android.support.v7.widget.Toolbar toolBar;
    private ProgressBar progressBar;

    private LinearLayout graphPanel;

    private boolean isHomeNow = true;

    private String currentStockDisplayed = null;

    private LinearLayout companyOverviewLayout = null;
    private TextView companyName = null;

    StockDataAccess stockDataAccess = null;

    StockResponse stockResponse = null;
    ArrayList<StockListItem> listResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.deleteDatabase(StockDbAdapter.DATABASE_NAME);

        stockDataAccess = new StockDataAccess(this);

        stockDbAdapter = new StockDbAdapter(this.getApplicationContext());
        stockDbAdapter.open();

        setContentView(R.layout.activity_main);
        companyOverviewLayout = findViewById(R.id.company_overview_layout);
        companyName = findViewById(R.id.company_name);

        progressBar = findViewById(R.id.pbLoading);
        progressBar.setVisibility(View.VISIBLE);

        toolBar = findViewById(R.id.stock_activity_toolbar);
        setSupportActionBar(toolBar);

        // add home icon to toolbar
        if (getSupportActionBar() != null) {

            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_my_home);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        recyclerViewStockList = findViewById(R.id.recycler_view_stock_list);
        RecyclerView.LayoutManager recyclerlayoutManagerStockList = new LinearLayoutManager(MainActivity.this);
        recyclerViewStockList.setLayoutManager(recyclerlayoutManagerStockList);
        recyclerViewStockList.setHasFixedSize(true);

        recyclerViewCompanyOverview = findViewById(R.id.recycler_view_company_overview);
        RecyclerView.LayoutManager recyclerlayoutManagerCompanyOverview = new LinearLayoutManager(MainActivity.this);
        recyclerViewCompanyOverview.setLayoutManager(recyclerlayoutManagerCompanyOverview);
        recyclerViewCompanyOverview.setHasFixedSize(true);

        graphPanel = findViewById(R.id.graph_panel);

        // Get stock name from preferences. If not in Preferences, use the default coded in this class.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String nameOfTheStockEntered = preferences.getString("stockNameKey", "");
        if (nameOfTheStockEntered == null || nameOfTheStockEntered.length() < 1) {
            nameOfTheStockEntered = DEFAULT_STOCK;
        }

        barGraphButton = findViewById(R.id.bar_graph);
        barGraphButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                graphDisplayType = GraphChart.BAR_GRAPH;
                MainActivity.this.graphChart.changeGraphType(graphDisplayType);
            }
        });


        lineGraphButton = findViewById(R.id.line_graph);
        lineGraphButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                graphDisplayType = GraphChart.LINEAR_GRAPH;
                MainActivity.this.graphChart.changeGraphType(graphDisplayType);
            }
        });

        areaGraphButton = findViewById(R.id.area_graph);
        areaGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphDisplayType = GraphChart.FILL_GRAPH;
                MainActivity.this.graphChart.changeGraphType(graphDisplayType);
            }
        });

        candleStickGraphButton = findViewById(R.id.candle_graph);
        candleStickGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphDisplayType = GraphChart.CANDLE_STICK_GRAPH;
                MainActivity.this.graphChart.changeGraphType(graphDisplayType);
            }
        });

        // Height of the graph in percent of the total device height
        int heightInPercent = 25;
        int graphHeight = calculatePercentageHeight(heightInPercent);
        graphChart = MainActivity.this.findViewById(R.id.Graph);

        graphChart.getLayoutParams().height = graphHeight;
        graphChart.init(null, graphDisplayType);
        setHomeDisplay(true);
        refreshed = findViewById(R.id.refreshed); // display the details page subtitle
        onItemSelected(nameOfTheStockEntered);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            setHomeDisplay(!isHomeNow);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    // sets the display to list of stocks
    private void setHomeDisplay(boolean isHome) {
        if (isHome) {
            recyclerViewStockList.setVisibility(View.VISIBLE);
            companyOverviewLayout.setVisibility(View.INVISIBLE);
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_my_home);
            toolBar.setTitle(getResources().getString(R.string.Select_a_stock));
            graphPanel.setVisibility(View.GONE);
            this.isHomeNow = true;
        } else { // sets the current display to the details page
            recyclerViewStockList.setVisibility(View.INVISIBLE);
            companyOverviewLayout.setVisibility(View.VISIBLE);
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_my_return);
            graphChart.changeGraphType(GraphChart.LINEAR_GRAPH);
            toolBar.setTitle(getResources().getString(R.string.Data_for_stock) + " " + currentStockDisplayed);
            graphPanel.setVisibility(View.VISIBLE);
            this.isHomeNow = false;
        }
    }

    private int calculatePercentageHeight(int desiredPercentage) {
        int percentHeightInPixels = 0;
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int height = display.getHeight();

        percentHeightInPixels = (desiredPercentage * height) / 100;
        return percentHeightInPixels;
    }

    // Adapter for the recycler view that displays the stock list items in the main activity window
    class StockListRVadapter extends RecyclerView.Adapter<StockListItemVH> {
        ArrayList<StockListItem> stockList;
        private StockListItemVH viewHolder;

        public StockListRVadapter(ArrayList<StockListItem> list) {

            this.stockList = list;
        }

        @Override
        public StockListItemVH onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stock, parent, false);
            return new StockListItemVH(view);
        }

        @Override
        public void onBindViewHolder(StockListItemVH holder, int position) {

            viewHolder = holder;
            viewHolder.stockSymbol = this.stockList.get(position).getStockSymbol();
            viewHolder.stockSymbolTV.setText(this.stockList.get(position).getStockSymbol());
            viewHolder.stockNameTV.setText(this.stockList.get(position).getStockName());
        }

        @Override
        public int getItemCount() {
            if(stockList != null)
            {
                return this.stockList.size();
            } else
                return 0;
        }

        public void setItems(ArrayList<StockListItem> list) {
            this.stockList = list;
        }

        public StockListItemVH getViewHolder() {
            return viewHolder;
        }
    }

    private class StockListItemVH extends RecyclerView.ViewHolder {

        public String stockSymbol;
        public final TextView stockSymbolTV;
        public final TextView stockNameTV;

        public StockListItemVH(View itemView) {
            super(itemView);

            stockSymbolTV = itemView.findViewById(R.id.symbol_textview);
            stockSymbolTV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MainActivity.this.onItemSelected(stockSymbol);
                }
            });

            stockNameTV = itemView.findViewById(R.id.stock_list_textview);
            stockNameTV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    MainActivity.this.onItemSelected(stockSymbol);
                }
            });
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.INVISIBLE);
        blocking = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving data - instance state ");
    }

    public void updateUIwithData() {

        if (stockResponse != null && stockResponse.getMetaData() != null) {

            MainActivity.this.graphChart.newStockReceived(stockResponse);

            Date date = stockResponse.getMetaData().getLastRefreshedDate();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MM/dd/yyyy");
            String dateString = simpleDateFormat.format(date);

            Log.d(TAG, "Saving symbol to SharedPreferences");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("stockNameKey", stockResponse.getMetaData().getSymbol());
            editor.apply();

            toolBar.setTitle(getResources().getString(R.string.Data_for_stock) + " " + stockResponse.getMetaData().getSymbol());
            currentStockDisplayed = stockResponse.getMetaData().getSymbol();
            refreshed.setText(getResources().getString(R.string.Refreshed) + "  " + dateString);

            LinkedHashMap compOverMap = stockResponse.getCompanyOverviewMap();
            companyOverviewRVadapter = new CompanyOverviewRVadapter(compOverMap);
            recyclerViewCompanyOverview.setAdapter(companyOverviewRVadapter);
            companyOverviewRVadapter.notifyDataSetChanged();

            setHomeDisplay(false);

            graphPanel.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        if (stockListRVadapter == null) {
            stockListRVadapter = new StockListRVadapter(listResponse);
            recyclerViewStockList.setAdapter(stockListRVadapter);
        } else {
            stockListRVadapter = new StockListRVadapter(listResponse);
            recyclerViewStockList.setAdapter(stockListRVadapter);
            stockListRVadapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.INVISIBLE);
    }


    public void onItemSelected(String stockSymbol) {

        DataRepository dataRepo = new DataRepository();
        String stockName = null;
        for (RepositoryItem i : dataRepo.getRepository()) {
            RepositoryItem item = i;
            if (item.getStockSymbol().equals(stockSymbol)) {
                stockName = item.getStockName();
                companyName.setText(getResources().getString(R.string.Company_name) + ": " + stockName);
            }
        }

        if (!blocking) {
            blocking = true;
            progressBar.setVisibility(ProgressBar.VISIBLE);
            toolBar.setTitle(getResources().getString(R.string.Looking_for_stock_data) + stockSymbol + " ...");
            graphPanel.setVisibility(View.VISIBLE);

            // main call to access the webservices and write data into db
            onItemSelectedBackgroundWork(REQUEST_TYPE_VAL, stockSymbol, INTERVAL_VAL, OUTPUT_SIZE_VAL, SERIES_TYPE_VAL, API_KEY_OBTAINED_VAL);
        } else {
            Log.d(TAG, " >>>>>>>>>>>>>>>>> BLOCKING <<<<<<<<<<<<<< ");
        }
    }

    public boolean onItemSelectedBackgroundWork(String requestType, String stockSymbol, String interval, String outputSize, String seriesType, String apiKeyObtained) {

        DataRepository dataRepo = new DataRepository();
        String stockName = null;
        for (RepositoryItem i : dataRepo.getRepository()) {
            RepositoryItem item = i;
            if (item.getStockSymbol().equals(stockSymbol)) {
                stockName = item.getStockName();
                companyName.setText(getResources().getString(R.string.Company_name) + ": " + stockName);
            }
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        boolean[] success = new boolean[1];

        executor.execute(new Runnable() {
            @Override
            public void run() {
                //Background work here
                try {
                    // Access the webservices and write data into db
                    // returns true if call successful
                    success[0] = stockDataAccess.getStockData(requestType, stockSymbol, interval, outputSize, seriesType, apiKeyObtained);

                    // Read the data from the db
                    if (success[0] == true) {
                        stockResponse = stockDbAdapter.getStockResponse();
                        listResponse = stockDbAdapter.getStocksList();
                    }

                    blocking = false;
                } catch (Exception e) {
                    success[0] = false;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        updateUIwithData();
                    }
                });
            }
        });

        return success[0];
    }
    // Adapter for the recycler view that displays Company Overview in the main activity window
    class CompanyOverviewRVadapter extends RecyclerView.Adapter<CompanyOverviewItemVH> {
        final LinkedHashMap<String, String> companyOverviewMap;
        private CompanyOverviewItemVH viewHolder;

        public CompanyOverviewRVadapter(LinkedHashMap<String, String> companyOverviewMap) {
            this.companyOverviewMap = companyOverviewMap;
        }

        @Override
        public CompanyOverviewItemVH onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_company_overview, parent, false);
            return new CompanyOverviewItemVH(view);
        }

        @Override
        public void onBindViewHolder(CompanyOverviewItemVH holder, int position) {

            viewHolder = holder;

            // For now this is hardcoded as the full list is very long and key names are not proper English.  Need to decide which of the items are worth showing
            if (position == 0) {
                viewHolder.itemNameTV.setText(getResources().getString(R.string.Exchange));
                viewHolder.itemValueTV.setText(companyOverviewMap.get("exchange"));
            } else if (position == 1) {
                viewHolder.itemNameTV.setText(getResources().getString(R.string.fifty_two_week_high));
                viewHolder.itemValueTV.setText(companyOverviewMap.get("_52WeekHigh"));
            } else if (position == 2) {
                viewHolder.itemNameTV.setText(getResources().getString(R.string.fifty_two_week_low));
                viewHolder.itemValueTV.setText(companyOverviewMap.get("_52WeekLow"));
            } else if (position == 3) {
                viewHolder.itemNameTV.setText(getResources().getString(R.string.fifty_day_moving_average));
                viewHolder.itemValueTV.setText(companyOverviewMap.get("_50DayMovingAverage"));
            } else {
                viewHolder.itemNameTV.setText(getResources().getString(R.string.Profit_Margin));
                viewHolder.itemValueTV.setText(companyOverviewMap.get("profitMargin"));
            }

        }

        @Override
        public int getItemCount() {
            // Harcoded for now so it will display only five items
            return 5;
        }

        public CompanyOverviewItemVH getViewHolder() {
            return viewHolder;
        }
    }

    private class CompanyOverviewItemVH extends RecyclerView.ViewHolder {

        public final TextView itemNameTV;
        public final TextView itemValueTV;

        public CompanyOverviewItemVH(View itemView) {
            super(itemView);

            itemNameTV = itemView.findViewById(R.id.item_name);
            itemValueTV = itemView.findViewById(R.id.item_value);
        }
    }

}
