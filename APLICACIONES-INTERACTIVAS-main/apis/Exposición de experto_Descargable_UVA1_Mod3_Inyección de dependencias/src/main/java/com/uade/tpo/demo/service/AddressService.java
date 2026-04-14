package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.dto.AddressRequest;

public interface AddressService {
    public ArrayList<Address> getAddresses();

    public Optional<Address> getAddressById(int addressId);

    public List<Address> getAddressesByUser(int userId);

    public Address createAddress(AddressRequest addressRequest);

    public Address updateAddress(int addressId, AddressRequest addressRequest);

    public void deleteAddress(int addressId);
}
