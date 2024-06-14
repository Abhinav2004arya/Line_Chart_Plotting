/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package line_chart_plotting;

/**
 *
 * @author ABHINAV ARYA
 */
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StockChart extends ApplicationFrame {

    public StockChart(String title) {
        super(title);
        JFreeChart chart = createChart();
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private JFreeChart createChart() {
        // Load data from CSV
        TimeSeries closingPrices = new TimeSeries("Closing Prices");
        TimeSeries ma20 = new TimeSeries("20-Period MA");
        XYSeries volumes = new XYSeries("Volumes");
        String inputFilePath = "e:/StockDataBANKBARODA.csv";
       

         try (CSVReader csvReader = new CSVReader(new FileReader(inputFilePath))) 
         {
            csvReader.readNext();
            String[] lines;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
            
            while ((lines = csvReader.readNext())!= null)
                {
                    System.out.println("Date: " + lines[1] + ", Opening Price: " + lines[2] + ", Highest Price: " + lines[3]
                            + ", Lowest Price: " + lines[4] + ", Closing Price: " + lines[5] + ", Total Volume: " + lines[6] );
                    
                    
                    
                    Date date = dateFormat.parse(lines[1]);
                    double closePrice = Double.parseDouble(lines[5]);
                    long volume = Long.parseLong(lines[6]);
                    
                    closingPrices.addOrUpdate(new Second(date), closePrice);
                    volumes.add(date.getTime(), volume);
                    
                }

        }
            catch(CsvValidationException | IOException e)
            {
                System.out.println("This Exception has arrived: " + e);
            }
         catch (ParseException ex) {
             System.out.println(ex);
        }

        // Calculate moving averages
        calculateMovingAverage(closingPrices, ma20, 20);

        // Create dataset
        TimeSeriesCollection priceDataset = new TimeSeriesCollection();
        priceDataset.addSeries(closingPrices);
        priceDataset.addSeries(ma20);

        XYSeriesCollection volumeDataset = new XYSeriesCollection();
        volumeDataset.addSeries(volumes);

        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "BANK OF BARODA Stock Prices",
                "Date",
                "Price",
                priceDataset,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);

        // Customize renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        
        plot.setRenderer(renderer);
        
        
         
        
        

        // Add volume data to the secondary axis
        NumberAxis volumeAxis = new NumberAxis("Volume");
        plot.setRangeAxis(1, volumeAxis);
        plot.setDataset(1, volumeDataset);
        plot.mapDatasetToRangeAxis(1, 1);
        
        // Customize renderer for volumes
        XYLineAndShapeRenderer volumeRenderer = new XYLineAndShapeRenderer();
        volumeRenderer.setSeriesPaint(0, Color.YELLOW); 
        plot.setRenderer(1, volumeRenderer);
        

       

        
        
        return chart;
    }
 

    private void calculateMovingAverage(TimeSeries source, TimeSeries destination, int period) {
        int itemCount = source.getItemCount();

        for (int i = 0; i < itemCount; i++) {
            double sum = 0.0;
            int count = 0;

            // Calculate sum of available points up to the current period
            for (int j = 0; j < period && (i - j) >= 0; j++) {
                sum += source.getValue(i - j).doubleValue();
                count++;
            }

            // Calculate average and add it to the destination series
            double average = sum / count;
            destination.add(source.getTimePeriod(i), average);
        }
    }

    public static void main(String[] args) {
        StockChart chart = new StockChart("Stock Prices Chart");
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}
