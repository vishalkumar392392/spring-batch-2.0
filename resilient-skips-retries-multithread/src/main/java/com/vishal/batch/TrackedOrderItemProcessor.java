package com.vishal.batch;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;

public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {

	@Override
	public TrackedOrder process(Order item) throws Exception {
		
		System.out.println("Processing order with id: "+item.getOrderId());
		System.out.println("Processing with thread: "+Thread.currentThread().getName());
		TrackedOrder trackedOrder = new TrackedOrder(item);
		trackedOrder.setTrackingNumber(getcode());
		

		return trackedOrder;
	}

	private String getcode() throws OrderProcessingException {
		if(Math.random() <.3) {
			throw new OrderProcessingException();
		}
		return UUID.randomUUID().toString();
	}

}
