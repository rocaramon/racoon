package com.nyxus.racoon.test;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nyxus.racoon.entity.Test;
import com.nyxus.racoon.services.ITestService;


public class TestExec {
	@Autowired
	ITestService testService;
	
	
	@PostConstruct
	public void test() {
		for(Test t: testService.findAll()) {
			System.out.println("-----");
			System.out.println(t.getId()+" "+t.getDescription());
		}
	}

}
