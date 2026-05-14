package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumReport {
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_RESOLVED = "resolved";

    public static final String ACTION_DISMISSED = "dismissed";
    public static final String ACTION_HIDDEN = "hidden";
    public static final String ACTION_UNHIDDEN = "unhidden";
    public static final String ACTION_DELETED = "deleted";

    private Integer id;
    private User reporter;
    private Post targetPost;
    private Commentaire targetComment;
    private String reason;
    private String details;
    private String status = STATUS_OPEN;
    private String resolutionAction;
    private User resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
