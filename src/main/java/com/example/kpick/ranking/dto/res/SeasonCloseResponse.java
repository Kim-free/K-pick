package com.example.kpick.ranking.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonCloseResponse {
    private String message;
    private int affectedProfileCount;
    private int decayRate;
}
