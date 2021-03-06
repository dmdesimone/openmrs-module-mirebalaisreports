/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */


package org.openmrs.module.mirebalaisreports.definitions;

import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.openmrs.util.OpenmrsUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base implementation of ReportManager that provides some common method implementations
 */
public abstract class BaseReportManager implements ReportManager {

	/**
	 * @return the message code prefix used for all translations for the report
	 */
	protected abstract String getMessageCodePrefix();

	@Override
	public String getName() {
		return translate("name");
	}

	@Override
	public String getDescription() {
		return translate("description");
	}

	@Override
	public List<Parameter> getParameters() {
		return new ArrayList<Parameter>();
	}

	@Override
	public List<RenderingMode> getRenderingModes() {
		return new ArrayList<RenderingMode>();
	}

	@Override
	public String getRequiredPrivilege() {
		return null;
	}

	@Override
	public EvaluationContext initializeContext(Map<String, Object> parameters) {
		EvaluationContext context = new EvaluationContext();
		context.setParameterValues(parameters == null ? new HashMap<String, Object>() : parameters);
		return context;
	}

	protected String translate(String code) {
		String messageCode = getMessageCodePrefix()+code;
		String translation = MessageUtil.translate(messageCode);
		if (messageCode.equals(translation)) {
			return code;
		}
		return translation;
	}

    protected ReportDesign xlsReportDesign(ReportDefinition reportDefinition, byte[] excelTemplate, Properties designProperties) {
        ReportDesign design = new ReportDesign();
        design.setName("mirebalaisreports.output.excel");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(XlsReportRenderer.class);
        if (excelTemplate != null) {
            ReportDesignResource resource = new ReportDesignResource();
            resource.setName("template");
            resource.setExtension("xls");
            resource.setContentType("application/vnd.ms-excel");
            resource.setContents(excelTemplate);
            resource.setReportDesign(design);
            design.addResource(resource);
            if (designProperties != null) {
                design.setProperties(designProperties);
            }
        }
        return design;
    }

    protected ReportDesign csvReportDesign(ReportDefinition reportDefinition) {
        ReportDesign design = new ReportDesign();
        design.setName("mirebalaisreports.output.csv");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(CsvReportRenderer.class);
        design.addPropertyValue("blacklistRegex", "[^\\p{InBasicLatin}\\p{L}]");
        design.addPropertyValue("characterEncoding", "ISO-8859-1");
        return design;
    }

    public <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

    protected byte[] getBytesForResource(String pathToResource) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathToResource);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        OpenmrsUtil.copyFile(inputStream, bytes);
        return bytes.toByteArray();
    }
}
