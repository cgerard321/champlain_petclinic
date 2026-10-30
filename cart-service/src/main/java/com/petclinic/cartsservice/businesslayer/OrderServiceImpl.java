package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportReason;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportRepository;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.presentationlayer.BillingRequestModel;
import com.petclinic.cartsservice.presentationlayer.CreateOrderReportRequestModel;
import com.petclinic.cartsservice.presentationlayer.OrderReportResponseModel;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import com.petclinic.cartsservice.presentationlayer.UpdateOrderReportStatusRequestModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;




@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderReportStatus> OPEN_STATUSES =
            EnumSet.of(
                    OrderReportStatus.NEW,
                    OrderReportStatus.IN_REVIEW
            );

    private static final Set<OrderReportReason> ITEM_REASONS =
            EnumSet.of(
                    OrderReportReason.DAMAGED_ITEM,
                    OrderReportReason.MISSING_ITEM,
                    OrderReportReason.WRONG_ITEM
            );

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final OrderReportRepository orderReportRepository;


    public OrderServiceImpl(OrderRepository orderRepository, CustomerClient customerClient,
                            OrderReportRepository orderReportRepository) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
        this.orderReportRepository = orderReportRepository;
    }


    @Override
    public Mono<OrderResponseModel> createOrder(Cart cart, String stripeSessionId) {
          return customerClient.getCustomerById(cart.getCustomerId())
                          .switchIfEmpty(Mono.error(
                                  new NotFoundException("Customer not found")))
                          .map(customer ->
                               EntityModelUtil.toOrder(cart, customer, stripeSessionId))
                          .flatMap(orderRepository::save)
                          .map(EntityModelUtil::toOrderResponseModel);
    }
    @Override
    public Mono<OrderResponseModel> createOrder(Cart cart, BillingRequestModel billing) {
        return customerClient.getCustomerById(cart.getCustomerId())
                        .switchIfEmpty(Mono.error(
                                new NotFoundException("Customer not found")))
                        .map(customer ->
                    EntityModelUtil.toOrder(cart, customer, billing))
                        .flatMap(orderRepository::save)
                        .map(EntityModelUtil::toOrderResponseModel);
    }


    @Override
    public Mono<OrderResponseModel> getOrderByOrderId(String orderId) {
        return orderRepository.findOrderByOrderId(orderId)
                .map(EntityModelUtil::toOrderResponseModel)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
    }

    @Override
    public Flux<OrderResponseModel> getAllOrders() {
        return orderRepository.findAll()
                .map(EntityModelUtil::toOrderResponseModel);
        }


    @Override
    public Flux<OrderResponseModel> findOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Flux.error(new InvalidInputException("customerId must not be null or empty"));
        }
        return orderRepository.findAllByCustomerId(customerId)
                .sort((order1, order2) ->
                        order2.getOrderDate().compareTo(order1.getOrderDate())
                )
                .map(EntityModelUtil::toOrderResponseModel);
    }


    @Override
    public Mono<OrderReportResponseModel> createOrderReport(
            String customerId,
            String orderId,
            CreateOrderReportRequestModel request
    ) {
        return Mono.defer(() -> {
            String validCustomerId = requireIdentifier(
                    customerId,
                    "customerId"
            );
            String validOrderId = requireIdentifier(orderId, "orderId");
            List<String> productIds = validateRequest(request);

            return orderRepository.findByOrderIdAndCustomerId(
                            validOrderId,
                            validCustomerId
                    )
                    .switchIfEmpty(Mono.error(new NotFoundException(
                            "Order not found for this customer"
                    )))
                    .flatMap(order -> {
                        List<OrderReport.AffectedItem> affectedItems =
                                resolveAffectedItems(order, productIds);

                        return orderReportRepository
                                .existsByOrderIdAndStatusIn(
                                        validOrderId,
                                        OPEN_STATUSES
                                )
                                .defaultIfEmpty(false)
                                .flatMap(reportExists -> {
                                    if (Boolean.TRUE.equals(reportExists)) {
                                        return Mono.error(
                                                new ResponseStatusException(
                                                        HttpStatus.CONFLICT,
                                                        "An open report already exists for this order"
                                                )
                                        );
                                    }

                                    OrderReport report =
                                            EntityModelUtil.toOrderReport(
                                                    validOrderId,
                                                    validCustomerId,
                                                    order,
                                                    request,
                                                    affectedItems
                                            );

                                    return orderReportRepository.save(report)
                                            .map(EntityModelUtil::toOrderReportResponseModel);
                                });
                    });
        });
    }

    @Override
    public Flux<OrderReportResponseModel> findReportsByCustomerId(
            String customerId
    ) {
        return Flux.defer(() -> {
            String validCustomerId = requireIdentifier(
                    customerId,
                    "customerId"
            );

            return orderReportRepository
                    .findAllByCustomerIdOrderBySubmittedAtDesc(
                            validCustomerId
                    )
                    .map(EntityModelUtil::toOrderReportResponseModel);
        });
    }

    @Override
    public Flux<OrderReportResponseModel> getAllReports() {
        return orderReportRepository.findAllByOrderBySubmittedAtDesc()
                .map(EntityModelUtil::toOrderReportResponseModel);
    }

    @Override
    public Mono<OrderReportResponseModel> updateReportStatus(
            String reportId,
            UpdateOrderReportStatusRequestModel request
    ) {
        return Mono.defer(() -> {
            String validReportId = requireIdentifier(reportId, "reportId");
            String note = validateStatusRequest(request);

            return orderReportRepository.findByReportId(validReportId)
                    .switchIfEmpty(Mono.error(new NotFoundException(
                            "Report not found"
                    )))
                    .flatMap(report -> {
                        if (!OPEN_STATUSES.contains(report.getStatus())) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "This report is already closed"
                            ));
                        }

                        report.setStatus(request.getStatus());
                        if (request.getStatus() == OrderReportStatus.IN_REVIEW) {
                            report.setResolutionNote(null);
                            report.setResolvedAt(null);
                        } else {
                            report.setResolutionNote(note);
                            report.setResolvedAt(LocalDateTime.now());
                        }

                        return orderReportRepository.save(report);
                    })
                    .map(EntityModelUtil::toOrderReportResponseModel);
        });
    }

    // trimmed note or null
    private String validateStatusRequest(
            UpdateOrderReportStatusRequestModel request
    ) {
        if (request == null || request.getStatus() == null) {
            throw new InvalidInputException("A status is required");
        }

        if (request.getStatus() == OrderReportStatus.NEW) {
            throw new InvalidInputException(
                    "A report cannot be moved back to NEW"
            );
        }

        String note = null;
        if (request.getResolutionNote() != null
                && !request.getResolutionNote().trim().isEmpty()) {
            note = request.getResolutionNote().trim();
        }

        if (note != null && note.length() > 500) {
            throw new InvalidInputException(
                    "Note must contain at most 500 characters"
            );
        }

        if (request.getStatus() == OrderReportStatus.REJECTED && note == null) {
            throw new InvalidInputException(
                    "A reason is required to reject a report"
            );
        }

        return note;
    }

    private List<String> validateRequest(
            CreateOrderReportRequestModel request
    ) {
        if (request == null) {
            throw new InvalidInputException(
                    "The report request is required"
            );
        }

        if (request.getReason() == null) {
            throw new InvalidInputException(
                    "A report reason is required"
            );
        }

        String description = request.getDescription();
        if (description == null) {
            throw new InvalidInputException(
                    "A description is required"
            );
        }

        int descriptionLength = description.trim().length();
        if (descriptionLength < 15 || descriptionLength > 500) {
            throw new InvalidInputException(
                    "Description must contain between 15 and 500 characters"
            );
        }

        List<String> productIds = normalizeProductIds(
                request.getProductIds()
        );
        if (ITEM_REASONS.contains(request.getReason())
                && productIds.isEmpty()) {
            throw new InvalidInputException(
                    "Select at least one affected product for this reason"
            );
        }

        return productIds;
    }

    private List<String> normalizeProductIds(List<String> productIds) {
        if (productIds == null) {
            return List.of();
        }

        Set<String> uniqueProductIds = new LinkedHashSet<>();
        for (String productId : productIds) {
            if (productId == null || productId.trim().isEmpty()) {
                throw new InvalidInputException(
                        "Selected products must have valid IDs"
                );
            }

            String normalizedId = productId.trim();
            if (!uniqueProductIds.add(normalizedId)) {
                throw new InvalidInputException(
                        "A product can only be selected once"
                );
            }
        }

        return List.copyOf(uniqueProductIds);
    }

    private List<OrderReport.AffectedItem> resolveAffectedItems(
            Order order,
            List<String> productIds
    ) {
        List<CartProduct> orderProducts = order.getProducts() == null
                ? List.of()
                : order.getProducts();

        Map<String, CartProduct> productsById = orderProducts.stream()
                .filter(product -> product.getProductId() != null)
                .collect(Collectors.toMap(
                        CartProduct::getProductId,
                        Function.identity(),
                        (first, ignored) -> first
                ));

        return productIds.stream()
                .map(productId -> {
                    CartProduct product = productsById.get(productId);
                    if (product == null) {
                        throw new InvalidInputException(
                                "Selected product does not belong to this order: "
                                        + productId
                        );
                    }

                    return OrderReport.AffectedItem.builder()
                            .productId(product.getProductId())
                            .productName(product.getProductName())
                            .build();
                })
                .toList();
    }

    private String requireIdentifier(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(
                    fieldName + " must not be null or empty"
            );
        }

        return value.trim();
    }

}
