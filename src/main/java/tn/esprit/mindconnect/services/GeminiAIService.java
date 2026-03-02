package tn.esprit.mindconnect.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Service pour l'intégration avec l'API Google Gemini.
 * Permet de générer du contenu personnalisé pour le bien-être mental.
 */
public class GeminiAIService {
    
    private static final String API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    /**
     * Génère une citation inspirante personnalisée.
     */
    public String generateInspirationalQuote() {
        String prompt = "Génère une citation inspirante et positive courte (max 150 caractères) pour le bien-être mental et le développement personnel. Réponds uniquement avec la citation, sans guillemets ni auteur.";
        return generateContent(prompt);
    }
    
    /**
     * Génère des conseils personnalisés basés sur l'humeur de l'utilisateur.
     */
    public String generateWellnessAdvice(String mood, String context) {
        String prompt = String.format(
            "En tant qu'expert en bien-être mental, donne un conseil personnalisé et encourageant pour quelqu'un qui se sent %s. Contexte: %s. Sois empathique et donne des conseils pratiques (max 200 caractères).",
            mood, context
        );
        return generateContent(prompt);
    }
    
    /**
     * Génère une suggestion d'activité basée sur les préférences.
     */
    public String generateActivitySuggestion(String preferences, String availableTime) {
        String prompt = String.format(
            "Suggère une activité de bien-être spécifique basée sur ces préférences: %s. Temps disponible: %s. Sois précis et pratique (max 150 caractères).",
            preferences, availableTime
        );
        return generateContent(prompt);
    }
    
    /**
     * Génère des conseils basés sur l'humeur actuelle.
     */
    public String generateMoodBasedAdvice(String mood) {
        String prompt = String.format(
            "Donne un conseil immédiat et pratique pour quelqu'un qui se sent %s. Sois direct, positif et actionnable (max 180 caractères).",
            mood.toLowerCase()
        );
        return generateContent(prompt);
    }
    
    /**
     * Suggère des activités basées sur le temps disponible et les préférences.
     */
    public String suggestActivities(String timeAvailable, String preferences, String energyLevel) {
        String prompt = String.format(
            "Suggère 2-3 activités de bien-être parfaites pour %s avec préférences %s et niveau d'énergie %s. Sois précis et varié (max 200 caractères).",
            timeAvailable, preferences, energyLevel
        );
        return generateContent(prompt);
    }
    
    /**
     * Analyse les progrès et donne des insights personnalisés.
     */
    public String analyzeProgress(int completedActivities, int streakDays, double avgSatisfaction) {
        String prompt = String.format(
            "Analyse ces progrès bien-être: %d activités complétées, %s jours consécutifs, satisfaction %.1f/5. Donne un insight motivant et une suggestion d'amélioration (max 250 caractères).",
            completedActivities, streakDays, avgSatisfaction
        );
        return generateContent(prompt);
    }
    
    /**
     * Génère un message de motivation personnalisé.
     */
    public String generateMotivation(String userName, int currentStreak, String goal) {
        String prompt = String.format(
            "Motive %s qui a une série de %s jours avec l'objectif '%s'. Sois inspirant et personnel (max 180 caractères).",
            userName, currentStreak, goal
        );
        return generateContent(prompt);
    }
    
    /**
     * Génère du contenu en utilisant l'API Gemini.
     */
    private String generateContent(String prompt) {
        try {
            System.out.println("🤖 Gemini API - Starting request...");
            System.out.println("📝 Prompt: " + prompt);
            System.out.println("🔗 URL: " + BASE_URL);
            
            URL url = new URL(BASE_URL + "?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Construire le corps de la requête
            String requestBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n")
            );
            
            System.out.println("📦 Request body: " + requestBody);
            
            // Envoyer la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Lire la réponse
            int responseCode = conn.getResponseCode();
            System.out.println("📡 Response code: " + responseCode);
            
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    String fullResponse = response.toString();
                    System.out.println("✅ Raw API Response: " + fullResponse);
                    
                    // Parser la réponse JSON correctement
                    try {
                        JSONObject jsonResponse = new JSONObject(fullResponse);
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        
                        if (candidates.length() > 0) {
                            JSONObject firstCandidate = candidates.getJSONObject(0);
                            JSONObject content = firstCandidate.getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            
                            if (parts.length() > 0) {
                                JSONObject firstPart = parts.getJSONObject(0);
                                String generatedText = firstPart.getString("text");
                                
                                // Nettoyer le texte des caractères d'échappement
                                generatedText = generatedText.replace("\\n", "\n")
                                                              .replace("\\\"", "\"")
                                                              .replace("\\'", "'")
                                                              .replace("\\\\", "\\");
                                
                                System.out.println("🎉 Generated content: " + generatedText);
                                return generatedText;
                            }
                        }
                        
                        System.out.println("⚠️ Structure JSON inattendue");
                        return "Réponse non disponible";
                        
                    } catch (Exception jsonEx) {
                        System.out.println("❌ Erreur parsing JSON: " + jsonEx.getMessage());
                        System.out.println("🔍 Tentative de parsing manuel...");
                        
                        // Fallback: essayer le parsing manuel
                        if (fullResponse.contains("\"text\":\"")) {
                            int textStart = fullResponse.indexOf("\"text\":\"") + 8;
                            int textEnd = fullResponse.indexOf("\"", textStart);
                            if (textEnd > textStart) {
                                String generatedText = fullResponse.substring(textStart, textEnd)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\'", "'");
                                System.out.println("🎉 Generated content (fallback): " + generatedText);
                                return generatedText;
                            }
                        }
                        
                        return "Erreur de parsing";
                    }
                }
            } else {
                // Lire le message d'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    
                    System.out.println("❌ API Error Response: " + errorResponse.toString());
                    System.out.println("🚨 Erreur API Gemini - Code: " + responseCode);
                    System.out.println("📋 Détail erreur: " + errorResponse.toString());
                }
                
                return "Erreur lors de la génération";
            }
            
        } catch (Exception e) {
            System.out.println("💥 Exception during API call: " + e.getMessage());
            e.printStackTrace();
            return "Erreur de connexion";
        }
    }
    
    /**
     * Extrait le texte de la réponse JSON de Gemini en utilisant du parsing manuel.
     */
    private String extractTextFromResponse(String jsonResponse) {
        try {
            // Chercher le pattern "text":"..." dans la réponse
            int textIndex = jsonResponse.indexOf("\"text\":\"");
            if (textIndex != -1) {
                int startIndex = textIndex + 8; // Longueur de "text":"
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    String extractedText = jsonResponse.substring(startIndex, endIndex);
                    // Unescape les caractères JSON
                    return extractedText.replace("\\\"", "\"")
                                      .replace("\\n", "\n")
                                      .replace("\\r", "\r")
                                      .replace("\\t", "\t")
                                      .replace("\\\\", "\\");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing de la réponse: " + e.getMessage());
        }
        return getFallbackResponse();
    }
    
    /**
     * Retourne une réponse de secours en cas d'erreur.
     */
    private String getFallbackResponse() {
        String[] fallbackResponses = {
            "Prenez un moment pour respirer profondément et vous recentrer.",
            "Chaque petit pas vers le bien-être compte énormément.",
            "Soyez gentil(ille) avec vous-même aujourd'hui.",
            "Le bien-être est un voyage, pas une destination."
        };
        
        return fallbackResponses[new java.util.Random().nextInt(fallbackResponses.length)];
    }
    
    /**
     * Test la connexion à l'API Gemini.
     */
    public boolean testConnection() {
        try {
            String testResponse = generateContent("Dis bonjour en une phrase.");
            return testResponse != null && !testResponse.isEmpty() && !testResponse.equals(getFallbackResponse());
        } catch (Exception e) {
            System.err.println("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
}
