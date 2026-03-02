package tn.esprit.mindconnect.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tn.esprit.mindconnect.services.ActiviteService;
import tn.esprit.mindconnect.services.AvisService;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.net.URL;
import java.util.*;

/**
 * Contrôleur pour l'affichage des statistiques avec des graphiques.
 * Affiche des graphiques circulaires, à barres et linéaires pour visualiser les données.
 */
public class StatsController implements Initializable {

    @FXML
    private StackPane pieChartContainer;
    @FXML
    private StackPane barChartContainer;
    @FXML
    private StackPane lineChartContainer;
    @FXML
    private Label totalProgrammesLabel;
    @FXML
    private Label totalActivitesLabel;
    @FXML
    private Label totalAvisLabel;
    @FXML
    private Label noteMoyenneLabel;

    private ProgrammeService programmeService = new ProgrammeService();
    private ActiviteService activiteService = new ActiviteService();
    private AvisService avisService = new AvisService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStats();
        createCharts();
    }

    /**
     * Charge les données statistiques et met à jour les labels.
     */
    private void loadStats() {
        try {
            // Total programmes
            int totalProgrammes = programmeService.getAll().size();
            totalProgrammesLabel.setText(String.valueOf(totalProgrammes));

            // Total activités
            int totalActivites = activiteService.getAll().size();
            totalActivitesLabel.setText(String.valueOf(totalActivites));

            // Total avis et note moyenne
            var allAvis = avisService.getAll();
            totalAvisLabel.setText(String.valueOf(allAvis.size()));
            
            if (!allAvis.isEmpty()) {
                double noteMoyenne = allAvis.stream()
                        .mapToInt(avis -> avis.getNote())
                        .average()
                        .orElse(0.0);
                noteMoyenneLabel.setText(String.format("%.1f", noteMoyenne));
            } else {
                noteMoyenneLabel.setText("0.0");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    /**
     * Crée les différents graphiques.
     */
    private void createCharts() {
        createPieChart();
        createBarChart();
        createLineChart();
    }

    /**
     * Crée un graphique circulaire pour la répartition des programmes.
     */
    private void createPieChart() {
        try {
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Répartition par Type");
            
            // Données simulées - à remplacer avec de vraies données
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Méditation", 35),
                    new PieChart.Data("Thérapie", 25),
                    new PieChart.Data("Coaching", 20),
                    new PieChart.Data("Développement Personnel", 20)
            );
            
            pieChart.setData(pieChartData);
            pieChart.setLegendVisible(true);
            
            // Style
            pieChart.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            
            pieChartContainer.getChildren().clear();
            pieChartContainer.getChildren().add(pieChart);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du graphique circulaire: " + e.getMessage());
        }
    }

    /**
     * Crée un graphique à barres pour les activités par jour.
     */
    @SuppressWarnings("unchecked")
    private void createBarChart() {
        try {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            
            barChart.setTitle("Nombre d'Activités par Jour");
            xAxis.setLabel("Jour");
            yAxis.setLabel("Nombre d'activités");
            
            // Données simulées - à remplacer avec de vraies données
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.setName("Activités");
            
            series1.getData().addAll(
                    new XYChart.Data<>("Jour 1", 4),
                    new XYChart.Data<>("Jour 2", 3),
                    new XYChart.Data<>("Jour 3", 5),
                    new XYChart.Data<>("Jour 4", 2),
                    new XYChart.Data<>("Jour 5", 6),
                    new XYChart.Data<>("Jour 6", 4),
                    new XYChart.Data<>("Jour 7", 3)
            );
            
            barChart.getData().add(series1);
            barChart.setLegendVisible(false);
            
            // Style
            barChart.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            
            barChartContainer.getChildren().clear();
            barChartContainer.getChildren().add(barChart);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du graphique à barres: " + e.getMessage());
        }
    }

    /**
     * Crée un graphique linéaire pour l'évolution des avis.
     */
    private void createLineChart() {
        try {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            
            lineChart.setTitle("Évolution des Avis (30 derniers jours)");
            xAxis.setLabel("Date");
            yAxis.setLabel("Nombre d'avis");
            
            // Données simulées - à remplacer avec de vraies données
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.setName("Avis reçus");
            
            // Simuler les 7 derniers jours
            Calendar cal = Calendar.getInstance();
            for (int i = 6; i >= 0; i--) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
                String dateStr = String.format("%02d/%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1);
                series1.getData().add(new XYChart.Data<>(dateStr, (int)(Math.random() * 10) + 2));
                cal.add(Calendar.DAY_OF_MONTH, 1); // Reset
            }
            
            lineChart.getData().add(series1);
            lineChart.setLegendVisible(false);
            
            // Style
            lineChart.getStylesheets().add(getClass().getResource("/tn/esprit/mindconnect/style.css").toExternalForm());
            
            lineChartContainer.getChildren().clear();
            lineChartContainer.getChildren().add(lineChart);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du graphique linéaire: " + e.getMessage());
        }
    }
}
