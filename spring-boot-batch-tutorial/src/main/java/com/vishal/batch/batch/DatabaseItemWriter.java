package com.vishal.batch.batch;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vishal.batch.entity.User;
import com.vishal.batch.repository.UserRepository;

@Component
public class DatabaseItemWriter implements ItemWriter<User> {
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public void write(List<? extends User> items) throws Exception {
		
		System.out.println("Data saved for users: "+items);
		userRepository.saveAll(items);
	}

}
