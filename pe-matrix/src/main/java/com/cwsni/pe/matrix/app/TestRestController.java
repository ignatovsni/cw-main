package com.cwsni.pe.matrix.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/test")
public class TestRestController {
	
	public class TestData {
		private String key;
		private String value;
		public TestData(String key, String value) {
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}

	@GetMapping(value="/{testId}")
	public TestData getTestData(@PathVariable String testId) {
		return new TestData("1+1", testId);
	}
	
}
