package lab4.task1;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.Map;

public class WordLengthHistogram {
    public static void displayHistogram(Map<Integer, Integer> wordLengthCounts) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Word Length Histogram");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new ChartPanel(createChart(createDataset(wordLengthCounts))));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static CategoryDataset createDataset(Map<Integer, Integer> wordLengthCounts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Integer, Integer> entry : wordLengthCounts.entrySet()) {
            dataset.addValue(entry.getValue(), "Word Count", entry.getKey());
        }
        return dataset;
    }

    private static JFreeChart createChart(CategoryDataset dataset) {
        return ChartFactory.createBarChart(
                "Word Length Distribution",
                "Word Length",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }
}

