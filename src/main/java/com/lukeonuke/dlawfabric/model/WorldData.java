package com.lukeonuke.dlawfabric.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorldData {
    private String seed;
    private Long time;
    private String type;
}
