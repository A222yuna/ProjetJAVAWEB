package tn.esprit.mindconnect.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import tn.esprit.mindconnect.services.ActiviteService;
import tn.esprit.mindconnect.services.AvisService;
import tn.esprit.mindconnect.services.GeminiAIService;
import tn.esprit.mindconnect.services.GoogleCalendarService;
import tn.esprit.mindconnect.services.ProgrammeService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le tableau de bord patient avec statistiques et IA.
 * Gère l'affichage des informations et des statistiques spécifiques au patient.
 */
public class PatientDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label activeProgramsLabel;
    @FXML
    private Label completedActivitiesLabel;
    @FXML
    private Label streakLabel;
    @FXML
    private Label satisfactionLabel;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Label bestStreakLabel;
    @FXML
    private Label currentLevelLabel;
    @FXML
    private Label nextGoalLabel;
    @FXML
    private Text quoteText;
    @FXML
    private Label quoteAuthor;
    @FXML
    private Button refreshQuoteBtn;
    @FXML
    private ComboBox<String> moodComboBox;
    @FXML
    private ComboBox<String> timeComboBox;
    @FXML
    private Button getAdviceBtn;
    @FXML
    private Text adviceText;
    @FXML
    private Button analyzeProgressBtn;
    @FXML
    private Text progressAnalysisText;
    @FXML
    private Button addToCalendarBtn;

    private ProgrammeService programmeService = new ProgrammeService();
    private ActiviteService activiteService = new ActiviteService();
    private AvisService avisService = new AvisService();
    private GeminiAIService aiService = new GeminiAIService();
    private GoogleCalendarService calendarService = new GoogleCalendarService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupButtonActions();
        loadUserStatistics();
        loadAIQuote();
    }

    /**
     * Configure les ComboBoxes.
     */
    private void setupComboBoxes() {
        // Mood options
        moodComboBox.getItems().addAll(
                "😊 Heureux(se)",
                "😐 Neutre",
                "😔 Triste",
                "😰 Anxieux(se)",
                "😴 Fatigué(e)",
                "🤔 Stressé(e)",
                "😌 Calme",
                "🔥 Motivé(e)"
        );
        moodComboBox.setValue("😐 Neutre");

        // Time options
        timeComboBox.getItems().addAll(
                "5-10 minutes",
                "15-30 minutes",
                "30-60 minutes",
                "1-2 heures",
                "Plus de 2 heures"
        );
        timeComboBox.setValue("15-30 minutes");
    }

    /**
     * Configure les actions des boutons.
     */
    private void setupButtonActions() {
        refreshQuoteBtn.setOnAction(e -> loadAIQuote());
        getAdviceBtn.setOnAction(e -> loadPersonalizedAdvice());
        analyzeProgressBtn.setOnAction(e -> analyzeProgress());
        addToCalendarBtn.setOnAction(e -> addToGoogleCalendar());
    }

    /**
     * Charge les statistiques de l'utilisateur.
     */
    private void loadUserStatistics() {
        try {
            // Statistiques simulées - à remplacer avec de vraies données utilisateur
            int activePrograms = programmeService.getAll().size();
            int completedActivities = activiteService.getAll().size();
            int streak = calculateStreak();
            double avgSatisfaction = calculateAverageSatisfaction();
            int totalTime = calculateTotalTime();
            int bestStreak = calculateBestStreak();
            String currentLevel = calculateCurrentLevel();
            String nextGoal = calculateNextGoal();

            activeProgramsLabel.setText(String.valueOf(activePrograms));
            completedActivitiesLabel.setText(String.valueOf(completedActivities));
            streakLabel.setText(String.valueOf(streak));
            satisfactionLabel.setText(String.format("%.1f/5", avgSatisfaction));
            totalTimeLabel.setText(totalTime + "h");
            bestStreakLabel.setText(bestStreak + " jours");
            currentLevelLabel.setText(currentLevel);
            nextGoalLabel.setText(nextGoal);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            setDefaultStats();
        }
    }

    /**
     * Définit les statistiques par défaut.
     */
    private void setDefaultStats() {
        activeProgramsLabel.setText("0");
        completedActivitiesLabel.setText("0");
        streakLabel.setText("0");
        satisfactionLabel.setText("0.0/5");
        totalTimeLabel.setText("0h");
        bestStreakLabel.setText("0 jours");
        currentLevelLabel.setText("Débutant");
        nextGoalLabel.setText("1 jour");
    }

    /**
     * Calcule le nombre de jours consécutifs d'activité.
     */
    private int calculateStreak() {
        return new Random().nextInt(30) + 1;
    }

    /**
     * Calcule la meilleure série.
     */
    private int calculateBestStreak() {
        return new Random().nextInt(50) + 10;
    }

    /**
     * Calcule le temps total d'activités.
     */
    private int calculateTotalTime() {
        return new Random().nextInt(100) + 10;
    }

    /**
     * Calcule le niveau actuel.
     */
    private String calculateCurrentLevel() {
        String[] levels = {"Débutant", "Intermédiaire", "Avancé", "Expert"};
        return levels[new Random().nextInt(levels.length)];
    }

    /**
     * Calcule le prochain objectif.
     */
    private String calculateNextGoal() {
        return (new Random().nextInt(20) + 5) + " jours";
    }

    /**
     * Calcule la moyenne de satisfaction.
     */
    private double calculateAverageSatisfaction() {
        try {
            var allAvis = avisService.getAll();
            if (allAvis.isEmpty()) return 0.0;

            return allAvis.stream()
                    .mapToInt(avis -> avis.getNote())
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            return 4.5; // Valeur par défaut
        }
    }

    /**
     * Charge une citation inspirante générée par l'IA Gemini.
     */
    private void loadAIQuote() {
        quoteText.setText("Génération de votre inspiration personnalisée...");

        new Thread(() -> {
            try {
                String quote = aiService.generateInspirationalQuote();
                javafx.application.Platform.runLater(() -> {
                    quoteText.setText(quote);
                    quoteAuthor.setText("- MindConnect AI");
                });
            } catch (Exception e) {
                System.err.println("Erreur lors de la génération de la citation: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
                    quoteText.setText("Le bien-être est un voyage, pas une destination. Chaque petit pas compte.");
                    quoteAuthor.setText("- MindConnect");
                });
            }
        }).start();
    }

    /**
     * Charge des conseils personnalisés basés sur l'humeur et le temps.
     */
    private void loadPersonalizedAdvice() {
        String mood = moodComboBox.getValue();
        String time = timeComboBox.getValue();

        adviceText.setText("Génération de vos conseils personnalisés...");

        new Thread(() -> {
            try {
                String moodAdvice = aiService.generateMoodBasedAdvice(mood);
                String activitySuggestion = aiService.suggestActivities(time, "bien-être", "moyen");

                String fullAdvice = moodAdvice + "\n\n" + activitySuggestion;

                javafx.application.Platform.runLater(() -> {
                    adviceText.setText(fullAdvice);
                });
            } catch (Exception e) {
                System.err.println("Erreur lors de la génération des conseils: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
                    adviceText.setText("Prenez 5 minutes pour respirer profondément. Essayez une marche de 10 minutes pour vous ressourcer.");
                });
            }
        }).start();
    }

    /**
     * Analyse les progrès avec l'IA.
     */
    private void analyzeProgress() {
        try {
            int completedActivities = Integer.parseInt(completedActivitiesLabel.getText());
            int streak = Integer.parseInt(streakLabel.getText());
            // Fix parsing error: replace comma with period for decimal parsing
            String satisfactionText = satisfactionLabel.getText().replace("/5", "").replace(",", ".");
            double satisfaction = Double.parseDouble(satisfactionText);

            progressAnalysisText.setText("Analyse de vos progrès en cours...");

            new Thread(() -> {
                try {
                    String analysis = aiService.analyzeProgress(completedActivities, streak, satisfaction);
                    String motivation = aiService.generateMotivation("Maissa", streak, "bien-être continu");

                    String fullAnalysis = analysis + "\n\n" + motivation;

                    javafx.application.Platform.runLater(() -> {
                        progressAnalysisText.setText(fullAnalysis);
                    });
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'analyse: " + e.getMessage());
                    javafx.application.Platform.runLater(() -> {
                        progressAnalysisText.setText("Félicitations pour vos " + streak + " jours consécutifs! Continuez comme ça, vous êtes sur la bonne voie. Essayez de maintenir cette régularité pour de meilleurs résultats.");
                    });
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse des progrès: " + e.getMessage());
        }
    }

    /**
     * Ajoute au calendrier Google.
     */
    private void addToGoogleCalendar() {
        try {
            // Ajouter la première activité de demain
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
            calendarService.addActivityQuick("Session MindConnect", tomorrow);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout au calendrier: " + e.getMessage());
        }
    }
}
