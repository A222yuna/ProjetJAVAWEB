package com.psychologie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsyCabinet {
    private Integer id;
    private User psychologue;
    private Cabinet cabinet;
}
