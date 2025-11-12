package com.lukeonuke.dlawfabric.model.backend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataModel {
    private Integer id;
    private String uuid;
    private UserModel user;
    private GuildModel guild;
    private String createdAt;

    @Override
    public String toString() {
        return "DataModel{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", user=" + user +
                ", guild=" + guild +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
