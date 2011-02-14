
// compile: javac -cp "../jfreechart-1.0.13/lib/*" PVGraph.java

// run: java -cp ".:../jfreechart-1.0.13/*:/usr/share/java/mysql-connector-java.jar" PVGraph

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
    	
    	buttonsPanel.add(yearDecButton);
    	buttonsPanel.add(yearIncButton);
    	buttonsPanel.add(monthDecButton);
    	buttonsPanel.add(monthIncButton);
    	buttonsPanel.add(dayDecButton);
    	buttonsPanel.add(dayIncButton);
    	
    	mainPanel.add(buttonsPanel);
    	setContentPane(mainPanel);
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
    	
    	XYDataset dataset = createDayDataset(year, month, day);
    	
    	JFreeChart chart = ChartFactory.createTimeSeriesChart(
    	    year + "/" + month + "/" + day, // title
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
            renderer.setSeriesPaint(0, new Color(0, 128, 0));
            //renderer.setSeriesShapesVisible(0, false);
            renderer.setSeriesShape(0, new Rectangle(-1, -1, 2, 2));
        }
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        
        return chart;   
    }

    private XYDataset createDayDataset(int year, int month, int day) {
    	
    	java.util.List<Timestamp> times = new ArrayList<Timestamp>(24 * 12);
    	java.util.List<Integer> powers = new ArrayList<Integer>(24 * 12);
    	getDayData(year, month, day, times, powers);
    	//System.err.println("Got " + dayData.size() + " values");

        TimeSeries s1 = new TimeSeries(year + "/" + month + "/" + day);
        for(int i = 0; i < times.size(); ++i)
            s1.add(new Minute(times.get(i)), powers.get(i));
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

        return dataset;
    }

    public void windowClosing(java.awt.event.WindowEvent event) {
    	synchronized(graphs) {
    	    graphs.remove(this);
    	}
    	super.windowClosing(event);
    }
    
    public void getDayData(int year, int month, int day, java.util.List<Timestamp> times, java.util.List<Integer> powers) {
    	Statement stmt = null;
    	String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " and dayofmonth(DateTime) = " + day + " order by DateTime";
    	try {
    	    stmt = conn.createStatement();
    	    ResultSet rs = stmt.executeQuery(query);
    	    while (rs.next()) {
    	    	times.add(rs.getTimestamp("DateTime"));
    	    	powers.add(rs.getInt("CurrentPower"));
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
    }
    
    public static void main (String[] args) {
    	
    	props = new Properties(System.getProperties());
    	try {
    	    props.load(new FileInputStream("pvgraph.properties"));
    	}
    	catch (IOException ioe) {
    	    // relax
    	}
    	
    	Connection conn = null;
    	try {
    	    String user = props.getProperty("user");
    	    String password = props.getProperty("password");
    	    String url = props.getProperty("url");
    	    Class.forName ("com.mysql.jdbc.Driver").newInstance();
    	    conn = DriverManager.getConnection (url, user, password);
    	    System.out.println ("Database connection established");
    	    PVGraph pvgraph = new PVGraph(conn);
    	    pvgraph.pack();
    	    pvgraph.setVisible(true);
    	    boolean finished = false;
    	    while(!finished) {
    	    	try {
    	    	    Thread.sleep(100);	
    	    	}
    	    	catch (InterruptedException ie) {
    	    	    // relax
    	    	}
    	    	synchronized(graphs) {
    	    	    finished = (graphs.size() == 0);
    	    	}
    	    }
    	}
    	catch (Exception e) {
    	    System.err.println("Cannot connect to database server: " + e.getMessage());
    	}
    	finally {
    	    if (conn != null) {
    	    	try {
    	    	    conn.close();
    	    	    System.out.println("Database connection terminated");
    	    	}
    	    	catch (Exception e) { /* ignore close errors */ }
    	    }
    	}
    }
}
