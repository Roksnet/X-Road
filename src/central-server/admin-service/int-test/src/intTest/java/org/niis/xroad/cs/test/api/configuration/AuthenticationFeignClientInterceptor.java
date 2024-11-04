/*
 * The MIT License
 *
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
package org.niis.xroad.cs.test.api.configuration;

import com.nortal.test.core.services.ScenarioContext;
import com.nortal.test.feign.interceptor.FeignClientInterceptor;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.niis.xroad.cs.test.glue.CommonStepDefs;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static org.niis.xroad.cs.test.glue.BaseStepDefs.StepDataKey.TOKEN_TYPE;

@Component
@RequiredArgsConstructor
public class AuthenticationFeignClientInterceptor implements FeignClientInterceptor {
    private static final int EXECUTION_ORDER = 50;

    private final ObjectProvider<ScenarioContext> scenarioContextProvider;

    @Override
    public int getOrder() {
        return EXECUTION_ORDER;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        if (StringUtils.isNotBlank(chain.request().header(HttpHeaders.AUTHORIZATION))) {
            return chain.proceed(chain.request());
        }
        if (!isTokenConfiguredForScenario()) {
            return chain.proceed(chain.request());
        }

        var request = chain.request().newBuilder()
                .addHeader(HttpHeaders.AUTHORIZATION, getToken());
        return chain.proceed(request.build());
    }

    private boolean isTokenConfiguredForScenario() {
        return Optional.ofNullable(scenarioContextProvider.getIfAvailable())
                .map(scenarioContext -> scenarioContext.getStepData(TOKEN_TYPE.name()))
                .isPresent();
    }

    private String getToken() {
        return Optional.ofNullable(scenarioContextProvider.getIfAvailable())
                .map(scenarioContext -> scenarioContext.getStepData(TOKEN_TYPE.name()))
                .map(object -> (CommonStepDefs.TokenType) object)
                .map(CommonStepDefs.TokenType::getHeaderToken)
                .orElseThrow(() -> new IllegalArgumentException("Authentication token was not found."));
    }
}
