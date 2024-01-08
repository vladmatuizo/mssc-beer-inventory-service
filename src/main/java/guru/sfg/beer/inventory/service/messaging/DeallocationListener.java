package guru.sfg.beer.inventory.service.messaging;

import com.example.common.model.BeerOrderDto;
import com.example.common.model.event.DeallocateOrderRequest;
import guru.sfg.beer.inventory.service.services.AllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static guru.sfg.beer.inventory.service.config.MessagingConfig.DEALLOCATE_ORDER_QUEUE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocationListener {

    private final AllocationService allocationService;

    @JmsListener(destination = DEALLOCATE_ORDER_QUEUE_NAME)
    public void listen(DeallocateOrderRequest deallocateOrderRequest) {
        BeerOrderDto beerOrder = deallocateOrderRequest.beerOrder();

        allocationService.deallocateOrder(beerOrder);
    }
}
