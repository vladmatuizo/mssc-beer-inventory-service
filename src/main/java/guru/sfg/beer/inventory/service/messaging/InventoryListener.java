package guru.sfg.beer.inventory.service.messaging;

import com.example.common.model.event.BeerDto;
import com.example.common.model.event.NewInventoryEvent;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static guru.sfg.beer.inventory.service.config.MessagingConfig.NEW_INVENTORY_QUEUE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryListener {

    private final BeerInventoryRepository beerInventoryRepository;

    @JmsListener(destination = NEW_INVENTORY_QUEUE_NAME)
    public void listenInventoryEvent(NewInventoryEvent inventoryEvent) {
        log.debug("Received new inventory event {}", inventoryEvent.toString());

        BeerDto beerDto = inventoryEvent.getBeerDto();
        BeerInventory inventory = BeerInventory.builder()
                .beerId(beerDto.getId())
                .upc(beerDto.getUpc())
                .quantityOnHand(beerDto.getQuantityOnHand())
                .build();
        beerInventoryRepository.save(inventory);
    }
}
