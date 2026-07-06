package com.bob.pay.application;

import com.bob.pay.domain.model.technician.Technician;
import com.bob.pay.domain.model.order.GeoPoint;
import com.bob.pay.domain.model.technician.TechnicianRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianApplicationService {

    private final TechnicianRepository technicianRepository;

    public TechnicianApplicationService(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    public List<Technician> listTechnicians(String serviceId) {
        return technicianRepository.findAvailableByService(serviceId);
    }

    public List<Technician> listTechnicians(String serviceId, Double latitude, Double longitude) {
        var customerLocation = latitude == null || longitude == null ? null : new GeoPoint(latitude, longitude, "客户当前位置");
        return technicianRepository.findAvailableByService(serviceId).stream()
                .map(technician -> customerLocation == null ? technician : technician.withDistance(distanceKm(customerLocation, technician.location())))
                .toList();
    }

    public Technician updateLocation(String technicianId, GeoPoint location) {
        var technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("技师不存在"));
        return technicianRepository.save(technician.withLocation(location));
    }

    public static double distanceKm(GeoPoint from, GeoPoint to) {
        if (from == null || to == null) return 0;
        double earthRadius = 6371.0;
        double latDistance = Math.toRadians(to.latitude() - from.latitude());
        double lonDistance = Math.toRadians(to.longitude() - from.longitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.latitude())) * Math.cos(Math.toRadians(to.latitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadius * c * 100.0) / 100.0;
    }
}
