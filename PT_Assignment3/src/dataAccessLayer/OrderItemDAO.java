package dataAccessLayer;

import dataAccessLayer.AbstractDAO;
import model.OrderItem;

public class OrderItemDAO extends AbstractDAO<OrderItem> {
    public OrderItemDAO() {
        super();
    }

    protected String getIdFieldName() {
        return "orderId, productId";
    }
}