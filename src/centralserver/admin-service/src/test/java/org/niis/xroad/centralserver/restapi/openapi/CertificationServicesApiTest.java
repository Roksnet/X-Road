/**
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.centralserver.restapi.openapi;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.niis.xroad.centralserver.openapi.model.ApprovedCertificationService;
import org.niis.xroad.centralserver.restapi.util.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.OK;

class CertificationServicesApiTest extends AbstractApiRestTemplateTestContext {

    private static final String CERT_PROFILE_INFO_PROVIDER
            = "ee.ria.xroad.common.certificateprofile.impl.BasicCertificateProfileInfoProvider";

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void getCertificationServices() {
        TestUtils.addApiKeyAuthorizationHeader(restTemplate);

        ResponseEntity<ApprovedCertificationService[]> response = restTemplate.getForEntity(
                "/api/v1/certification-services",
                ApprovedCertificationService[].class);

        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
    }

    @Test
    void addCertificationService() {
        TestUtils.addApiKeyAuthorizationHeader(restTemplate);
        var entity = prepareAddCertificationServiceRequest();

        ResponseEntity<ApprovedCertificationService> response = restTemplate.postForEntity(
                "/api/v1/certification-services",
                entity,
                ApprovedCertificationService.class);

        assertNotNull(response);
        assertEquals(OK, response.getStatusCode());
        assertEquals("*.google.com", response.getBody().getCaCertificate().getSubjectCommonName());
        assertNotNull(response.getBody().getCaCertificate().getNotBefore());
        assertNotNull(response.getBody().getCaCertificate().getNotAfter());
    }

    private HttpEntity<MultiValueMap> prepareAddCertificationServiceRequest() {
        MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();
        request.add("certificate", generateMockCertFile());
        request.add("tls_auth", Boolean.FALSE);
        request.add("certificate_profile_info", CERT_PROFILE_INFO_PROVIDER);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(request, headers);
    }

    @SneakyThrows
    private ByteArrayResource generateMockCertFile() {
        return new ByteArrayResource(
                IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("google-cert.der"))) {
            @Override
            public String getFilename() {
                return "google-cert.der";
            }
        };
    }

}
