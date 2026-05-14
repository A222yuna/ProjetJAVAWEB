package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Integer id;
    private User auteur;
    private String auteurRole;
    private String titre;
    private String contenu;
    private String categorie;
    private int nbLikes = 0;
    private LocalDateTime date;
    private List<Commentaire> commentaires;
}
