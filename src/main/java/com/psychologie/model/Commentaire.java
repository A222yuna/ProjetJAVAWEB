package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commentaire {
    private Integer id;
    private Post post;
    private User auteur;
    private String auteurRole;
    private String contenu;
    private int nbLikes = 0;
    private LocalDateTime date;
    private boolean hidden = false;

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    private Commentaire parent;
    private List<Commentaire> replies;
}
