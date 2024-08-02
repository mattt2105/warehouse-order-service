package com.order_service.order_service.service;

import com.order_service.order_service.dto.InventoryDTO;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${inventory.rest.path}")
    private String inventory_url;


    public Order addOrder(Long productId, int quantity){
        InventoryDTO data = getProduct(productId);
        return createOrder(data,quantity);
    }

    private InventoryDTO getProduct(Long id){
        String url = inventory_url+"/getProduct/"+id;
        ResponseEntity<InventoryDTO> reponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                InventoryDTO.class,
                id);
        return reponse.getBody() == null ? new InventoryDTO() : reponse.getBody();
    }

    private Order createOrder(InventoryDTO data, int quantity){
        if (data == null ||data.getId() == null) {
            throw new IllegalArgumentException("Product doenst exist");
        }
        if (quantity > data.getQuantity()){
            throw new IllegalArgumentException("Product not available");
        }
        Order newOrder = new Order();
        newOrder.setProductId(data.getId());
        newOrder.setProductName(data.getName());
        newOrder.setQuantity(quantity);
        newOrder.setPrice(data.getPrice());
        newOrder.setTotalPrice(data.getPrice().multiply(BigDecimal.valueOf(quantity)));

        updateQtyInventory(newOrder.getProductId(), newOrder.getQuantity());
        return orderRepository.save(newOrder);
    }

    private void updateQtyInventory(Long id, int quantity){
        String url = inventory_url+"update/qty/purchase/"+id;
        ResponseEntity<InventoryDTO> reponse = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                null,
                InventoryDTO.class,
                id,quantity);
    }
}
