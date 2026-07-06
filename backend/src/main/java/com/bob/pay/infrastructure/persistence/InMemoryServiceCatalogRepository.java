package com.bob.pay.infrastructure.persistence;

import com.bob.pay.domain.model.service.ServiceCatalogRepository;
import com.bob.pay.domain.model.service.ServiceItem;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryServiceCatalogRepository implements ServiceCatalogRepository {

    private final List<ServiceItem> services = new ArrayList<>(List.of(
            new ServiceItem("tuina", "中式推拿", "缓解颈椎，腰肌劳损", 60, new BigDecimal("198.00"), new BigDecimal("298.00"), 28105, "https://images.unsplash.com/photo-1519823551278-64ac92734fb1?auto=format&fit=crop&w=520&q=80", defaultDetail()),
            new ServiceItem("tongluo", "舒享通络", "祛湿通络，激活能量", 80, new BigDecimal("298.00"), new BigDecimal("368.00"), 8716, "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=520&q=80", defaultDetail()),
            new ServiceItem("thai", "泰式SPA", "缺乏运动，腰腿酸软", 100, new BigDecimal("368.00"), new BigDecimal("438.00"), 5867, "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?auto=format&fit=crop&w=520&q=80", defaultDetail()),
            new ServiceItem("french", "法式SPA", "释放压力，舒缓疲劳", 120, new BigDecimal("468.00"), new BigDecimal("538.00"), 4480, "https://images.unsplash.com/photo-1600334129128-685c5582fd35?auto=format&fit=crop&w=520&q=80", defaultDetail()),
            new ServiceItem("foot", "足疗套餐(赠采耳)", "通络活络 缓解疲劳", 80, new BigDecimal("298.00"), new BigDecimal("398.00"), 1178, "https://images.unsplash.com/photo-1611073615830-42c8532f318c?auto=format&fit=crop&w=520&q=80", defaultDetail())
    ));

    @Override
    public List<ServiceItem> findAll() {
        return services;
    }

    @Override
    public Optional<ServiceItem> findById(String id) {
        return services.stream().filter(service -> service.id().equals(id)).findFirst();
    }

    @Override
    public ServiceItem save(ServiceItem serviceItem) {
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).id().equals(serviceItem.id())) {
                services.set(i, serviceItem);
                return serviceItem;
            }
        }
        services.add(serviceItem);
        return serviceItem;
    }

    private static ServiceItem.ServiceDetail defaultDetail() {
        return new ServiceItem.ServiceDetail(
                List.of("缓解肌肉紧张", "缓解身体疲劳", "舒筋通络", "改善亚健康"),
                List.of("放松头部、肩、颈部20分钟", "放松手臂10分钟", "放松腿部10分钟"),
                List.of("按摩布", "酒精消毒"),
                List.of("由结核菌、化脓菌引起的运动器官病症不宜进行推拿", "癌症一般不做推拿", "皮肤病病变损害处、皮开肉绽及烫伤处一般不宜推拿"),
                List.of("开放式软组织损害禁按", "急性传染病、肝炎、肺结核、皮肤病变等禁按", "生理期、孕期谨慎使用", "年老体衰、病久血虚、劳累过度、过饥过饱、醉酒及疾病谨慎使用", "精神紧张焦虑不适合推拿"),
                List.of("性价比高足不出户就可以享受规范上门服务", "可以自由选择合适技师，服务流程透明安心")
        );
    }
}
