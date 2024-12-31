/*
 * The MIT License
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.securityserver.restapi.service;

import ee.ria.xroad.common.util.JsonUtils;
import ee.ria.xroad.signer.protocol.dto.KeyInfo;
import ee.ria.xroad.signer.protocol.dto.TokenInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.niis.xroad.securityserver.restapi.converter.GlobalConfDiagnosticConverter;
import org.niis.xroad.securityserver.restapi.converter.OcspResponderDiagnosticConverter;
import org.niis.xroad.securityserver.restapi.converter.TimestampingServiceDiagnosticConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.function.Predicate.not;

@Slf4j
@Service
@Transactional
@PreAuthorize("isAuthenticated()")
public class SystemInformationService {
    private static final ObjectMapper MAPPER = JsonUtils.getObjectMapperCopy()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<String, Supplier<Object>> collectors = new LinkedHashMap<>();
    private final TokenService tokenService;

    public SystemInformationService(DiagnosticService diagnosticService,
                                    VersionService versionService,
                                    TokenService tokenService,
                                    OSHelper osHelper,
                                    GlobalConfDiagnosticConverter gcDiagnosticConverter,
                                    TimestampingServiceDiagnosticConverter tsDiagnosticConverter,
                                    OcspResponderDiagnosticConverter ocspResponderDiagnosticConverter) {
        this.tokenService = tokenService;

        collectors.put("X-Road and Java Version", versionService::getVersionInfo);
        collectors.put("OS Version", osHelper::getOsDetails);
        collectors.put("Global configuration", combine(diagnosticService::queryGlobalConfStatus, gcDiagnosticConverter::convert));
        collectors.put("Timestamping", combine(diagnosticService::queryTimestampingStatus, tsDiagnosticConverter::convert));
        collectors.put("OCSP responders", combine(diagnosticService::queryOcspResponderStatus, ocspResponderDiagnosticConverter::convert));
        collectors.put("Authentication certificates", this::collectAuthCertificates);
        collectors.put("Installed X-Road packages", osHelper::getXRoadPackagesDetails);
    }

    public byte[] collectSystemInformation() throws JsonProcessingException {
        var data = new LinkedList<SystemInformationService.InfoFragment>();

        collectors.forEach((name, provider) -> data.add(collect(name, provider)));
        return MAPPER.writeValueAsBytes(data);
    }

    private List<Certificate> collectAuthCertificates() {
        return tokenService.getAllTokens().stream()
                .map(TokenInfo::getKeyInfo)
                .flatMap(Collection::stream)
                .filter(not(KeyInfo::isForSigning))
                .map(KeyInfo::getCerts)
                .flatMap(Collection::stream)
                .map(cert -> new Certificate(cert.getCertificateDisplayName(), cert.getStatus(), cert.isActive()))
                .toList();
    }

    private SystemInformationService.InfoFragment collect(String name, Supplier<Object> collector) {
        try {
            return new SystemInformationService.InfoFragment(name, collector.get(), null);
        } catch (Exception e) {
            log.error("Failed to read data for {}", name, e);
            return new SystemInformationService.InfoFragment(name, null, e.getMessage());
        }
    }

    private static <I, O> Supplier<O> combine(Supplier<I> supplier, Function<I, O> mapper) {
        return () -> mapper.apply(supplier.get());
    }

    private record InfoFragment(String name, Object value, String errorMessage) {
    }

    private record Certificate(String name, String status, boolean active) {
    }


}

