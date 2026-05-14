package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumNotification {
    private Integer id;
    private User recipient;
    private Commentaire comment;
    private Post post;
    private boolean isRead = false;
    private LocalDateTime createdAt;
}
