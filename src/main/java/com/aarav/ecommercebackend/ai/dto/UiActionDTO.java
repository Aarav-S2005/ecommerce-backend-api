package com.aarav.ecommercebackend.ai.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UiActionDTO {
    private UiActionType type;
    private Object payload;
    public static UiActionDTO none() {
        return UiActionDTO.builder()
                .type(UiActionType.NONE)
                .payload(null)
                .build();
    }
    public static UiActionDTO of(UiActionType type, Object payload) {
        return UiActionDTO.builder()
                .type(type)
                .payload(payload)
                .build();
    }
}
