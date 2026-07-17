package com.saas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.saas.testsupport.AbstractIntegrationTest;

class ApiApplicationTests extends AbstractIntegrationTest {

	@Autowired
	private ApplicationContext applicationContext;

	// The real assertion here is implicit: the Spring context must build without an exception for
	// this test to even reach its body. The explicit assertion below only exists to give the test
	// a checkable outcome, not because a null context is a realistic failure mode.
	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

}
