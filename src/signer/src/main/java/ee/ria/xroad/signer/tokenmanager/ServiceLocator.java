/*
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
package ee.ria.xroad.signer.tokenmanager;

import akka.actor.ActorContext;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import static ee.ria.xroad.signer.protocol.ComponentNames.MODULE_MANAGER;
import static ee.ria.xroad.signer.protocol.ComponentNames.OCSP_RESPONSE_MANAGER;
import static ee.ria.xroad.signer.util.ExceptionHelper.tokenNotFound;

/**
 * Utility class for getting specific actor paths in Signer.
 */
public final class ServiceLocator {

    private ServiceLocator() {
    }

    /**
     * @param context the actor context
     * @return the OCSP response manager actor
     */
    public static ActorSelection getOcspResponseManager(
            ActorContext context) {
        return context.actorSelection("/user/" + OCSP_RESPONSE_MANAGER);
    }

    @Deprecated
    public static ActorSelection getOcspResponseManager(
            ActorSystem actorSystem) {
        return actorSystem.actorSelection("/user/" + OCSP_RESPONSE_MANAGER);
    }

    /**
     * @param context the actor context
     * @param tokenId the token id
     * @return the token actor
     */
    public static ActorSelection getToken(ActorContext context,
                                          String tokenId) {
        String path = String.format("/user/%s/%s/%s", MODULE_MANAGER,
                getModuleId(tokenId), tokenId);
        return context.actorSelection(path);
    }

    @Deprecated
    public static ActorSelection getToken(ActorSystem actorSystem,
                                          String tokenId) {
        String path = String.format("/user/%s/%s/%s", MODULE_MANAGER,
                getModuleId(tokenId), tokenId);
        return actorSystem.actorSelection(path);
    }

    private static String getModuleId(String tokenId) {
        String moduleId = TokenManager.getModuleId(tokenId);
        if (moduleId == null) {
            throw tokenNotFound(tokenId);
        }

        return moduleId;
    }
}
