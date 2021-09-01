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

package ee.ria.xroad.common.certificateprofile.impl;

import ee.ria.xroad.common.certificateprofile.CertificateProfileInfoProvider;
import ee.ria.xroad.common.certificateprofile.DnFieldDescription;
import ee.ria.xroad.common.certificateprofile.DnFieldValue;
import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.identifier.SecurityServerId;

import org.bouncycastle.util.Arrays;
import org.junit.Test;
import org.mockito.Mockito;

import javax.security.auth.x500.X500Principal;

import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NeomCertificateProfileInfoProviderTest {

    /**
     * Tests whether provider returns correct implementation as expected.
     */
    @Test
    public void providerReturnsCorrectImplementations() {
        CertificateProfileInfoProvider provider = provider();
        assertTrue(
                "Must return instance of NeomAuthCertificateProfileInfo",
                provider.getAuthCertProfile(
                        new AuthCertificateProfileInfoParameters(
                                SecurityServerId.create("XX", "foo", "bar", "server"), "foo"
                        )
                ) instanceof NeomAuthCertificateProfileInfo
        );

        assertTrue(
                "Must return instance of NeomSignCertificateProfileInfo",
                provider.getSignCertProfile(
                        new SignCertificateProfileInfoParameters(
                                ClientId.create("XX", "foo", "bar"), "foo"
                        )
                ) instanceof NeomSignCertificateProfileInfo
        );
    }

    /**
     * Tests whether getting expected subject fields succeeds as expected.
     * 
     */
    @Test
    public void signProfileSubjectFields() {
        DnFieldDescription[] expectedFields = {
                /*new EnumLocalizedFieldDescriptionImpl("CN", DnFieldLabelLocalizationKey.ORGANIZATION_NAME_CN, "")
                        .setReadOnly(true),*/

                /*new EnumLocalizedFieldDescriptionImpl("C", DnFieldLabelLocalizationKey.COUNTRY_CODE, "")
                        .setReadOnly(true),*/

                new EnumLocalizedFieldDescriptionImpl("O", DnFieldLabelLocalizationKey.INSTANCE_IDENTIFIER_O, "XX")
                        .setReadOnly(true),

                new EnumLocalizedFieldDescriptionImpl("businessCategory", DnFieldLabelLocalizationKey.MEMBER_CLASS_BIZ,
                        "foo"
                        ).setReadOnly(true),
                
                new EnumLocalizedFieldDescriptionImpl("serialNumber", DnFieldLabelLocalizationKey.MEMBER_CODE_SN,
                        "bar"
                        ).setReadOnly(true),
                
        };

        assertTrue(
                "Did not get expected fields",
                Arrays.areEqual(expectedFields, getSignProfile().getSubjectFields())
        );
    }

    /**
     * Tests whether validating correct subject field succeeds as expected.
     *
     * @throws Exception in case of any unexpected errors
     */
    @Test
    public void signProfileValidateFieldSuccessfully() throws Exception {
        getSignProfile().validateSubjectField(
                new DnFieldValueImpl("C", "XX")
        );
    }

    /**
     * Tests whether validating unknown subject field fails as expected.
     *
     * @throws Exception in case of any unexpected errors
     */
    @Test(expected = Exception.class)
    public void signProfileFailToValidateUnknownField() throws Exception {
        getSignProfile().validateSubjectField(
                new DnFieldValueImpl("X", "foo")
        );
    }

    /**
     * Tests whether validating blank subject field of sign profile fails
     * as expected.
     *
     * @throws Exception in case of any unexpected errors
     */
    @Test(expected = Exception.class)
    public void signProfileFailToValidateBlankField() throws Exception {
        getSignProfile().validateSubjectField(
                new DnFieldValueImpl("O", "")
        );
    }

    /**
     * Tests whether creating subject Dn of sign profile succeeds as expected.
     */
    @Test
    public void signProfileCreateSubjectDn() {
        assertEquals(
                new X500Principal("C=foo, O=bar, CN=baz"),
                getSignProfile().createSubjectDn(
                        new DnFieldValue[] {
                                new DnFieldValueImpl("C", "foo"),
                                new DnFieldValueImpl("O", "bar"),
                                new DnFieldValueImpl("CN", "baz")
                        }
                )
        );
    }

    /**
     * Tests whether getting subject identifier of sign profile succeeds
     * as expected.
     */
    @Test
    public void signProfileGetSubjectIdentifier() {
        X509Certificate mockCert = Mockito.mock(X509Certificate.class);
        Mockito.when(mockCert.getSubjectX500Principal()).thenReturn(
                new X500Principal("C=YY, CN=abc, O=XX, businessCategory=Foo, serialNumber=bar")
        );

        assertEquals(
                ClientId.create("XX", "Foo", "bar"),
                getSignProfile().getSubjectIdentifier(mockCert)
        );
    }

    /**
     * Tests whether getting expected fields of auth profile succeeds
     * as expected.
     */
    @Test
    public void authProfileSubjectFields() {
        DnFieldDescription[] expectedFields = {
                /*new EnumLocalizedFieldDescriptionImpl("C", DnFieldLabelLocalizationKey.COUNTRY_CODE, "")
                        .setReadOnly(true),*/
                new EnumLocalizedFieldDescriptionImpl("O", DnFieldLabelLocalizationKey.INSTANCE_IDENTIFIER_O, "XX")
                        .setReadOnly(true),
                /*new EnumLocalizedFieldDescriptionImpl("serialNumber", DnFieldLabelLocalizationKey.MEMBER_CODE_SN, "")
                        .setReadOnly(true),*/
                new EnumLocalizedFieldDescriptionImpl("CN", DnFieldLabelLocalizationKey.SERVER_CODE,
                        "server"
                        ).setReadOnly(true),
        };

        assertTrue(
                "Did not get expected fields",
                Arrays.areEqual(expectedFields, getAuthProfile().getSubjectFields())
        );
    }

    /**
     * Tests whether validating correct subject field of auth profile succeeds
     * as expected.
     *
     * @throws Exception in case of any unexpected errors
     */
    @Test
    public void authProfileValidateFieldSuccessfully() throws Exception {
        getAuthProfile().validateSubjectField(
                new DnFieldValueImpl("O", "YY")
        );
    }

    /**
     * Tests whether validating unknown subject field of auth profile fails
     * as expected.
     *
     * @throws Exception in case of any unexpected errors
     */
    @Test(expected = Exception.class)
    public void authProfileFailToValidateUnknownField() throws Exception {
        getAuthProfile().validateSubjectField(
                new DnFieldValueImpl("X", "XX")
        );
    }

    /**
     * Tests whether validating blank subject field of auth profile fails
     * as expected.
     *
     * @throws Exception in case of any unexpected errors
     */
    @Test(expected = Exception.class)
    public void authProfileFailToValidateBlankField() throws Exception {
        getAuthProfile().validateSubjectField(
                new DnFieldValueImpl("O", "")
        );
    }

    /**
     * Tests whether creating subject Dn of auth profile succeeds as expected.
     */
    @Test
    public void authProfileCreateSubjectDn() {
        assertEquals(
                new X500Principal("C=XX, O=foo, CN=baz"),
                getAuthProfile().createSubjectDn(
                        new DnFieldValue[] {
                                new DnFieldValueImpl("C", "XX"),
                                new DnFieldValueImpl("O", "foo"),
                                new DnFieldValueImpl("CN", "baz")
                        }
                )
        );
    }

    // ------------------------------------------------------------------------

    private CertificateProfileInfoProvider provider() {
        return new NeomCertificateProfileInfoProvider();
    }

    private NeomSignCertificateProfileInfo getSignProfile() {
        return new NeomSignCertificateProfileInfo(
                new SignCertificateProfileInfoParameters(
                        ClientId.create("XX", "foo", "bar"),
                        "foo"
                )
        );
    }

    private NeomAuthCertificateProfileInfo getAuthProfile() {
        return new NeomAuthCertificateProfileInfo(
                new AuthCertificateProfileInfoParameters(
                        SecurityServerId.create("XX", "foo", "bar", "server"),
                        "foo"
                )
        );
    }
    
}
