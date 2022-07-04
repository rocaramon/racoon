package com.nyxus.racoon.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nyxus.racoon.entity.Test;

public interface ITestDao extends JpaRepository<Test, Long> {

}
