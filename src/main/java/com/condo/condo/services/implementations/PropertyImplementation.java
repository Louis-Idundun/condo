package com.condo.condo.services.implementations;

import com.condo.condo.dtos.PropertyDto;
import com.condo.condo.dtos.UserUtil;
import com.condo.condo.enums.BookingStatus;
import com.condo.condo.enums.Role;
import com.condo.condo.exceptions.FlexisafException;
import com.condo.condo.mapper.PropertyMapper;
import com.condo.condo.models.Booking;
import com.condo.condo.models.Property;
import com.condo.condo.models.User;
import com.condo.condo.payloads.ApiResponse;
import com.condo.condo.payloads.PropertyData;
import com.condo.condo.repositories.BookingRepository;
import com.condo.condo.repositories.PropertyRepository;
import com.condo.condo.repositories.UserRepository;
import com.condo.condo.services.EmailService;
import com.condo.condo.services.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class PropertyImplementation implements PropertyService {

        private final UserRepository userRepository;
        private final PropertyRepository propertyRepository;
        private final BookingRepository bookingRepository;
        private final EmailService emailService;
        @Override
        @Transactional
        public ResponseEntity<ApiResponse<String>> createProperty(PropertyDto propertyDto) {
            userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Property property = PropertyMapper.mapToProperty(propertyDto, new Property());
            property.setDescription(propertyDto.description());
            propertyRepository.save(property);
            return new ResponseEntity<>(new ApiResponse<>(
                    "Property posted successfully", HttpStatus.CREATED), HttpStatus.CREATED);
        }

        @Override
        @Transactional
        public ResponseEntity<ApiResponse<String>> bookProperty(Long propertyId) {
            User user = userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Property property = propertyRepository.findById(propertyId).orElseThrow(
                    () -> new FlexisafException("Property not found")
            );
            if(property.isAvailable()){
                User owner = property.getOwner();

                Booking booking = new Booking();
                booking.setCustomer(user);
                booking.setProperty(property);
                bookingRepository.save(booking);

                return new ResponseEntity<>(new ApiResponse<>(String.format(
                        "You have successfully initiated a booking for the property %s", property.getBookings()),
                        HttpStatus.OK), HttpStatus.OK);
            } else{
                return new ResponseEntity<>(new ApiResponse<>("This property is not available",
                        HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }
        }

        @Override
        @Transactional
        public ResponseEntity<ApiResponse<String>> cancelBooking(Long bookingId) {
            User user = userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                    () -> new FlexisafException("Property booking not found"));
            if(user.equals(booking.getCustomer()) || user.equals(booking.getOwner())){
                Property property = booking.getProperty();
                if(!property.isAvailable()){
                    property.setAvailable(true);
                    propertyRepository.save(property);
                }
                if(user.equals(property.getOwner())){
                    emailService.sendEmail(booking.getCustomer().getEmailAddress(), "Rent Cancellation",
                            String.format("Dear %s, %n%n This is to notify you that the owner " +
                                            "of the property you booked has cancelled your booking.%n%n You can check V-Space " +
                                            "for more options.%n%n Best regards, V-Space.",
                                    booking.getCustomer().getFirstName()));
                }
                if(user.equals(booking.getCustomer())){
                    emailService.sendEmail(booking.getOwner().getEmailAddress(), "Rent Cancellation",
                            String.format("Dear %s, %n%n This is to notify you that your customer %s " +
                                            "has cancelled his booking for your property %s.%n%n Best regards, V-Space.",
                                    property.getOwner().getFirstName(), booking.getCustomer().getFirstName(),
                                    property.getId()));
                }
                bookingRepository.delete(booking);
                return new ResponseEntity<>(new ApiResponse<>("You have successfully cancelled this booking",
                        HttpStatus.OK), HttpStatus.OK);
            } else{
                return new ResponseEntity<>(new ApiResponse<>("You can not cancel this booking",
                        HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }
        }

        @Override
        @Transactional
        public ResponseEntity<ApiResponse<String>> approveBooking(Long bookingId) {
            User user = userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                    () -> new FlexisafException("Property booking not found"));
            if(user.equals(booking.getOwner())){
                if(booking.getStatus().CONFIRMED == null){
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                    Property property = booking.getProperty();
                    property.setAvailable(false);
                    propertyRepository.save(property);

                    emailService.sendEmail(booking.getCustomer().getEmailAddress(), "Rent Approval",
                            (String.format("Dear %s, %n%n Your booking for the property %s has been approved" +
                                            " by the owner.%n%n You can continue further processes with the owner.%n%n Please do not " +
                                            "fail to contact us if anything goes wrong.%n%n Best regards, V-Space",
                                    booking.getCustomer().getFirstName(), property.getId())));


                    return new ResponseEntity<>(new ApiResponse<>("You have successfully approved this booking",
                            HttpStatus.OK), HttpStatus.OK);
                } else{
                    return new ResponseEntity<>(new ApiResponse<>("This booking is already approved",
                            HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>(new ApiResponse<>("You can not approve this booking",
                    HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }

        @Override
        @Transactional(readOnly = true)
        public ResponseEntity<ApiResponse<List<PropertyData>>> getAllProperty(Integer pageNumber, Integer pageSize) {
            pageNumber = pageNumber != null && pageNumber >= 0? pageNumber:0;
            pageSize = pageSize != null && pageSize > 0? pageSize:DEFAULT_PAGE_SIZE;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            List<PropertyData> propertyList = propertyRepository.findByIsAvailableTrueOrderByCreatedAtDesc(pageable)
                    .stream().map( data -> PropertyMapper.mapToPropertyData(
                            data, new PropertyData())).collect(Collectors.toList());
            return new ResponseEntity<>(new ApiResponse<>(propertyList, "All property retrieved successfully")
                    , HttpStatus.OK);
        }

        @Override
        @Transactional(readOnly = true)
        public ResponseEntity<ApiResponse<List<PropertyData>>> searchProperty(String keyword) {
            userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));

            List<PropertyData> propertyData = propertyRepository.findByDescriptionContainsIgnoreCase(keyword)
                    .stream().map(data -> PropertyMapper.mapToPropertyData(data, new PropertyData()))
                    .collect(Collectors.toList());
            if(!propertyData.isEmpty()){
                return new ResponseEntity<>(new ApiResponse<>(propertyData, "Successful"), HttpStatus.OK);
            } else{
                return new ResponseEntity<>(new ApiResponse<>( "No results were found", HttpStatus.NOT_FOUND)
                        , HttpStatus.NOT_FOUND);
            }
        }

        @Override
        @Transactional(readOnly = true)
        public ResponseEntity<ApiResponse<PropertyData>> viewPropertyDetail(Long id) {
            userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Property property = propertyRepository.findById(id).orElseThrow(
                    () -> new FlexisafException("Property not found"));
            PropertyData propertyData = PropertyMapper.mapToPropertyData(property, new PropertyData());
            return new ResponseEntity<>(new ApiResponse<>(propertyData, "Property details retrieved successfully")
                    , HttpStatus.OK);
        }

        @Override
        @Transactional
        public ResponseEntity<ApiResponse<String>> updateProperty(Long id, PropertyDto dto) {
            User user = userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Property property = propertyRepository.findById(id).orElseThrow(
                    () -> new FlexisafException("Property not found"));

            if(!property.getOwner().equals(user)){
                return new ResponseEntity<>(new ApiResponse<>("You are not authorized to update this property",
                        HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            } else{
                propertyRepository.save(PropertyMapper.mapToProperty(dto, property));
                return new ResponseEntity<>(new ApiResponse<>("Property updated successfully", HttpStatus.OK),
                        HttpStatus.OK);
            }
        }

        @Override
        @Transactional
        public ResponseEntity<ApiResponse<String>> deleteProperty(Long id) {
            User user = userRepository.findByEmailAddress(UserUtil.getLoginUser()).orElseThrow(
                    () -> new FlexisafException("User not found"));
            Property property = propertyRepository.findById(id).orElseThrow(
                    () -> new FlexisafException("Property not found"));

            if(!property.getOwner().equals(user) || !user.getRole().equals(Role.ADMIN)){
                return new ResponseEntity<>(new ApiResponse<>("You are not authorized to remove this property",
                        HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            } else{
                propertyRepository.delete(property);
                return new ResponseEntity<>(new ApiResponse<>("Property removed successfully", HttpStatus.OK),
                        HttpStatus.OK);
            }
        }
    }
