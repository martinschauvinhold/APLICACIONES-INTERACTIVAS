package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.AddressRequest;
import com.uade.tpo.demo.repository.AddressRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public ArrayList<Address> getAddresses() {
        return new ArrayList<>(addressRepository.findAll());
    }

    public Optional<Address> getAddressById(int addressId) {
        return addressRepository.findById(addressId);
    }

    public List<Address> getAddressesByUser(int userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address createAddress(AddressRequest addressRequest) {
        User user = userRepository.findById(addressRequest.getUserId()).get();
        Address address = Address.builder()
                .user(user)
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .zipCode(addressRequest.getZipCode())
                .referenceNote(addressRequest.getReferenceNote())
                .build();
        return addressRepository.save(address);
    }

    public Address updateAddress(int addressId, AddressRequest addressRequest) {
        Address address = addressRepository.findById(addressId).get();
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setZipCode(addressRequest.getZipCode());
        address.setReferenceNote(addressRequest.getReferenceNote());
        return addressRepository.save(address);
    }

    public void deleteAddress(int addressId) {
        addressRepository.deleteById(addressId);
    }
}
