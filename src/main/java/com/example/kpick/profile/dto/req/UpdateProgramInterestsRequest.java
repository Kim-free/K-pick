package com.example.kpick.profile.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateProgramInterestsRequest {
    private List<Long> programIds;
}
