package com.tomk.android.stockapp.WebAccess;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.gson.Gson;
import com.tomk.android.stockapp.MainActivity;
import com.tomk.android.stockapp.models.StockDbAdapter;
import com.tomk.android.stockapp.Util;
import com.tomk.android.stockapp.models.JSONparser;
import com.tomk.android.stockapp.models.RawDataTest;
import com.tomk.android.stockapp.models.Repository.DataRepository;
import com.tomk.android.stockapp.models.Repository.RepositoryItem;
import com.tomk.android.stockapp.models.StockResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * <p>
 */
public class StockDataAccess {

    public static final String STOCK_LIST_ACTION = "com.tomk.android.stockapp.STOCK_ACTION";
    public static final String NUMBER_OF_ITEMS = "0";
    public static final String RESULT_STRING = "";
    private StockDbAdapter stockDbAdapter = null;

    private ConnectivityManager conMan;
    private Context appCon;

    private static final String TAG = "StockDataAccess";

    /*
    In this class the following functionality is performed:

    1. Download data from the Web
    2. Save the data to the SQLite DB
    */

    public StockDataAccess (Context appContext)
    {
        this.appCon = appContext;
        this.conMan = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean getStockData(String type, String stockSymbol, String interval, String outputSize, String seriesType, String apiKeyObtained) {

        int numberOfItems = 0;

        Intent broadcastIntent = new Intent();
        StockResponse stockResponse = null;

        // Access the web to get the stock response
        stockResponse = getStockResponse(stockSymbol, interval, outputSize, seriesType, apiKeyObtained, type);

        // Save the stock response to database
        if (stockResponse != null && stockResponse.getTimeSeriesItems() != null && stockResponse.getTimeSeriesItems().size() != 0) {
            if (stockResponse.getResultString() != null && stockResponse.getResultString().equals(MainActivity.NO_ERRORS)) {
                saveDataInDB(stockResponse);
                Log.d(TAG, " ---------------- getStockData saveDataInDB " + stockResponse.getCompanyOverviewMap().get("symbol"));
                return true;
            }
            numberOfItems = stockResponse.getTimeSeriesItems().size();
        }

        return false;
    }

    /**
     * After the Stock Data has been downloaded from the Web Services, it is then
     * parsed and packed into a list of items and returned.
     */
    private StockResponse getStockResponse(String stockSymbol, String interval, String timePeriod, String seriesType, String apiKeyObtained, String type) {

        StockResponse stockResponse = new StockResponse();
        String rawData = null;

        if (Util.isWiFiAvailable(conMan)) {

            StockSymbolHttpClient stockSymbolHttpClient = new StockSymbolHttpClient();
            rawData = stockSymbolHttpClient.getStockData(stockSymbol, interval, timePeriod, seriesType, apiKeyObtained, type);
//            rawData = RawDataTest.RawDataTestString; // testing only

            if (rawData == null || rawData.length() < 1) {
                stockResponse.setResultString(MainActivity.ERROR_CONNECTING);
                return stockResponse;
            } else if (rawData.contains(MainActivity.STOCK_NOT_FOUND)) {
                stockResponse.setResultString(MainActivity.STOCK_NOT_FOUND);
                return stockResponse;
            } else if (rawData.contains(MainActivity.ERROR_CONNECTING)) {
                stockResponse.setResultString(MainActivity.ERROR_CONNECTING);
                return stockResponse;
            }

            try {
                if (stockResponse != null) {
                    stockResponse = JSONparser.getStockResponse(rawData);
                } else {
                    stockResponse = new StockResponse();
                    stockResponse.setResultString(MainActivity.STOCK_NOT_FOUND);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            // If we got to this point there are no errors so we set the result string according;y
            stockResponse.setResultString(MainActivity.NO_ERRORS);
        } else {
            stockResponse.setResultString(MainActivity.NO_WIFI);
            return stockResponse;
        }


        // Now try to get the Company Overview raw string from the web services
        String rawDataCompanyOverview = null;
//        conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Util.isWiFiAvailable(conMan)) {

            //Use http client to obtain data from the web services call.  Raw data is returned as a Stringf
            CompanyOverviewHttpClient companyOverviewHttpClient = new CompanyOverviewHttpClient();
            rawDataCompanyOverview = companyOverviewHttpClient.getCompanyOverview(stockSymbol, apiKeyObtained);

            // Validate the raw data string
            if (rawData == null || rawData.length() < 1) {
                stockResponse.setResultString(MainActivity.ERROR_CONNECTING);
                return stockResponse;
            } else if (rawData.contains(MainActivity.STOCK_NOT_FOUND)) {
                stockResponse.setResultString(MainActivity.STOCK_NOT_FOUND);
                return stockResponse;
            } else if (rawData.contains(MainActivity.ERROR_CONNECTING)) {
                stockResponse.setResultString(MainActivity.ERROR_CONNECTING);
                return stockResponse;
            }

            // Convert from raw string to JSON
            Gson gson = new Gson();
            CompanyOverview companyOverview = gson.fromJson(rawDataCompanyOverview, CompanyOverview.class);
            stockResponse.setResultString(MainActivity.NO_ERRORS);

            CompanyOverviewMapObject companyOverviewMapObject = new CompanyOverviewMapObject();
            LinkedHashMap companyOverviewMap = companyOverviewMapObject.getCompanyOverviewMap(companyOverview);
            stockResponse.setCompanyOverviewMap(companyOverviewMap);

        } else {
            stockResponse.setResultString(MainActivity.NO_WIFI);
            return stockResponse;
        }

        return stockResponse;
    }

    // Call adapter methods to save data to db
    protected void saveDataInDB(StockResponse stockResponse) {

        // Create database adapter and open the database
        if (stockDbAdapter == null) {
            stockDbAdapter = new StockDbAdapter(appCon);
            stockDbAdapter.open();
        }

        stockDbAdapter.insertStockResponse(stockResponse);

        DataRepository repository = new DataRepository();
        ArrayList<StockListItem> stocksListItems = new ArrayList<>();

        // Handle the list of stocks and save it in the database
        for (RepositoryItem repositoryItem : repository.getRepository()) {
            StockListItem stockListItem = new StockListItem(repositoryItem.getStockSymbol(), repositoryItem.getStockName(), null, null, null);
            stocksListItems.add(stockListItem);
        }

        stockDbAdapter.insertStocksList(stocksListItems, true);

    }


}
