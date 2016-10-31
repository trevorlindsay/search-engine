import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Window extends JFrame {

    private InvertedIndex invertedIndex;
    private HashMap<String, HashMap<Integer, Integer>> countMap;
    private final String[] colNames = new String[] {"Word/Phrase", "Year", "Count"};

    public Window() {
        this.invertedIndex = new InvertedIndex("index/inverted_index.txt");
        initiateUI();
    }

    private void initiateUI() {

        JPanel searchBar = new JPanel();
        searchBar.setLayout(new BoxLayout(searchBar, BoxLayout.X_AXIS));
        searchBar.setAlignmentY(TOP_ALIGNMENT);

        final JTextField searchBox = new JTextField(30);
        searchBox.setMaximumSize(searchBox.getPreferredSize());
        JButton searchButton = new JButton("Search!");

        searchBar.add(searchBox);
        searchBar.add(searchButton);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        menu.setAlignmentX(CENTER_ALIGNMENT);
        menu.add(searchBar);

        JTable table = new JTable(new Object[][] {}, colNames);
        table.setPreferredScrollableViewportSize(new Dimension(400,100));
        table.setAutoCreateRowSorter(true);
        menu.add(new JScrollPane(table));

        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(null, "Year", "Count", dataset,
                PlotOrientation.VERTICAL, true, true, true);
        ChartPanel chartpanel = new ChartPanel(chart);
        chartpanel.setDomainZoomable(true);
        chartpanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        Container contentPane = getContentPane();
        contentPane.add(menu, BorderLayout.CENTER);
        contentPane.add(chartpanel, BorderLayout.PAGE_END);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String query = searchBox.getText();
                countMap = invertedIndex.search(query);
                refreshTable(table);
                refreshChart(dataset, chart);
            }
        });

        pack();
        setTitle("N-gram Finder");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    private void refreshTable(JTable table) {

        int NUM_WORDS = countMap.keySet().size();
        int NUM_YEARS = invertedIndex.NUM_YEARS;
        int NUM_ENTRIES = NUM_WORDS * NUM_YEARS;
        int NUM_COLUMNS = colNames.length;

        Object[][] results = new Object[NUM_ENTRIES][NUM_COLUMNS];
        int counter = 0;

        List<Integer> years = new ArrayList<>(countMap.values().iterator().next().keySet());
        Collections.sort(years);

        for (String word : countMap.keySet()) {
            for (Integer year : years) {

                Object[] entry = {word, year, countMap.get(word).get(year)};
                results[counter] = entry;
                counter++;

            }
        }

        DefaultTableModel model = new DefaultTableModel(results, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
    }

    private void refreshChart(XYSeriesCollection dataset, JFreeChart chart) {

        dataset.removeAllSeries();
        List<Integer> years = new ArrayList<>(countMap.values().iterator().next().keySet());
        Collections.sort(years);

        for (String word : countMap.keySet()) {

            XYSeries series = new XYSeries(word);

            for (Integer year : years) {
                series.add(year, countMap.get(word).get(year));
            }

            dataset.addSeries(series);
        }

        XYPlot plot = chart.getXYPlot();
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setRange(years.get(0), years.get(years.size() - 1));
        chart.fireChartChanged();
    }

}
