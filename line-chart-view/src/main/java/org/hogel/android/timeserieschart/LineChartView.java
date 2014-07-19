package org.hogel.android.timeserieschart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.view.View;

import java.util.List;

public class LineChartView extends View {

    public static class Point {
        private long x;

        private long y;

        public Point() {
        }

        public Point(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public long getX() {
            return x;
        }

        public void setX(long x) {
            this.x = x;
        }

        public long getY() {
            return y;
        }

        public void setY(long y) {
            this.y = y;
        }
    }

    protected final List<Point> points;

    protected final Paint paint = new Paint();

    protected final Paint labelPaint = new Paint();

    protected final Paint framePaint = new Paint();

    protected final ShapeDrawable chartDrawable;

    protected final ShapeDrawable yLabelDrawable;

    protected final ShapeDrawable xLabelDrawable;

    protected final LineChartStyle lineChartStyle;

    protected Long manualXGridUnit = null;

    protected Long manualYGridUnit = null;

    protected long yLabelWidth = 0;

    protected long xLabelHeight = 0;

    protected long chartTopMargin = 0;

    protected long chartRightMargin = 0;

    protected List<Long> manualXLabels = null;

    private List<Long> manualYLabels = null;

    public LineChartView(Context context, List<Point> points) {
        this(context, points, new LineChartStyle());
    }

    public LineChartView(Context context, List<Point> points, LineChartStyle lineChartStyle) {
        super(context);
        this.points = points;
        this.lineChartStyle = lineChartStyle;
        paint.setAntiAlias(true);

        yLabelDrawable = new ShapeDrawable();
        xLabelDrawable = new ShapeDrawable();

        chartDrawable = new ShapeDrawable();
        updateDrawables();
    }

    public void updateDrawables() {
        drawXLabels(xLabelDrawable);
        drawYLabels(yLabelDrawable);
        drawLineChart(chartDrawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        yLabelDrawable.draw(canvas);
        xLabelDrawable.draw(canvas);
        chartDrawable.draw(canvas);
    }

    protected void drawYLabels(ShapeDrawable labelDrawable) {
        Shape labelsShape = new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                labelPaint.setTextAlign(Paint.Align.RIGHT);
                labelPaint.setTextSize(lineChartStyle.getLabelTextSize());
                labelPaint.setColor(lineChartStyle.getLabelTextColor());

                long minY = getMinY();
                long maxY = getMaxY();
                long yrange = maxY - minY;

                int canvasHeight = canvas.getHeight();

                if (manualYLabels != null) {
                    for (long y : manualYLabels) {
                        String label = formatYLabel(y);
                        float yCoordinate = getYCoordinate(canvasHeight, y, minY, yrange);
                        canvas.drawText(label, yLabelWidth, yCoordinate, labelPaint);
                    }
                } else {
                    long yGridUnit = getYGridUnit();
                    long y = minY;
                    while (y <= maxY) {
                        String label = formatYLabel(y);
                        float yCoordinate = getYCoordinate(canvasHeight, y, minY, yrange);
                        canvas.drawText(label, yLabelWidth, yCoordinate, labelPaint);
                        y += yGridUnit;
                    }
                }
            }
        };
        measureYLabel();
        labelDrawable.setBounds(0, 0, getWidth(), getHeight());
        labelDrawable.setShape(labelsShape);
    }

    protected void measureYLabel() {
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        labelPaint.setTextSize(lineChartStyle.getLabelTextSize());

        long minY = getMinY();
        long maxY = getMaxY();
        long yGridUnit = getYGridUnit();

        yLabelWidth = 0;
        chartTopMargin = 0;

        long y = minY;
        Rect textBounds = new Rect();

        while (y <= maxY) {
            String label = formatYLabel(y);
            labelPaint.getTextBounds(label, 0, label.length(), textBounds);
            if (textBounds.width() > yLabelWidth) {
                yLabelWidth = textBounds.width();
            }
            chartTopMargin = textBounds.height();
            y += yGridUnit;
        }
    }

    protected String formatYLabel(long y) {
        if (lineChartStyle.getYLabelFormatter() != null) {
            return lineChartStyle.getYLabelFormatter().format(y);
        }
        return String.format("%,d", y);
    }

    protected void drawXLabels(ShapeDrawable labelDrawable) {
        Shape labelsShape = new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                labelPaint.setTextAlign(Paint.Align.CENTER);
                labelPaint.setTextSize(lineChartStyle.getLabelTextSize());
                labelPaint.setColor(lineChartStyle.getLabelTextColor());

                long minX = getMinX();
                long maxX = getMaxX();
                long xGridUnit = getXGridUnit();
                long xrange = maxX - minX;

                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();

                if (manualXLabels != null) {
                    for (long x : manualXLabels) {
                        String label = formatXLabel(x);
                        float xCoordinate = getXCoordinate(canvasWidth, x, minX, xrange);
                        canvas.drawText(label, xCoordinate, canvasHeight, labelPaint);
                    }
                } else {
                    long x = getRawMinX();
                    while (x <= maxX) {
                        String label = formatXLabel(x);
                        float xCoordinate = getXCoordinate(canvasWidth, x, minX, xrange);
                        canvas.drawText(label, xCoordinate, canvasHeight, labelPaint);
                        x += xGridUnit;
                    }
                }
            }
        };
        measureXLabel();
        labelDrawable.setBounds(0, 0, getWidth(), getHeight());
        labelDrawable.setShape(labelsShape);
    }

    protected void measureXLabel() {
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(lineChartStyle.getLabelTextSize());

        long minX = getRawMinX();
        long maxX = getMaxX();
        long xGridUnit = getXGridUnit();

        xLabelHeight = 0;
        chartRightMargin = 0;

        long x = minX;
        Rect textBounds = new Rect();

        while (x <= maxX) {
            String label = formatXLabel(x);
            labelPaint.getTextBounds(label, 0, label.length(), textBounds);
            if (textBounds.height() > xLabelHeight) {
                xLabelHeight = textBounds.height();
            }
            chartRightMargin = textBounds.width() / 2;
            x += xGridUnit;
        }
    }

    protected String formatXLabel(long x) {
        if (lineChartStyle.getXLabelFormatter() != null) {
            return lineChartStyle.getXLabelFormatter().format(x);
        }
        return String.format("%,d", x);
    }

    protected void drawLineChart(ShapeDrawable chartDrawable) {
        Shape chartShape = new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                long minX = getMinX();
                long maxX = getMaxX();
                long xrange = maxX - minX;

                long minY = getMinY();
                long maxY = getMaxY();
                long yrange = maxY - minY;

                int width = canvas.getWidth();
                int height = canvas.getHeight();
                float left = getChartLeftMargin();
                float top = getChartTopMargin();
                float right = width - getChartRightMargin();
                float bottom = height - getChartBottomMargin();

                drawChartFrame(canvas, left, top, right, bottom);

                drawXGrid(canvas, minX, xrange);
                drawYGrid(canvas, minY, yrange);

                drawChartFrameBorder(canvas, left, top, right, bottom);

                drawLines(canvas, minX, xrange, minY, yrange);

                if (lineChartStyle.isDrawPoint()) {
                    drawPoints(canvas, minX, xrange, minY, yrange);
                }
            }
        };
        chartDrawable.setBounds(0, 0, getWidth(), getHeight());
        chartDrawable.setShape(chartShape);
    }

    protected void drawChartFrame(Canvas canvas, float left, float top, float right, float bottom) {
        canvas.save();
        canvas.clipRect(left, top, right, bottom);
        canvas.drawColor(lineChartStyle.getBackgroundColor());
        canvas.restore();
    }

    protected void drawChartFrameBorder(Canvas canvas, float left, float top, float right, float bottom) {
        framePaint.setColor(lineChartStyle.getFrameBorderColor());
        framePaint.setStrokeWidth(lineChartStyle.getFrameBorderWidth());

        LineChartStyle.Border border = lineChartStyle.getFrameBorder();
        if (border.left()) {
            canvas.drawLine(left, top, left, bottom, framePaint);
        }
        if (border.top()) {
            canvas.drawLine(left, top, right, top, framePaint);
        }
        if (border.right()) {
            canvas.drawLine(right, top, right, bottom, framePaint);
        }
        if (border.bottom()) {
            canvas.drawLine(left, bottom, right, bottom, framePaint);
        }
    }

    protected float getChartLeftMargin() {
        return yLabelWidth + lineChartStyle.getYLabelMargin();
    }

    protected float getChartTopMargin() {
        return chartTopMargin;
    }

    protected float getChartRightMargin() {
        return chartRightMargin;
    }

    protected float getChartBottomMargin() {
        return xLabelHeight + lineChartStyle.getXLabelMargin();
    }

    protected long getMinX() {
        return getRawMinX();
    }

    protected long getRawMinX() {
        return points.get(0).getX();
    }

    protected long getMaxX() {
        return getRawMaxX();
    }

    protected long getRawMaxX() {
        return points.get(points.size() - 1).getX();
    }

    protected float getXCoordinate(int canvasWidth, Point point, long minX, long xrange) {
        return getXCoordinate(canvasWidth, point.getX(), minX, xrange);
    }

    protected float getXCoordinate(int canvasWidth, long x, long minX, long xrange) {
        return getXCoordinate(canvasWidth, x, minX, xrange, true);
    }

    protected float getXCoordinate(int canvasWidth, long x, long minX, long xrange, boolean inChartArea) {
        if (inChartArea) {
            float left = getChartLeftMargin();
            float right = getChartRightMargin();
            float margin = left + right;
            return (canvasWidth - margin) * (x - minX) * 1.0f / (xrange) + left;
        } else {
            return canvasWidth * (x - minX) * 1.0f / xrange;
        }
    }

    protected long getMinY() {
        return 0;
    }

    protected long getMaxY() {
        long rawMaxY = getRawMaxY();
        long step = getStep(rawMaxY);
        return (rawMaxY / step + 1) * step;
    }

    protected long getRawMaxY() {
        long maxY = Long.MIN_VALUE;
        for (Point point : points) {
            long y = point.getY();
            if (y > maxY) {
                maxY = y;
            }
        }
        return maxY;
    }

    protected long getStep(long maxValue) {
        int digits = String.valueOf(maxValue).length();
        return (long) Math.pow(10, digits - 1);
    }

    protected float getYCoordinate(int canvasHeight, Point point, long minY, long yrange) {
        return getYCoordinate(canvasHeight, point.getY(), minY, yrange);
    }

    protected float getYCoordinate(int canvasHeight, long y, long minY, long yrange) {
        return getYCoordinate(canvasHeight, y, minY, yrange, true);
    }

    protected float getYCoordinate(int canvasHeight, long y, long minY, long yrange, boolean inChartArea) {
        if (inChartArea) {
            float top = getChartTopMargin();
            float bottom = getChartBottomMargin();
            float margin = top + bottom;
            return (canvasHeight - margin) * (1.0f - (y - minY) * 1.0f / (yrange)) + top;
        } else {
            return canvasHeight * (1.0f - (y - minY) * 1.0f / yrange);
        }
    }

    protected void drawXGrid(Canvas canvas, long minX, long xrange) {
        long rawMinX = getRawMinX();
        long rawMaxX = getRawMaxX();
        long xGridUnit = getXGridUnit();

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float top = getChartTopMargin();
        float bottom = canvasHeight - getChartBottomMargin();

        paint.setColor(lineChartStyle.getGridColor());
        paint.setStrokeWidth(lineChartStyle.getGridWidth());

        long x = rawMinX;

        while (x <= rawMaxX) {
            float xCoordinate = getXCoordinate(canvasWidth, x, minX, xrange);
            canvas.drawLine(xCoordinate, bottom, xCoordinate, top, paint);
            x += xGridUnit;
        }
    }

    protected void drawYGrid(Canvas canvas, long minY, long yrange) {
        long yGridUnit = getYGridUnit();
        long maxY = getMaxY();

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float left = getChartLeftMargin();
        float right = canvasWidth - getChartRightMargin();

        paint.setColor(lineChartStyle.getGridColor());
        paint.setStrokeWidth(lineChartStyle.getGridWidth());

        long y = 0;
        while (y <= maxY) {
            float yCoordinate = getYCoordinate(canvasHeight, y, minY, yrange);
            canvas.drawLine(left, yCoordinate, right, yCoordinate, paint);
            y += yGridUnit;
        }
    }

    protected void drawLines(Canvas canvas, long minX, long xrange, long minY, long yrange) {
        Point prevPoint = null;
        float px = 0.0f, py = 0.0f;

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        paint.setColor(lineChartStyle.getLineColor());
        paint.setStrokeWidth(lineChartStyle.getLineWidth());
        for (Point point : points) {
            float x = getXCoordinate(canvasWidth, point, minX, xrange);
            float y = getYCoordinate(canvasHeight, point, minY, yrange);
            if (prevPoint != null) {
                canvas.drawLine(px, py, x, y, paint);
            }
            prevPoint = point;
            px = x;
            py = y;
        }
    }

    protected void drawPoints(Canvas canvas, long minX, long xrange, long minY, long yrange) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        for (Point point : points) {
            float x = getXCoordinate(canvasWidth, point, minX, xrange);
            float y = getYCoordinate(canvasHeight, point, minY, yrange);

            paint.setColor(lineChartStyle.getLineColor());
            canvas.drawCircle(x, y, lineChartStyle.getPointSize(), paint);

            if (lineChartStyle.isDrawPointCenter()) {
                paint.setColor(lineChartStyle.getBackgroundColor());
                canvas.drawCircle(x, y, lineChartStyle.getPointCenterSize(), paint);
            }
        }
    }

    public void setXGridUnit(long xGridUnit) {
        manualXGridUnit = xGridUnit;
        updateDrawables();
    }

    public long getXGridUnit() {
        if (manualXGridUnit != null) {
            return manualXGridUnit;
        }
        long rawMaxX = getRawMaxX();
        long xStep = getStep(rawMaxX);
        return xStep >= 10 ? xStep / 2 : xStep;
    }

    public void setYGridUnit(long yGridUnit) {
        manualYGridUnit = yGridUnit;
        updateDrawables();
    }

    public long getYGridUnit() {
        if (manualYGridUnit != null) {
            return manualYGridUnit;
        }
        long rawMaxY = getRawMaxY();
        final long yStep = getStep(rawMaxY);
        return yStep >= 10 ? yStep / 2 : yStep;
    }

    public void setXLabels(List<Long> labels) {
        manualXLabels = labels;
        updateDrawables();
    }

    public void setYLabels(List<Long> labels) {
        manualYLabels = labels;
        updateDrawables();
    }
}