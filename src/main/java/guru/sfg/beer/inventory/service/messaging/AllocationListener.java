package guru.sfg.beer.inventory.service.messaging;

import com.example.common.model.BeerOrderDto;
import com.example.common.model.event.AllocateOrderRequest;
import com.example.common.model.event.AllocateOrderResult;
import guru.sfg.beer.inventory.service.services.AllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import static guru.sfg.beer.inventory.service.config.MessagingConfig.ALLOCATE_ORDER_QUEUE_NAME;
import static guru.sfg.beer.inventory.service.config.MessagingConfig.ALLOCATE_ORDER_RESPONSE_QUEUE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationListener {

    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = ALLOCATE_ORDER_QUEUE_NAME)
    public void listen(AllocateOrderRequest allocateOrderRequest) {
        BeerOrderDto beerOrder = allocateOrderRequest.beerOrder();
        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder()
                .beerOrder(beerOrder);

        try {
            boolean orderIsAllocated = allocationService.allocateOrder(beerOrder);
            builder.pendingInventory(!orderIsAllocated);
        } catch (Exception e) {
            log.error("Allocation error for beer order {}, message: {}", beerOrder.getId().toString(), e.getMessage());
            builder.allocationError(true);
        }

        jmsTemplate.convertAndSend(ALLOCATE_ORDER_RESPONSE_QUEUE_NAME, builder.build());
    }
}
