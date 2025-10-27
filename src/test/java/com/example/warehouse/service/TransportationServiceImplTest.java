package com.example.warehouse.service;

import com.example.warehouse.dto.TransportationDTO;
import com.example.warehouse.entity.Transportation;
import com.example.warehouse.entity.Item;
import com.example.warehouse.entity.Vehicle;
import com.example.warehouse.entity.User;
import com.example.warehouse.entity.Storage;
import com.example.warehouse.enumeration.TransportStatus;
import com.example.warehouse.exception.TransportationNotFoundException;
import com.example.warehouse.exception.ItemNotFoundException;
import com.example.warehouse.exception.VehicleNotFoundException;
import com.example.warehouse.exception.UserNotFoundException;
import com.example.warehouse.exception.StorageNotFoundException;
import com.example.warehouse.exception.OperationNotAllowedException;
import com.example.warehouse.mapper.TransportationMapper;
import com.example.warehouse.repository.TransportationRepository;
import com.example.warehouse.repository.ItemRepository;
import com.example.warehouse.repository.VehicleRepository;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import org.springframework.data.domain.*;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class TransportationServiceImplTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private TransportationMapper transportationMapper;

    @InjectMocks
    private TransportationServiceImpl transportationService;

    private TransportationDTO transportationDTO;
    private Transportation transportation;

    @BeforeEach
    void setUp() {
        transportationDTO = new TransportationDTO(
                null,  // id can be null for a new transportation
                1L, // itemId
                1L, // vehicleId
                1L, // driverId
                1L, // fromStorageId
                1L, // toStorageId
                TransportStatus.PLANNED, // Status
                LocalDateTime.now().plusDays(1), // scheduledDeparture
                null, // actualDeparture
                LocalDateTime.now().plusDays(2), // scheduledArrival
                null, // actualArrival
                LocalDateTime.now() // createdAt
        );

        transportation = new Transportation();
        transportation.setId(1L);
        transportation.setItem(new Item());
        transportation.setVehicle(new Vehicle());
        transportation.setDriver(new User());
        transportation.setFromStorage(new Storage());
        transportation.setToStorage(new Storage());
        transportation.setStatus(TransportStatus.PLANNED);
        transportation.setScheduledDeparture(LocalDateTime.now().plusDays(1));
        transportation.setScheduledArrival(LocalDateTime.now().plusDays(2));
    }
    

    @Test
    void testCreateTransportation_ItemNotFound() {
        when(itemRepository.findById(transportationDTO.itemId())).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> transportationService.create(transportationDTO))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Item not found with ID");
    }


    @Test
    void testGetTransportationById_Success() {
        when(transportationRepository.findById(1L)).thenReturn(java.util.Optional.of(transportation));
        when(transportationMapper.toDTO(transportation)).thenReturn(transportationDTO);

        TransportationDTO foundTransportation = transportationService.getById(1L);

        assertThat(foundTransportation).isNotNull();
        assertThat(foundTransportation.itemId()).isEqualTo(1L);
    }

    @Test
    void testGetTransportationById_NotFound() {
        when(transportationRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> transportationService.getById(1L))
                .isInstanceOf(TransportationNotFoundException.class)
                .hasMessageContaining("Transportation not found with ID: 1");
    }

    @Test
    void testUpdateTransportation_Success() {
        Item item = new Item();
        item.setId(1L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);

        User driver = new User();
        driver.setId(1L);

        Storage fromStorage = new Storage();
        fromStorage.setId(1L);

        Storage toStorage = new Storage();
        toStorage.setId(2L);

        transportation = new Transportation();
        transportation.setId(1L);
        transportation.setItem(item);
        transportation.setVehicle(vehicle);
        transportation.setDriver(driver);
        transportation.setFromStorage(fromStorage);
        transportation.setToStorage(toStorage);
        transportation.setStatus(TransportStatus.PLANNED);
        transportation.setScheduledDeparture(LocalDateTime.now().plusDays(1));
        transportation.setScheduledArrival(LocalDateTime.now().plusDays(2));

        when(transportationRepository.findById(1L)).thenReturn(java.util.Optional.of(transportation));
        when(transportationRepository.save(transportation)).thenReturn(transportation);

        transportationDTO = new TransportationDTO(
                1L, 1L, 1L, 1L, 1L, 2L, TransportStatus.IN_TRANSIT, LocalDateTime.now(), LocalDateTime.now().plusHours(2), null, null, LocalDateTime.now()
        );

        transportationService.update(1L, transportationDTO);

        verify(transportationRepository, times(1)).save(transportation);
    }

    @Test
    void testDeleteTransportation_Success() {
        when(transportationRepository.findById(1L)).thenReturn(java.util.Optional.of(transportation));
        doNothing().when(transportationRepository).deleteById(1L);

        transportationService.delete(1L);

        verify(transportationRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTransportation_NotAllowed() {
        transportation.setStatus(TransportStatus.DELIVERED);

        when(transportationRepository.findById(1L)).thenReturn(java.util.Optional.of(transportation));

        assertThatThrownBy(() -> transportationService.delete(1L))
                .isInstanceOf(OperationNotAllowedException.class)
                .hasMessageContaining("Cannot delete transportation with status: DELIVERED");
    }

    @Test
    void testFindPage() {
        Page<Transportation> page = new PageImpl<>(Collections.singletonList(transportation));
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(transportationRepository.findAll(pageable)).thenReturn(page);

        Page<TransportationDTO> resultPage = transportationService.findPage(0, 10, null, null, null, null);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
    }
}
