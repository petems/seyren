/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.util.velocity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.util.config.SeyrenConfig;
import com.seyren.core.util.email.EmailHelper;

@Named
public class VelocityEmailHelper implements EmailHelper {

    private final SeyrenConfig seyrenConfig;

    private static final String TEMPLATE_FILE_NAME = seyrenConfig.getEmailTemplate();
    private static final String TEMPLATE_CONTENT = getTemplateAsString();

    @Inject
    public VelocityEmailHelper(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    public String createSubject(Check check) {
        return "Seyren alert: " + check.getName();
    }

    @Override
    public String createBody(Check check, Subscription subscription, List<Alert> alerts) {
        VelocityContext context = createVelocityContext(check, subscription, alerts);
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, "EmailNotificationService", TEMPLATE_CONTENT);
        return stringWriter.toString();
    }

    private VelocityContext createVelocityContext(Check check, Subscription subscription, List<Alert> alerts) {
        VelocityContext result = new VelocityContext();
        result.put("CHECK", check);
        result.put("ALERTS", alerts);
        result.put("SEYREN_URL", seyrenConfig.getBaseUrl());
        return result;
    }

    private static String getTemplateAsString() {
        try {
            return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException("Template file could not be found on classpath at " + TEMPLATE_FILE_NAME);
        }
    }

}
