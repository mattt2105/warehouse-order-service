package com.order_service.order_service;

import com.order_service.order_service.dto.InventoryDTO;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.repository.OrderRepository;
import com.order_service.order_service.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addOrder_Success() {
        Long productId = 1L;
        int quantity = 2;

        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setId(productId);
        inventoryDTO.setName("Test Product");
        inventoryDTO.setQuantity(5);
        inventoryDTO.setPrice(new BigDecimal("100.00"));

        ResponseEntity<InventoryDTO> responseEntity = ResponseEntity.ok(inventoryDTO);
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(InventoryDTO.class), anyLong()))
                .thenReturn(responseEntity);

        Order expectedOrder = new Order();
        expectedOrder.setProductId(productId);
        expectedOrder.setProductName("Test Product");
        expectedOrder.setQuantity(quantity);
        expectedOrder.setPrice(inventoryDTO.getPrice());
        expectedOrder.setTotalPrice(inventoryDTO.getPrice().multiply(BigDecimal.valueOf(quantity)));

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder); // Mengembalikan order yang diharapkan

        Order order = orderService.addOrder(productId, quantity);

        Assertions.assertNotNull(order);
        Assertions.assertEquals(productId, order.getProductId());
        Assertions.assertEquals("Test Product", order.getProductName());
        Assertions.assertEquals(quantity, order.getQuantity());
        Assertions.assertEquals(expectedOrder.getTotalPrice(), order.getTotalPrice());
        Mockito.verify(orderRepository, Mockito.times(1)).save(any(Order.class));
    }

    @Test
    public void addOrder_ProductNotAvailable() {
        Long productId = 1L;
        int quantity = 10;

        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setId(productId);
        inventoryDTO.setName("Test Product");
        inventoryDTO.setQuantity(5); // Available quantity

        ResponseEntity<InventoryDTO> responseEntity = ResponseEntity.ok(inventoryDTO);
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(InventoryDTO.class), anyLong()))
                .thenReturn(responseEntity);

        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.addOrder(productId, quantity));
    }

    @Test
    public void addOrder_ProductDoesNotExist() {
        Long productId = 1L;
        int quantity = 2;

        InventoryDTO inventoryDTO = new InventoryDTO();

        ResponseEntity<InventoryDTO> responseEntity = ResponseEntity.ok(inventoryDTO);
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(InventoryDTO.class), anyLong()))
                .thenReturn(responseEntity);

        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.addOrder(productId, quantity));
    }
}
