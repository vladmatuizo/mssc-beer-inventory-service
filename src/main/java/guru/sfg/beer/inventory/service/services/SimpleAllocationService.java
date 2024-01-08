package guru.sfg.beer.inventory.service.services;

import com.example.common.model.BeerOrderDto;
import com.example.common.model.BeerOrderLineDto;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleAllocationService implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public boolean allocateOrder(BeerOrderDto beerOrder) {
        log.debug("Starting allocation of order {}", beerOrder.getId().toString());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            int orderQuantity = beerOrderLine.getOrderQuantity();
            int orderAllocatedQuantity = beerOrderLine.getQuantityAllocated();
            if (orderQuantity - orderAllocatedQuantity > 0) {
                allocateBeerOrderLine(beerOrderLine);
            }
            totalOrdered.set(totalOrdered.get() + orderQuantity);
            totalAllocated.set(totalAllocated.get() + orderQuantity);
        });

        int totalOrderedValue = totalOrdered.get();
        int totalAllocatedValue = totalAllocated.get();
        log.debug("Total ordered: {}, total allocated: {}", totalOrderedValue, totalAllocatedValue);

        return totalOrderedValue == totalAllocatedValue;
    }

    @Override
    public void deallocateOrder(BeerOrderDto beerOrder) {
        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            BeerInventory savedInventory = beerInventoryRepository.save(
                    BeerInventory.builder()
                            .beerId(beerOrderLine.getBeerId())
                            .upc(beerOrderLine.getUpc())
                            .quantityOnHand(beerOrderLine.getQuantityAllocated())
                            .build());
            log.debug("Completed inventory deallocation (id: {}) for beer upc {}, ",
                    savedInventory.getId(), savedInventory.getUpc());
        });
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLine) {
        List<BeerInventory> beerInventories = beerInventoryRepository.findAllByUpc(beerOrderLine.getUpc());

        beerInventories.forEach(beerInventory -> {
            int inventory = beerInventory.getQuantityOnHand();
            int orderQuantity = beerOrderLine.getOrderQuantity();
            int allocatedQuantity = beerOrderLine.getQuantityAllocated();
            int quantityToAllocate = orderQuantity - allocatedQuantity;

            if (inventory >= quantityToAllocate) {
                //full allocation
                inventory -= quantityToAllocate;
                beerOrderLine.setQuantityAllocated(orderQuantity);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if (inventory > 0) {
                //partial allocation
                beerOrderLine.setQuantityAllocated(allocatedQuantity + inventory);
                beerInventory.setQuantityOnHand(0);

                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
