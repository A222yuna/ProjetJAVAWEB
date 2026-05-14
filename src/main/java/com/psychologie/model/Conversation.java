package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    private Integer id;
    private LocalDate dateCreation;
    private String statutConversation;
    private boolean archiverConversation = false;
    private List<Message> messages;
}
