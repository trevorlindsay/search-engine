import java.util.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;


public class Chart extends Application {

    private HashMap<String, HashMap<Integer, Integer>> countMap;
    public LineChart<String, Number> lineChart;

    @Override
    public void start(Stage stage) {

        InvertedIndex index = new InvertedIndex("index/inverted_index.txt");
        this.countMap = index.search("cat,dog,bird");

        stage.setTitle("n-Gram Viewer");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Year");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("n-Gram Viewer");

        for (String ngram : countMap.keySet()) {

            HashMap<Integer, Integer> counts = countMap.get(ngram);
            XYChart.Series series = new XYChart.Series();
            series.setName(ngram);

            ArrayList<Integer> years = new ArrayList<>(counts.keySet());
            Collections.sort(years);

            for (Integer year : years) {
                series.getData().add(new XYChart.Data(year.toString(), counts.get(year)));
            }

            lineChart.getData().add(new XYChart.Series(ngram, series.getData()));
        }

        Scene scene  = new Scene(lineChart, 1000, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}