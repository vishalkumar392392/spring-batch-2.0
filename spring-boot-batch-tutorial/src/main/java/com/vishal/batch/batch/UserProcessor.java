package com.vishal.batch.batch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.vishal.batch.entity.User;

@Component
public class UserProcessor implements ItemProcessor<User, User> {

	private static final Map<String, String> DEPT_NAMES = new HashMap<>();
	static {
		DEPT_NAMES.put("001", "Technology");
		DEPT_NAMES.put("002", "Operations");
		DEPT_NAMES.put("003", "Accounts");
	}

	@Override
	public User process(User item) throws Exception {
		item.setDept(DEPT_NAMES.get(item.getDept()));
		System.out.println("Processing item: "+item.getId());
		return item;
	}

}
