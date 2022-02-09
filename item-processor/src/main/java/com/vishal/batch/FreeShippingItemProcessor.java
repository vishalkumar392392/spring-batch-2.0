package com.vishal.batch;

import org.springframework.batch.item.ItemProcessor;

public class FreeShippingItemProcessor implements ItemProcessor<TrackedOrder, TrackedOrder> {

	@Override
	public TrackedOrder process(TrackedOrder item) throws Exception {
		TrackedOrder trackedOrder = new TrackedOrder(item);
		if(trackedOrder.getCost().intValue()>80) {
			trackedOrder.setFreeShipping(true);
			return trackedOrder;
		}else {
			return null;
		}
		
	}



}
