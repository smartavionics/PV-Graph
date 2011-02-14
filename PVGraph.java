
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
    
    public PVGraph(Connection conn) {
        super("PV Power");
        this.conn = conn;
        synchronized(graphs) {
            graphs.add(this);
        }

        date = new GregorianCalendar();
        final ChartPanel chartPanel = (ChartPanel)createDayChartPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(chartPanel);
        JPanel buttonsPanel = new JPanel();
        JButton dayDecButton = new JButton("Day -");
        dayDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.DAY_OF_MONTH, -1);
                    chartPanel.setChart(createDayChart());;
                }
        });
        JButton dayIncButton = new JButton("Day +");
        dayIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.DAY_OF_MONTH, 1);
                    chartPanel.setChart(createDayChart());;
                }
        });
        
        JButton monthDecButton = new JButton("Month -");
        monthDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.MONTH, -1);
                    chartPanel.setChart(createDayChart());;
                }
        });
        JButton monthIncButton = new JButton("Month +");
        monthIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.MONTH, 1);
                    chartPanel.setChart(createDayChart());;
                }
        });
        
        JButton yearDecButton = new JButton("Year -");
        yearDecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.YEAR, -1);
                    chartPanel.setChart(createDayChart());;
                }
        });
        JButton yearIncButton = new JButton("Year +");
        yearIncButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    date.add(Calendar.YEAR, 1);
                    chartPanel.setChart(createDayChart());;
                }
        });
        
        JButton newGraphButton = new JButton("New Graph");
        newGraphButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    new PVGraph(PVGraph.this.conn);
                }
        });
        
        buttonsPanel.add(yearDecButton);
        buttonsPanel.add(yearIncButton);
        buttonsPanel.add(monthDecButton);
        buttonsPanel.add(monthIncButton);
        buttonsPanel.add(dayDecButton);
        buttonsPanel.add(dayIncButton);
        buttonsPanel.add(newGraphButton);
        
        mainPanel.add(buttonsPanel);
        setContentPane(mainPanel);
        pack();
        setVisible(true);
    }
    
    public JPanel createDayChartPanel() {
        JFreeChart chart = createDayChart();
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
            year + "/" + month + "/" + day + " " + dayPower, // title
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
        double maxPower = Double.parseDouble(props.getProperty("maxpower", "0"));
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
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        
        return chart;   
    }
    
    class DayData {
        String inverter;
        String serial;
        double startTotalPower;
        double endTotalPower;
        java.util.List<Timestamp> times = new java.util.ArrayList<Timestamp>(12 * 24);
        java.util.List<Integer> powers = new java.util.ArrayList<Integer>(12 * 24);
    };
    
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
