package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.entity.dto.WarehouseRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.WarehouseRepository;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    public ArrayList<Warehouse> getWarehouses() {
        return new ArrayList<>(warehouseRepository.findAll());
    }

    public Optional<Warehouse> getWarehouseById(int warehouseId) {
        return warehouseRepository.findById(warehouseId);
    }

    public Warehouse createWarehouse(WarehouseRequest warehouseRequest) {
        Warehouse warehouse = Warehouse.builder()
                .name(warehouseRequest.getName())
                .location(warehouseRequest.getLocation())
                .contactPhone(warehouseRequest.getContactPhone())
                .build();
        return warehouseRepository.save(warehouse);
    }

    public Warehouse updateWarehouse(int warehouseId, WarehouseRequest warehouseRequest) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse", warehouseId));
        warehouse.setName(warehouseRequest.getName());
        warehouse.setLocation(warehouseRequest.getLocation());
        warehouse.setContactPhone(warehouseRequest.getContactPhone());
        return warehouseRepository.save(warehouse);
    }

    public void deleteWarehouse(int warehouseId) {
        warehouseRepository.deleteById(warehouseId);
    }
}
