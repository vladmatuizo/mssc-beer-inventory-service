package guru.sfg.beer.inventory.service.services;

import com.example.common.model.BeerOrderDto;

public interface AllocationService {

    boolean allocateOrder(BeerOrderDto beerOrder);
}
