package com.bob.pay.infrastructure.persistence;

import com.bob.pay.domain.model.order.GeoPoint;
import com.bob.pay.domain.model.technician.Technician;
import com.bob.pay.domain.model.technician.TechnicianRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryTechnicianRepository implements TechnicianRepository {

    private final List<Technician> technicians = List.of(
            new Technician("sha", "沙鑫鑫", "初级", 0.44, 36, 7, 4, "23:00", true, "https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=480&q=80", new GeoPoint(29.3103, 120.0801, "技师当前位置"), List.of("tuina", "tongluo", "foot")),
            new Technician("wang", "王洛菲", "高级", 4.13, 293, 26, 78, "23:00", false, "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=480&q=80", new GeoPoint(29.3350, 120.1050, "技师当前位置"), List.of("tuina", "thai", "french")),
            new Technician("zhou", "周茉", "高级", 5.31, 30, 3, 2, "23:00", false, "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=480&q=80", new GeoPoint(29.3500, 120.1150, "技师当前位置"), List.of("thai", "french")),
            new Technician("li", "李潇潇", "高级", 6.20, 34, 7, 6, "23:00", true, "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=480&q=80", new GeoPoint(29.3600, 120.1250, "技师当前位置"), List.of("tongluo", "foot", "french"))
    );

    @Override
    public List<Technician> findAvailableByService(String serviceId) {
        return technicians.stream()
                .filter(technician -> technician.canServe(serviceId))
                .toList();
    }

    @Override
    public Optional<Technician> findById(String id) {
        return technicians.stream().filter(technician -> technician.id().equals(id)).findFirst();
    }

    @Override
    public Technician save(Technician technician) {
        for (int i = 0; i < technicians.size(); i++) {
            if (technicians.get(i).id().equals(technician.id())) {
                technicians.set(i, technician);
                return technician;
            }
        }
        technicians.add(technician);
        return technician;
    }
}
