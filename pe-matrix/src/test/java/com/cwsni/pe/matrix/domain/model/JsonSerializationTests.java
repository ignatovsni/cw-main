package com.cwsni.pe.matrix.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import com.cwsni.pe.matrix.domain.model.base.BaseId;
import com.cwsni.pe.matrix.domain.model.base.BaseObject;

@RunWith(SpringRunner.class)
@JsonTest
public class JsonSerializationTests {

	@Autowired
	private JacksonTester<BaseId> jsonBaseId;

	@Autowired
	private JacksonTester<BaseObject> jsonBaseObject;

	@Test
	public void testBaseIdSerialization() throws Exception {
		// base
		assertThat(this.jsonBaseId.write(new BaseId("army", 1))).isEqualToJson("\"army:1\"");
		// upper case type
		assertThat(this.jsonBaseId.write(new BaseId("aRMy", 10))).isEqualToJson("\"army:10\"");
		// negative id
		assertThat(this.jsonBaseId.write(new BaseId("new", -1234254))).isEqualToJson("\"new:-1234254\"");
	}

	@Test
	public void testBaseIdDeSerialization() throws Exception {
		assertThat(this.jsonBaseId.parseObject("\"army:11\"")).isEqualTo(new BaseId("army", 11));
	}

	@Test
	public void testBaseObjectSerialization() throws Exception {
		BaseObject bo = new BaseObject();
		bo.setId(new BaseId("army", 1));
		assertThat(this.jsonBaseObject.write(bo)).isEqualToJson("{\"id\":\"army:1\"}");
	}

	@Test
	public void testBaseObjectDeSerialization() throws Exception {
		// base deserialization
		BaseObject parsedObject = this.jsonBaseObject.parseObject("{\"id\":\"country:25\"}");
		assertThat(parsedObject.getId()).isEqualTo(new BaseId("country", 25));
	}

}
