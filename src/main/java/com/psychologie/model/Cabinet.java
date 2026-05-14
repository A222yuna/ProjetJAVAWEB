package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cabinet {
    private Integer id;
    private String adresse;
    private String ville;
    private String horaires;
    private String description;
    private boolean valide = false;
    private boolean archive = false;
}
