package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.AddressRequest;
import com.uade.tpo.demo.exceptions.DuplicateException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.AddressRepository;
import com.uade.tpo.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void getAddresses_deberiaRetornarListaCompleta() {
        // Arrange
        var addresses = List.of(
                Address.builder().id(1).street("Av. Corrientes 1234").city("Buenos Aires").build(),
                Address.builder().id(2).street("Av. Santa Fe 5678").city("Buenos Aires").build());
        when(addressRepository.findAll()).thenReturn(addresses);

        // Act
        var result = addressService.getAddresses();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getAddressById_deberiaRetornarDireccion_cuandoIdExiste() {
        // Arrange
        var address = Address.builder().id(1).street("Av. Corrientes 1234").city("Buenos Aires").build();
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        // Act
        var result = addressService.getAddressById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStreet()).isEqualTo("Av. Corrientes 1234");
    }

    @Test
    void getAddressById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(addressRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = addressService.getAddressById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAddressesByUser_deberiaRetornarDireccionesFiltradas() {
        // Arrange
        var user = User.builder().id(5).build();
        var addresses = List.of(
                Address.builder().id(1).user(user).street("Av. Corrientes 1234").build());
        when(addressRepository.findByUserId(5)).thenReturn(addresses);

        // Act
        var result = addressService.getAddressesByUser(5);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void createAddress_deberiaGuardarYRetornarDireccion_cuandoDatosValidos() {
        // Arrange
        var user = User.builder().id(1).build();
        var request = new AddressRequest();
        request.setUserId(1);
        request.setStreet("Av. Corrientes 1234");
        request.setCity("Buenos Aires");
        request.setState("CABA");
        request.setZipCode("1043");

        when(addressRepository.existsByUserIdAndStreetAndCityAndZipCode(1, "Av. Corrientes 1234", "Buenos Aires", "1043"))
                .thenReturn(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = addressService.createAddress(request);

        // Assert
        assertThat(result.getStreet()).isEqualTo("Av. Corrientes 1234");
        verify(addressRepository).save(any());
    }

    @Test
    void createAddress_deberiaLanzarDuplicateException_cuandoDireccionYaExiste() {
        // Arrange
        var request = new AddressRequest();
        request.setUserId(1);
        request.setStreet("Av. Corrientes 1234");
        request.setCity("Buenos Aires");
        request.setZipCode("1043");

        when(addressRepository.existsByUserIdAndStreetAndCityAndZipCode(1, "Av. Corrientes 1234", "Buenos Aires", "1043"))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> addressService.createAddress(request))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    void createAddress_deberiaLanzarNotFoundException_cuandoUserNoExiste() {
        // Arrange
        var request = new AddressRequest();
        request.setUserId(99);
        request.setStreet("Av. Corrientes 1234");
        request.setCity("Buenos Aires");
        request.setZipCode("1043");

        when(addressRepository.existsByUserIdAndStreetAndCityAndZipCode(99, "Av. Corrientes 1234", "Buenos Aires", "1043"))
                .thenReturn(false);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.createAddress(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateAddress_deberiaActualizarYRetornarDireccion_cuandoIdExiste() {
        // Arrange
        var address = Address.builder().id(1).street("Vieja Calle").city("CABA").build();
        var request = new AddressRequest();
        request.setStreet("Nueva Calle 999");
        request.setCity("La Plata");
        request.setState("Buenos Aires");
        request.setZipCode("1900");
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = addressService.updateAddress(1, request);

        // Assert
        assertThat(result.getStreet()).isEqualTo("Nueva Calle 999");
        assertThat(result.getCity()).isEqualTo("La Plata");
    }

    @Test
    void updateAddress_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(addressRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.updateAddress(99, new AddressRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteAddress_deberiaEliminar() {
        // Act
        addressService.deleteAddress(1);

        // Assert
        verify(addressRepository).deleteById(1);
    }
}
