package com.lukeonuke.dlawfabric.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SparkError {
    private String message;
    private Long timestamp;
}
