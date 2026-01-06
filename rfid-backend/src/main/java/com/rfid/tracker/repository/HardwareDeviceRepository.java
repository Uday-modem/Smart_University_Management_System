package com.rfid.tracker.repository;

import com.rfid.tracker.entity.HardwareDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface HardwareDeviceRepository extends JpaRepository<HardwareDevice, Long> {
    Optional<HardwareDevice> findByDeviceId(String deviceId);
    List<HardwareDevice> findByRoomNumber(String roomNumber);
    List<HardwareDevice> findByIsActive(Boolean isActive);
    List<HardwareDevice> findByBuilding(String building);
    Optional<HardwareDevice> findByRoomNumberAndDeviceType(String roomNumber, HardwareDevice.DeviceType deviceType);
}
