
// compile: javac -cp "jfreechart-1.0.13/lib/*" PVGraph.java

// run: java -cp ".:jfreechart-1.0.13/*:/usr/share/java/mysql-connector-java.jar" PVGraph

import java.sql.*;
import java.util.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import java.text.SimpleDateFormat;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class PVGraph extends ApplicationFrame {
    
    static Properties props;
    
    static LinkedList<PVGraph> graphs = new LinkedList<PVGraph>();
    
    private Connection conn;
    private Calendar date; 
    
    private class DayData {
        String inverter;
        String serial;
        double startTotalPower;
        double endTotalPower;
        java.util.List<Timestamp> times = new java.util.ArrayList<Timestamp>(12 * 24);
        java.util.List<Integer> powers = new java.util.ArrayList<Integer>(12 * 24);
    };

    private class MonthData {
        String inverter;
        String serial;
        double startTotalPower;
        double endTotalPower;
        double powers[] = new double[31];
        int numPowers;
    };
    
    public PVGraph(Connection conn) {
        super("PV Power");
        this.conn = conn;
        date = new GregorianCalendar();
        synchronized(graphs) {
            graphs.add(this);
        }
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Day", makeDayPanel());
        tabPane.addTab("Month", makeMonthPanel());
        setContentPane(tabPane);
        pack();
        setVisible(true);
    }
    
    public JPanel makeDayPanel() {
        
        JPanel dayPanel = new JPanel();
        dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));

        final ChartPanel dayChartPanel = (ChartPanel)createDayChartPanel();
        dayChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        dayPanel.add(dayChartPanel);
        
        JButton dayDecButton = new JButton("Day -");
        dayDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.DAY_OF_MONTH, -1);
                    dayChartPanel.setChart(createDayChart());;
                }
        });
        JButton dayIncButton = new JButton("Day +");
        dayIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.DAY_OF_MONTH, 1);
                    dayChartPanel.setChart(createDayChart());;
                }
        });
        
        JButton monthDecButton = new JButton("Month -");
        monthDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.MONTH, -1);
                    dayChartPanel.setChart(createDayChart());;
                }
        });
        JButton monthIncButton = new JButton("Month +");
        monthIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.MONTH, 1);
                    dayChartPanel.setChart(createDayChart());;
                }
        });
        
        JButton yearDecButton = new JButton("Year -");
        yearDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.YEAR, -1);
                    dayChartPanel.setChart(createDayChart());;
                }
        });
        JButton yearIncButton = new JButton("Year +");
        yearIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.YEAR, 1);
                    dayChartPanel.setChart(createDayChart());;
                }
        });
        
        JButton newGraphButton = new JButton("New Graph");
        newGraphButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    new PVGraph(PVGraph.this.conn);
                }
        });
        
        JPanel buttonsPanel = new JPanel();
        dayPanel.add(buttonsPanel);

        buttonsPanel.add(yearDecButton);
        buttonsPanel.add(yearIncButton);
        buttonsPanel.add(monthDecButton);
        buttonsPanel.add(monthIncButton);
        buttonsPanel.add(dayDecButton);
        buttonsPanel.add(dayIncButton);
        buttonsPanel.add(newGraphButton);
        
        return dayPanel;
    }
    
    public JPanel makeMonthPanel() {
        
        JPanel monthPanel = new JPanel();
        monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.Y_AXIS));

        final ChartPanel monthChartPanel = (ChartPanel)createMonthChartPanel();
        monthChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        monthPanel.add(monthChartPanel);
                
        JButton monthDecButton = new JButton("Month -");
        monthDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.MONTH, -1);
                    monthChartPanel.setChart(createMonthChart());;
                }
        });
        JButton monthIncButton = new JButton("Month +");
        monthIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.MONTH, 1);
                    monthChartPanel.setChart(createMonthChart());;
                }
        });
        
        JButton yearDecButton = new JButton("Year -");
        yearDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.YEAR, -1);
                    monthChartPanel.setChart(createMonthChart());;
                }
        });
        JButton yearIncButton = new JButton("Year +");
        yearIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.YEAR, 1);
                    monthChartPanel.setChart(createMonthChart());;
                }
        });
        
        JButton newGraphButton = new JButton("New Graph");
        newGraphButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    new PVGraph(PVGraph.this.conn);
                }
        });
        
        JPanel buttonsPanel = new JPanel();
        monthPanel.add(buttonsPanel);

        buttonsPanel.add(yearDecButton);
        buttonsPanel.add(yearIncButton);
        buttonsPanel.add(monthDecButton);
        buttonsPanel.add(monthIncButton);
        buttonsPanel.add(newGraphButton);
        
        return monthPanel;
    }

    
    public JPanel createDayChartPanel() {
        JFreeChart chart = createDayChart();
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public JPanel createMonthChartPanel() {
        JFreeChart chart = createMonthChart();
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }
    
    private JFreeChart createDayChart() {
        
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        int day = date.get(Calendar.DAY_OF_MONTH);
        
        java.util.List<DayData> dayData = getDayData(year, month, day);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        double totalDayPower = 0;
        
        for(DayData dd : dayData) {
            TimeSeries s = new TimeSeries(dd.inverter + (dayData.size() > 1? ("-" + dd.serial) : ""));
            for(int i = 0; i < dd.times.size(); ++i)
                s.add(new Minute(dd.times.get(i)), dd.powers.get(i));
            dataset.addSeries(s);
            totalDayPower += dd.endTotalPower - dd.startTotalPower;
        }
        
        String dayPower = totalDayPower < 1.0? String.format("%d W", (int)(totalDayPower * 1000)) : String.format("%.3f KW", totalDayPower);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            year + " / " + month + " / " + day + "      " + dayPower, // title
            "Time",     // x-axis label
            "Watts",    // y-axis label
            dataset,    // data
            true,       // create legend?
            true,       // generate tooltips?
            false       // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        double maxPower = Double.parseDouble(props.getProperty("maxpower.day", "0"));
        if(maxPower > 0) {
            ValueAxis powerAxis = plot.getRangeAxis();
            powerAxis.setAutoRange(false);
            powerAxis.setLowerBound(0.0);
            powerAxis.setUpperBound(maxPower * 1000);
        }
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
            //renderer.setSeriesPaint(0, new Color(0, 128, 0));
            for(int i = 0; i < dataset.getSeriesCount(); ++i)
                renderer.setSeriesShape(i, new Rectangle(-2, -2, 4, 4));
        }
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        
        return chart;   
    }

    private JFreeChart createMonthChart() {
        
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        
        java.util.List<MonthData> monthData = getMonthData(year, month);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        double totalMonthPower = 0;
        
        for(MonthData md : monthData) {
            TimeSeries s = new TimeSeries(md.inverter + (monthData.size() > 1? ("-" + md.serial) : ""));
            double lastPower = md.startTotalPower;
            for(int i = 0; i < md.numPowers; ++i) {
                if(md.powers[i] != 0) {
                    s.add(new Day(i + 1, month, year), md.powers[i] - lastPower);
                    lastPower = md.powers[i];
                }
            }
            dataset.addSeries(s);
            totalMonthPower += md.endTotalPower - md.startTotalPower;
        }
        
        String monthPower = totalMonthPower < 1.0? String.format("%d W", (int)(totalMonthPower * 1000)) : String.format("%.3f KW", totalMonthPower);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            year + " / " + month + "      " + monthPower, // title
            "Day",     // x-axis label
            "KW",    // y-axis label
            dataset,    // data
            true,       // create legend?
            true,       // generate tooltips?
            false       // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        double maxPower = Double.parseDouble(props.getProperty("maxpower.month", "0"));
        if(maxPower > 0) {
            ValueAxis powerAxis = plot.getRangeAxis();
            powerAxis.setAutoRange(false);
            powerAxis.setLowerBound(0.0);
            powerAxis.setUpperBound(maxPower);
        }
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
            //renderer.setSeriesPaint(0, new Color(0, 128, 0));
            for(int i = 0; i < dataset.getSeriesCount(); ++i)
                renderer.setSeriesShape(i, new Rectangle(-2, -2, 4, 4));
        }
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("d"));
        
        return chart;   
    }
    
    public void windowClosing(java.awt.event.WindowEvent event) {
        synchronized(graphs) {
            graphs.remove(this);
            if(graphs.size() == 0) {
                try {
                    conn.close();
                    System.out.println("Database connection terminated");
                }
                catch(Exception e) {
                    // relax
                }
                // this kills the application
                super.windowClosing(event);
            }
        }
    }
    
    public java.util.List<DayData> getDayData(int year, int month, int day) {
        Statement stmt = null;
        String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " and dayofmonth(DateTime) = " + day + " order by DateTime";
        Map<String, DayData> result = new HashMap<String, DayData>();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String serial = rs.getString("serial");
                DayData dd = result.get(serial);
                if(dd == null) {
                    dd = new DayData();
                    dd.serial = serial;
                    dd.inverter = rs.getString("inverter");
                    dd.startTotalPower = rs.getDouble("ETotalToday");
                    result.put(serial, dd);
                }
                dd.times.add(rs.getTimestamp("DateTime"));
                dd.powers.add(rs.getInt("CurrentPower"));
                dd.endTotalPower = rs.getDouble("ETotalToday");
            }
        } catch (SQLException e ) {
            System.err.println("Query failed: " + e.getMessage());
        } finally {
            try {
                stmt.close();
            }
            catch (SQLException e) {
                // relax
            }
        }
        return new java.util.ArrayList<DayData>(result.values());
    }
    
    public java.util.List<MonthData> getMonthData(int year, int month) {
        Statement stmt = null;
        String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " order by DateTime";
        Map<String, MonthData> result = new HashMap<String, MonthData>();
        GregorianCalendar gc = new GregorianCalendar();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String serial = rs.getString("serial");
                MonthData md = result.get(serial);
                if(md == null) {
                    md = new MonthData();
                    md.serial = serial;
                    md.inverter = rs.getString("inverter");
                    md.startTotalPower = rs.getDouble("ETotalToday");
                    result.put(serial, md);
                }
                gc.setTime(rs.getTimestamp("DateTime"));
                md.numPowers = gc.get(Calendar.DAY_OF_MONTH);
                double power = rs.getDouble("ETotalToday");
                md.powers[md.numPowers - 1] =  power;
                md.endTotalPower = power;
            }
        } catch (SQLException e ) {
            System.err.println("Query failed: " + e.getMessage());
        } finally {
            try {
                stmt.close();
            }
            catch (SQLException e) {
                // relax
            }
        }
        return new java.util.ArrayList<MonthData>(result.values());
    }
    
    public static void main (String[] args) {
        props = new Properties(System.getProperties());
        try {
            props.load(new FileInputStream("pvgraph.properties"));
        }
        catch (IOException ioe) {
            // relax
        }
        String user = props.getProperty("user");
        String password = props.getProperty("password");
        String url = props.getProperty("url");
        Connection conn = null;
        try {
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection (url, user, password);
            System.out.println ("Database connection established");
            new PVGraph(conn);
        }
        catch (SQLException e) {
            System.err.println("Cannot connect to " + url + ": " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
