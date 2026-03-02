package tn.psy.gestioncabinet.util;

import java.util.ArrayList;
import java.util.List;

/**
 * EventBus simple pour notifier les controllers des changements de cabinets
 * Permet de rafraîchir automatiquement les TableView après ajout/modification/suppression
 */
public class CabinetEventBus {
    
    private static CabinetEventBus instance;
    private final List<Runnable> listeners = new ArrayList<>();
    
    private CabinetEventBus() {
    }
    
    public static CabinetEventBus getInstance() {
        if (instance == null) {
            instance = new CabinetEventBus();
        }
        return instance;
    }
    
    /**
     * S'abonner aux événements de changement de cabinet
     * @param listener Callback à exécuter lors d'un changement
     */
    public void subscribe(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Se désabonner des événements
     * @param listener Callback à retirer
     */
    public void unsubscribe(Runnable listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifier tous les listeners d'un changement de cabinet
     */
    public void notifyCabinetChanged() {
        // Exécuter sur le thread JavaFX
        javafx.application.Platform.runLater(() -> {
            for (Runnable listener : new ArrayList<>(listeners)) {
                try {
                    listener.run();
                } catch (Exception e) {
                    System.err.println("Erreur lors de la notification : " + e.getMessage());
                }
            }
        });
    }
}
