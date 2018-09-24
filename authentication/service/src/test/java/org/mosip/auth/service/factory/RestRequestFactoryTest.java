package org.mosip.auth.service.factory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mosip.auth.core.constant.RestServicesConstants;
import org.mosip.auth.core.exception.IDDataValidationException;
import org.mosip.auth.core.util.dto.AuditRequestDto;
import org.mosip.auth.core.util.dto.AuditResponseDto;
import org.mosip.auth.core.util.dto.RestRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(value = { "classpath:audit.properties", "classpath:rest-services.properties" })
public class RestRequestFactoryTest {

	@InjectMocks
	RestRequestFactory restFactory;

	@Autowired
	ConfigurableEnvironment env;

	@Autowired
	MockMvc mockMvc;

	@InjectMocks
	AuditRequestFactory auditFactory;

	@Before
	public void before() {
		ReflectionTestUtils.setField(auditFactory, "env", env);
		ReflectionTestUtils.setField(restFactory, "env", env);
	}

	@Test
	public void testBuildRequest() throws IDDataValidationException {
		AuditRequestDto auditRequest = auditFactory.buildRequest("IDA", "desc");
		auditRequest.setActionTimeStamp(null);

		RestRequestDTO request = restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditRequest,
				AuditResponseDto.class);

		RestRequestDTO testRequest = new RestRequestDTO();
		String serviceName = RestServicesConstants.AUDIT_MANAGER_SERVICE.getServiceName();
		String uri = env.getProperty(serviceName.concat(".rest.uri"));
		String httpMethod = env.getProperty(serviceName.concat(".rest.httpMethod"));
		String mediaType = env.getProperty(serviceName.concat(".rest.headers.mediaType"));
		String timeout = env.getProperty(serviceName.concat(".rest.timeout"));

		testRequest.setUri(uri);
		testRequest.setHttpMethod(HttpMethod.valueOf(httpMethod));
		testRequest.setRequestBody(auditRequest);
		testRequest.setResponseType(AuditResponseDto.class);
		testRequest.setHeaders(headers -> headers.setContentType(MediaType.valueOf(mediaType)));
		testRequest.setTimeout(Integer.parseInt(timeout));

		request.setHeaders(null);
		testRequest.setHeaders(null);

		assertEquals(testRequest, request);

	}

	@Test(expected=IDDataValidationException.class)
	public void testBuildRequestEmptyUri() throws IDDataValidationException {

		MockEnvironment environment = new MockEnvironment();
		environment.merge(env);
		environment.setProperty("audit.rest.uri", "");

		ReflectionTestUtils.setField(restFactory, "env", environment);

		restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditFactory.buildRequest("IDA", "desc"),
				AuditResponseDto.class);
	}

	@Test(expected=IDDataValidationException.class)
	public void testBuildRequestNullProperties() throws IDDataValidationException {

		MockEnvironment environment = new MockEnvironment();

		ReflectionTestUtils.setField(restFactory, "env", environment);

		restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditFactory.buildRequest("IDA", "desc"),
				AuditResponseDto.class);
	}

	@Test(expected=IDDataValidationException.class)
	public void testBuildRequestEmptyHttpMethod() throws IDDataValidationException {

		MockEnvironment environment = new MockEnvironment();
		environment.merge(env);
		environment.setProperty("audit.rest.httpMethod", "");

		ReflectionTestUtils.setField(restFactory, "env", environment);

		restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditFactory.buildRequest("IDA", "desc"),
				AuditResponseDto.class);
	}

	@Test(expected=IDDataValidationException.class)
	public void testBuildRequestEmptyResponseType() throws IDDataValidationException {

		restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditFactory.buildRequest("IDA", "desc"),
				null);
	}

	@Test
	public void testBuildRequestEmptyTimeout() throws IDDataValidationException {

		MockEnvironment environment = new MockEnvironment();
		environment.merge(env);
		environment.setProperty("audit.rest.timeout", "");

		ReflectionTestUtils.setField(restFactory, "env", environment);

		restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditFactory.buildRequest("IDA", "desc"),
				AuditResponseDto.class);

		// TODO Assert response
	}

	@Test
	public void testBuildRequestHeaders() throws IDDataValidationException {

		MockEnvironment environment = new MockEnvironment();
		environment.merge(env);
		environment.setProperty("audit.rest.headers.accept", "application/json");

		ReflectionTestUtils.setField(restFactory, "env", environment);

		restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditFactory.buildRequest("IDA", "desc"),
				AuditResponseDto.class);
	}

}
