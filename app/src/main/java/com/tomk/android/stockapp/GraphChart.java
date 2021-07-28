package com.tomk.android.stockapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.tomk.android.stockapp.models.ChartItem;
import com.tomk.android.stockapp.models.StockResponse;
import com.tomk.android.stockapp.models.TimeSeriesItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Tom Kowszun on 6/1/2018.
 */
public class GraphChart extends ViewGroup {

    public static double MaxValueDisplay = Double.MIN_VALUE;
    public static double MinValueDisplay = Double.MAX_VALUE;
    public static double MaxValueReal = Double.MIN_VALUE;
    public static int MaxDigitsAllowed = 6;
    public static final int VertNumberOfTicks = 3;
    public static final int boxLineThicknessStat = 2;

    private List<ChartItem> graphItems = new ArrayList<ChartItem>();

    private RectF graphChartBounds = new RectF();
    private RectF componentBounds = new RectF();

    private final Paint graphBackPaint;
    private final Paint boxPaint;
    private final Paint tickLinePaint;
    private Paint textPaint;
    private final Paint horizontalGridPaint;
    private final int graphColor;

    private GraphChartView graphChartView;
    private AppCompatTextView graphWait;
    private final int minimumWidth = 100;
    private final int minimumHeight = 50;
    private final int tickYaxisOffset = 0;
    private final int textYaxisOffset = 20;

    private final int approximateCharacterWidth = 13;
    float leftMargin = 10;
    final float rightMargin = 20;
    final float bottomMargin = 10;
    final float topMargin = 0;
    final float barWidth = 10;
    final float minMarginSize = 20;
    int numLeftMinorTicks = 20;
    final int numBottomTicks = 5;

    int tickThickness = 1;
    final int boxLineThickness = boxLineThicknessStat;
    final int ticLength = 10;
    final int bottomTicHorizStartOffset = 0;
    final int bottomTextVertOffset = ticLength + 15;
    private final int lineOffset = 0; // affects location of Y axis and start of the X axis

    // Graph display types
    public static final int BAR_GRAPH = 1;
    public static final int LINEAR_GRAPH = 2;
    public static final int POINT_GRAPH = 3;
    public static final int FILL_GRAPH = 4;
    public static final int MINOR_GRAPH = 5;
    public static int graphDisplayType = GraphChart.LINEAR_GRAPH;

    // Data used to plot graph
    public static final int USE_OPEN = 1;
    public static final int USE_CLOSE = 2;
    public static final int USE_HIGH = 3;
    public static final int USE_LOW = 4;
    public static final int USE_VOLUME = 5;
    public static final int graphDataUsed = GraphChart.USE_OPEN;

    private static final String TAG = "GraphChart";

    /**
     * Custom component that shows
     * a chart graph.
     */
    public GraphChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint = new Paint();
        int spSize = 20;
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, context.getResources().getDisplayMetrics());
        textPaint.setTextSize(scaledSizeInPixels);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GraphChart, 0, 0);
        try {
            graphBackPaint = new Paint();
            graphBackPaint.setStyle(Paint.Style.FILL);

            boxPaint = new Paint();

            int boxElementsColor = ContextCompat.getColor(getContext(), android.R.color.primary_text_dark);

            graphColor = ContextCompat.getColor(getContext(), android.R.color.holo_green_light);



            boxPaint.setColor(boxElementsColor);
            boxPaint.setStrokeWidth(boxLineThickness);
            boxPaint.setStyle(Paint.Style.STROKE);

            tickLinePaint = new Paint();
            tickLinePaint.setColor(boxElementsColor);
            tickLinePaint.setStyle(Paint.Style.STROKE);

            horizontalGridPaint = new Paint();

            horizontalGridPaint.setColor(boxElementsColor);
            horizontalGridPaint.setStyle(Paint.Style.STROKE);
            horizontalGridPaint.setPathEffect(new DashPathEffect(new float[]{7, 6}, 0));


            int textColor = ContextCompat.getColor(getContext(), android.R.color.primary_text_dark);

            textPaint = new Paint();
            textPaint.setColor(textColor);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextSize(25);

        } finally {
            attributes.recycle();
        }

    }

    public void init(StockResponse stockResponse, int displayType) {

        graphDisplayType = displayType;
        graphChartView = new GraphChartView(getContext(), graphItems);
        addView(graphChartView);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // GraphChart will out its children in onSizeChanged().
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (graphItems != null && graphItems.size() > 0) {

            if(graphDisplayType == MINOR_GRAPH)
            {

            } else
            {
                // draw the bottom tick marks with numbers
                paintBottomNumbersAndTics(canvas, numBottomTicks);

                // draw the left side tick marks and major tic marks with numbers
                paintLeftNumbersAndTics(canvas, GraphChart.VertNumberOfTicks);

                // draw the graph box that forms the border of the graph
//            canvas.drawRect(graphChartBounds, boxPaint);
            }

        }

    }


    private void paintLeftNumbersAndTics(Canvas canvas, int numOfTics) {

        double minDisp = MinValueDisplay;
        double maxDisp = MaxValueDisplay;

        double singleGraphStep = (graphChartBounds.height() - boxLineThickness) / (numOfTics - 1);
        double singleValueStep = (maxDisp - minDisp) / (numOfTics - 1);
        DecimalFormat df = new DecimalFormat("###.##");
        double currentDisplayValue = maxDisp;
        double currentGraphHeight = graphChartBounds.height();
        Rect bounds = new Rect();

        // path
        Path polyPath = new Path();
        float pathX = (graphChartBounds.left - tickYaxisOffset - lineOffset + boxLineThickness / 2);
        float pathY = graphChartBounds.bottom - (float) (currentGraphHeight) + (boxLineThickness / 2);
        polyPath.moveTo(pathX, pathY);

        for (int i = 0; i < numOfTics; i++) {
            float yPosition = graphChartBounds.bottom - (float) (currentGraphHeight) + (boxLineThickness / 2);
            double displayValue = currentDisplayValue;

            // draw horizontal grid line
            pathY = yPosition;
            if (i != 0 && i != numOfTics - 1) {
                // draw a horizontal grid line
                pathX = graphChartBounds.right + tickYaxisOffset + lineOffset - boxLineThickness / 2;
                polyPath.lineTo(pathX, pathY);
                canvas.drawPath(polyPath, horizontalGridPaint);
            }

            // draw a single small tick
            // lineOffset is used so ticks can follow to the left with the Y axis and start of X axis
            canvas.drawLine((graphChartBounds.left - tickYaxisOffset - lineOffset - boxLineThickness / 2), yPosition, graphChartBounds.left - ticLength - tickYaxisOffset - lineOffset - boxLineThickness / 2, yPosition, tickLinePaint);

            // draw the left numbers
            textPaint.getTextBounds(df.format(displayValue), 0, df.format(displayValue).length(), bounds);
            canvas.drawText(df.format(displayValue), graphChartBounds.left - textYaxisOffset - lineOffset - bounds.width() - boxLineThickness / 2, yPosition + (bounds.height() / 2), textPaint);

            currentGraphHeight = currentGraphHeight - singleGraphStep;
            currentDisplayValue = currentDisplayValue - singleValueStep;
            pathX = (graphChartBounds.left - tickYaxisOffset - lineOffset + boxLineThickness / 2);
            pathY = graphChartBounds.bottom - (float) (currentGraphHeight) + (boxLineThickness / 2);
            polyPath.moveTo(pathX, pathY);
        }
    }

    private void paintBottomNumbersAndTics(Canvas canvas, int numOfTics) {

        if (graphItems != null && graphItems.size() > 0) {
            int numberOfDataPoints = graphItems.size();
            Log.d(TAG, " \n --------------->>> numberOfDataPoints  = " + numberOfDataPoints);
            leftMargin = String.valueOf(GraphChart.MaxValueDisplay).length() * approximateCharacterWidth;
            if (leftMargin < minMarginSize) leftMargin = minMarginSize;
            double xCoordScale = ((graphChartBounds.width() - boxLineThickness) / (numOfTics - 1));

            int ticStep = numberOfDataPoints / numOfTics;
            int yOrig = boxLineThickness / 2;
            int xOrigin = boxLineThickness / 2;
            Rect bounds = new Rect();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");//("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < numOfTics; i++) {

                // draw the bottom tick
                canvas.drawLine((float) ((graphChartBounds.left + xCoordScale * i) + bottomTicHorizStartOffset + xOrigin), (graphChartBounds.bottom + yOrig), (float) ((graphChartBounds.left + xCoordScale * i) + bottomTicHorizStartOffset + xOrigin), (graphChartBounds.bottom + ticLength + yOrig), tickLinePaint);
                String formattedDate = df.format(graphItems.get(ticStep * i).date);
                textPaint.getTextBounds(formattedDate, 0, formattedDate.length(), bounds);

                // draw the date or time
                canvas.drawText(formattedDate, (float) ((graphChartBounds.left + xCoordScale * i) + bottomTicHorizStartOffset - (bounds.width() / 2)) + xOrigin, (graphChartBounds.bottom + ticLength + bottomTextVertOffset + yOrig), textPaint);
            }
        }
    }

    /**
     * Do all of the recalculations needed when the data array changes.
     * <p>
     * In this method the existing chart item values are recalculated
     * to reflect changed itemValue
     */
    private void recalculateGraph() {

        MaxValueDisplay = Double.MIN_VALUE;
        MinValueDisplay = Double.MAX_VALUE;
        MaxValueReal = Double.MIN_VALUE;

        if (graphItems == null) return;
        for (ChartItem item : graphItems) {
            if (item.itemValue > MaxValueDisplay) MaxValueDisplay = item.itemValue;
            if (item.itemValue < MinValueDisplay) MinValueDisplay = item.itemValue;
        }

        leftMargin = String.valueOf(GraphChart.MaxValueDisplay).length() * approximateCharacterWidth;
        if (leftMargin < minMarginSize)

            leftMargin = minMarginSize;

        graphChartBounds = new RectF(componentBounds.left + leftMargin, componentBounds.top + topMargin, componentBounds.right - rightMargin, componentBounds.bottom - bottomMargin);

        // Lay out the child view that actually draws the graph.
        graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);

        int numberOfDataPoints = graphItems.size();
        double yOrigin = boxLineThickness / 2;
        int xOrigin = boxLineThickness / 2;

        double graphLineScaleFactor = (graphChartBounds.width() - boxLineThickness) / (numberOfDataPoints - 1);
        double valueScaleFactor = (graphChartBounds.height() - boxLineThickness) / (GraphChart.MaxValueDisplay - GraphChart.MinValueDisplay);

        int cnt = 0;
        for (ChartItem item : graphItems) {
            item.left = (float) (xOrigin + (graphLineScaleFactor * cnt));
            item.right = item.left + barWidth;
            item.top = graphChartBounds.height() - (float) ((valueScaleFactor * item.itemValue) - (valueScaleFactor * GraphChart.MinValueDisplay) + yOrigin);

            item.bottom = graphChartBounds.height();
            item.color = graphColor;
            cnt++;
        }
    }


    //
    // Measurement functions. This example uses a simple heuristic: it assumes that
    // the graph chart should be at least as wide as its label.
    //
    @Override
    protected int getSuggestedMinimumWidth() {
        return minimumWidth * 2;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return minimumHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
        int minh = (w - getSuggestedMinimumHeight()) + getPaddingBottom() + getPaddingTop();
        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;
        componentBounds = new RectF(0.0f, 0.0f, ww, hh);
        componentBounds.offsetTo(getPaddingLeft(), getPaddingTop());

        leftMargin = String.valueOf(GraphChart.MaxValueDisplay).length() * approximateCharacterWidth;
        if (leftMargin < minMarginSize) leftMargin = minMarginSize;

        graphChartBounds = new RectF(componentBounds.left + leftMargin, componentBounds.top + topMargin, componentBounds.right - rightMargin, componentBounds.bottom - bottomMargin);

        // Lay out the child view that actually draws the graph.
        graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);

    }

    public void newStockReceived(StockResponse stockResponse) {

        if (stockResponse != null && this.graphItems != null) {
            this.graphItems.clear();
            this.graphItems = GraphChart.convertGraphData(stockResponse);
            graphChartView.updateData(this.graphItems);
            recalculateGraph();
            changeGraphType(GraphChart.graphDisplayType);
            float xpad = (float) (getPaddingLeft() + getPaddingRight());
            float ypad = (float) (getPaddingTop() + getPaddingBottom());

            float ww = (float) this.getWidth() - xpad;
            float hh = (float) this.getHeight() - ypad;

            // Layout the child view that actually draws the graph.
            graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);
            this.invalidate();
            graphChartView.invalidate();
        }

    }

    public void changeGraphType(int graphType) {
        graphDisplayType = graphType;

        graphChartView.setDiagramType(graphDisplayType);

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        // Lay out the child view that actually draws the graph.
        graphChartView.layout((int) graphChartBounds.left, (int) graphChartBounds.top, (int) graphChartBounds.right, (int) graphChartBounds.bottom);
        this.invalidate();
        graphChartView.invalidate();

    }

    //
    // Extracts individual data items from the stockResponse and converts them to ChartItems
    // that are then drawn directly in the onDraw() method of the GraphChartView class.
    //
    // TimeSeriesItem from stockResponse is converted into ChartItem
    //
    //
    public static List<ChartItem> convertGraphData(StockResponse stockResponse) {
        List<ChartItem> chartItems = null;

        int chainCnt = 1;
        MaxValueDisplay = Double.MIN_VALUE;
        MinValueDisplay = Double.MAX_VALUE;
        MaxValueReal = MaxValueDisplay;

        int indIdx = 0;
        if (stockResponse.getTimeSeriesItems() != null && stockResponse.getTimeSeriesItems().size() > 0) {
            chartItems = new ArrayList<>();

            Iterator<TimeSeriesItem> timeSeriesIterator = stockResponse.getTimeSeriesItems().iterator();
            while (timeSeriesIterator.hasNext()) {
                TimeSeriesItem timeSeriesItem = timeSeriesIterator.next();
                ChartItem chartItem = new ChartItem();

                ChartItem indItem1 = new ChartItem();
                ChartItem indItem2 = new ChartItem();
                ChartItem indItem3 = new ChartItem();

                // switch statement with int data type
                switch (GraphChart.graphDataUsed) {
                    case StockResponse.GRAPH_LOW:
                        chartItem.itemValue = timeSeriesItem.getLow();
                        break;
                    case StockResponse.GRAPH_HIGH:
                        chartItem.itemValue = timeSeriesItem.getHigh();
                        break;
                    case StockResponse.GRAPH_OPEN:
                        chartItem.itemValue = timeSeriesItem.getOpen();
                        break;
                    case StockResponse.GRAPH_CLOSE:
                        chartItem.itemValue = timeSeriesItem.getClose();
                        break;
                    case StockResponse.GRAPH_VOLUME:
                        chartItem.itemValue = timeSeriesItem.getVolume();
                        break;
                    default:
                        chartItem.itemValue = timeSeriesItem.getLow();
                        break;
                }
                if (chartItem.itemValue > MaxValueDisplay) MaxValueDisplay = chartItem.itemValue;
                if (chartItem.itemValue < MinValueDisplay) MinValueDisplay = chartItem.itemValue;
                chartItem.date = (Date) timeSeriesItem.getDate().clone();
                chartItems.add(chartItem);

            }
        }

        MaxValueReal = MaxValueDisplay;
        double originalMaxValue = MaxValueDisplay;

        return chartItems;
    }

}
