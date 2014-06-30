package org.lunifera.tools.maven.plugin.subsystems;

/*
 * #%L
 * Lunifera Maven : Subsystem Plugin
 * %%
 * Copyright (C) 2012 - 2014 C4biz Softwares ME, Loetz KG
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.aries.subsystem.core.archive.Attribute;
import org.apache.aries.subsystem.core.archive.AttributeFactory;
import org.apache.aries.subsystem.core.archive.Directive;
import org.apache.aries.subsystem.core.archive.DirectiveFactory;
import org.apache.aries.subsystem.core.archive.Grammar;
import org.apache.aries.subsystem.core.archive.Parameter;
import org.apache.aries.subsystem.core.archive.ParameterFactory;
import org.apache.aries.subsystem.core.archive.ResolutionDirective;
import org.apache.aries.subsystem.core.archive.StartOrderDirective;
import org.apache.aries.subsystem.core.archive.SubsystemContentHeader;
import org.apache.aries.subsystem.core.archive.TypeAttribute;
import org.apache.aries.subsystem.core.archive.VersionAttribute;
import org.apache.aries.subsystem.core.archive.VersionRangeAttribute;
import org.osgi.framework.VersionRange;

/**
 * 
 * @author cvgaviao
 *
 */
public class MutableClause extends SubsystemContentHeader.Clause {

    private static final Pattern PATTERN_PARAMETER = Pattern.compile('('
            + Grammar.PARAMETER + ")(?=;|\\z)");

    /**
     * 
     * @param parameters
     */
    private static void fillInDefaults(Map<String, Parameter> parameters) {
        Parameter parameter = parameters.get(ATTRIBUTE_TYPE);
        if (parameter == null)
            parameters.put(ATTRIBUTE_TYPE, TypeAttribute.DEFAULT);
        parameter = parameters.get(ATTRIBUTE_VERSION);
        if (parameter == null)
            parameters.put(ATTRIBUTE_VERSION, VersionRangeAttribute.DEFAULT);
        parameter = parameters.get(DIRECTIVE_RESOLUTION);
        if (parameter == null)
            parameters.put(DIRECTIVE_RESOLUTION, ResolutionDirective.MANDATORY);
        parameter = parameters.get(DIRECTIVE_STARTORDER);
        if (parameter == null)
            // This is an implementation specific start-order directive
            // value. The specification states there is no default value.
            parameters.put(DIRECTIVE_STARTORDER, new StartOrderDirective("0"));
    }

    private final Map<String, Parameter> modparameters = new HashMap<String, Parameter>();

    /**
     * This constructor was create based on the immutable class
     * {@link org.apache.aries.subsystem.core.archive.SubsystemContentHeader.Clause}
     * .
     * 
     * @param clauseStr
     */
    public MutableClause(String clauseStr) {
        super(clauseStr);
        Matcher matcher = PATTERN_PARAMETER.matcher(clauseStr);
        while (matcher.find()) {
            Parameter parameter = ParameterFactory.create(matcher.group());
            if (parameter instanceof VersionAttribute)
                parameter = new VersionRangeAttribute(new VersionRange(
                        String.valueOf(parameter.getValue())));
            modparameters.put(parameter.getName(), parameter);
        }
        fillInDefaults(modparameters);
    }

    public static Parameter create(Parameter old,
            String value) {
        boolean isDirective = false;
        if (old instanceof Directive)
            isDirective = true;
        if (!isDirective) {
            return AttributeFactory.createAttribute(old.getName(), value);
        } else
            return DirectiveFactory.createDirective(old.getName(), value);
    }

    @Override
    public Attribute getAttribute(String name) {
        Parameter result = modparameters.get(name);
        if (result instanceof Attribute)
            return (Attribute) result;
        return null;
    }

    @Override
    public Collection<Attribute> getAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(
                modparameters.size());
        for (Parameter parameter : modparameters.values())
            if (parameter instanceof Attribute)
                attributes.add((Attribute) parameter);
        attributes.trimToSize();
        return attributes;
    }

    @Override
    public Directive getDirective(String name) {
        Parameter result = modparameters.get(name);
        if (result instanceof Directive)
            return (Directive) result;
        return null;
    }

    @Override
    public Collection<Directive> getDirectives() {
        ArrayList<Directive> directives = new ArrayList<Directive>(
                modparameters.size());
        for (Parameter parameter : modparameters.values())
            if (parameter instanceof Directive)
                directives.add((Directive) parameter);
        directives.trimToSize();
        return directives;
    }

    @Override
    public Parameter getParameter(String name) {
        return modparameters.get(name);
    }

    @Override
    public Collection<Parameter> getParameters() {
        return modparameters.values();
    }

    public void setAttribute(String name, String value) {
        Attribute attribute = getAttribute(name);
        if (attribute == null) {
            attribute = AttributeFactory.createAttribute(name, value);
            modparameters.put(attribute.getName(), attribute);
        }
    }

    public void setDirective(String name, String value) {
        Directive directive = getDirective(name);
        if (directive == null) {
            directive = DirectiveFactory.createDirective(name, value);
            modparameters.put(directive.getName(), directive);
        }
    }
}
