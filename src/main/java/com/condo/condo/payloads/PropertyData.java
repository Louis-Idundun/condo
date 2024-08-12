package com.condo.condo.payloads;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Data
public class PropertyData {
    private Long id;

    private String name;

    private String description;

    private String address;

    private Integer roomCount;

}
