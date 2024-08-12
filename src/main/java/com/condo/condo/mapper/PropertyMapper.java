package com.condo.condo.mapper;

import com.condo.condo.dtos.PropertyDto;
import com.condo.condo.payloads.PropertyData;
import com.condo.condo.models.Property;

public class PropertyMapper {
        public static Property mapToProperty(PropertyDto propertyDto, Property property){
            property.setDescription(propertyDto.description());
            property.setAddress(propertyDto.address());
            property.setRooms(propertyDto.roomCount());

            return property;
        }

        public static PropertyData mapToPropertyData(Property property, PropertyData propertyData){
            propertyData.setId(property.getId());
            propertyData.setDescription(property.getDescription());
            propertyData.setAddress(property.getAddress());
            propertyData.setRoomCount(property.getRooms());

            return propertyData;
        }

    }
