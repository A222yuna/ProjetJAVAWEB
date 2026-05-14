package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Integer id;
    private String contenuMessage;
    private LocalDateTime dateMessage;
    private boolean estLu = false;
    private User expediteur;
    private String expediteurRole;
    private User destinataire;
    private String destinataireRole;
    private Conversation conversation;
    
    // Add IDs for easier DB mapping
    private Integer expediteur_id_user;
    private Integer destinataire_id_user;
}
