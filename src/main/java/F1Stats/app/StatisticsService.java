package F1Stats.app;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.RectangleInsets;

import java.awt.*;
import java.util.List;

public class StatisticsService {

    public JFreeChart createDriverComparisonChart(List<ModelDriver> drivers, boolean comparePoints, boolean compareWins) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (ModelDriver driver : drivers) {
            if (comparePoints) {
                dataset.addValue(driver.getPoints(), "Points", driver.getName());
            }
            if (compareWins) {
                dataset.addValue(driver.getWins(), "Wins", driver.getName());
            }
        }

        String title = "Driver Comparison Chart";
        String categoryAxisLabel = "Drivers";
        String valueAxisLabel = "Values";

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customization
        chart.setBackgroundPaint(Color.white);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setOutlineVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        domainAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        rangeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter());

        Color barColor1 = new Color(79, 129, 189);
        Color barColor2 = new Color(192, 80, 77);

        renderer.setSeriesPaint(0, barColor1);
        renderer.setSeriesPaint(1, barColor2);

        plot.setRenderer(renderer);

        return chart;
    }
}
