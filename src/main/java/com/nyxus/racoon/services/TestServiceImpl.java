package com.nyxus.racoon.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nyxus.racoon.dao.ITestDao;
import com.nyxus.racoon.entity.Test;

@Service
public class TestServiceImpl implements ITestService {
	
	@Autowired
	private ITestDao masterDao;

	@Override
	public List<Test> findAll() {
		return masterDao.findAll();
	}
	

}
