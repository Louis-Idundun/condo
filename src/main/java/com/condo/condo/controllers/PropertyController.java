package com.condo.condo.controllers;

import com.condo.condo.dtos.PropertyDto;
import com.condo.condo.payloads.ApiResponse;
import com.condo.condo.payloads.PropertyData;
import com.condo.condo.services.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(
        name = "Property",
        description = "REST APIs allowing all property related operations"
)
public class PropertyController {
    private final PropertyService propertyservice;

    @PostMapping("/property")
    @Operation(summary = "Post property",
            description = "Allows an owner to post his property for rent")
    public ResponseEntity<ApiResponse<String>> createProperty(@RequestBody PropertyDto dto){
        return propertyservice.createProperty(dto);
    }

    @PostMapping("/property/{propertyId}/bookProperty")
    @Operation(summary = "Rent property",
            description = "Allows a customer to initiate rent for a propertys")
    public ResponseEntity<ApiResponse<String>> bookProperty(@PathVariable Long propertyId){
        return propertyservice.bookProperty(propertyId);
    }

    @DeleteMapping("/property/{propertyId}/cancel")
    @Operation(summary = "Cancel booking",
            description = "A user can cancel a rent initiated if no longer interested")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long propertyId){
        return propertyservice.cancelBooking(propertyId);
    }

    @PutMapping("/property/{bookingId}/approve")
    @Operation(summary = "Approve booking",
            description = "Allows an owner to approve a rent initiated for his property after agreement with the customer")
    public ResponseEntity<ApiResponse<String>> approveBooking(@PathVariable Long bookingId){
        return propertyservice.approveBooking(bookingId);
    }

    @GetMapping("/property/all")
    @Operation(summary = "Property listing",
            description = "Displays a paginated list of all available property from the most recent")
    public ResponseEntity<ApiResponse<List<PropertyData>>> getAllPropertys(@RequestParam(defaultValue = "0") Integer pageNumber,
                                                                           @RequestParam(defaultValue = "10") Integer pageSize){
        return propertyservice.getAllProperty(pageNumber, pageSize);
    }

    @GetMapping("/property/search")
    @Operation(summary = "Property search",
            description = "Searches for property by a criterion through the keywords supplied")
    public ResponseEntity<ApiResponse<List<PropertyData>>> searchProperty(@RequestParam String keyword){
        return propertyservice.searchProperty(keyword);
    }

    @GetMapping("/property/{propertyId}/view")
    @Operation(summary = "View property detail",
            description = "Display full information of a property")
    public ResponseEntity<ApiResponse<PropertyData>> viewPropertyDetail(@PathVariable Long propertyId){
        return propertyservice.viewPropertyDetail(propertyId);
    }

    @PutMapping("/property/{propertyId}/update")
    @Operation(summary = "Update property",
            description = "Allows an owner to update the details of a posted property")
    public ResponseEntity<ApiResponse<String>> updateProperty(@PathVariable Long propertyId,
                                                           @RequestBody PropertyDto dto){
        return propertyservice.updateProperty(propertyId, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/property/{propertyId}/delete")
    @Operation(summary = "Remove property",
            description = "This endpoint allows only the owner or an admin to remove a property posted on the platform")
    public ResponseEntity<ApiResponse<String>> deleteProperty(@PathVariable Long propertyId){
        return propertyservice.deleteProperty(propertyId);
    }
}
