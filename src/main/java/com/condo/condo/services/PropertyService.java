package com.condo.condo.services;

import com.condo.condo.dtos.PropertyDto;
import com.condo.condo.models.Property;
import com.condo.condo.payloads.ApiResponse;
import com.condo.condo.payloads.PropertyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PropertyService {
    ResponseEntity<ApiResponse<String>> createProperty(PropertyDto propertydto);
    ResponseEntity<ApiResponse<String>> deleteProperty(Long propertyId);

    ResponseEntity<ApiResponse<String>> bookProperty(Long propertyId);

    ResponseEntity<ApiResponse<String>> cancelBooking(Long bookingId);
    ResponseEntity<ApiResponse<String>> approveBooking(Long bookingId);
    ResponseEntity<ApiResponse<List<PropertyData>>> getAllProperty(Integer pageNumber, Integer pageSize);
    ResponseEntity<ApiResponse<List<PropertyData>>> searchProperty(String keyword);
    ResponseEntity<ApiResponse<PropertyData>> viewPropertyDetail(Long id);
    ResponseEntity<ApiResponse<String>> updateProperty(Long id, PropertyDto dto);
}
