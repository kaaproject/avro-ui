/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
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

package org.kaaproject.avro.ui.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The Class NamesValidator.
 */
public class NamesValidator {

    /** The Constant NAME_SPACE_PATTERN. */
    private static final Pattern NAME_SPACE_PATTERN = Pattern.compile("^[a-zA-Z_\\$][\\w\\$]*(?:\\.[a-zA-Z_\\$][\\w\\$]*)*$");
    
    /** The Constant CLASS_NAME_PATTERN. */
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*");

    /** The Constant RESERVED_KEYWORDS. */
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<String>(Arrays.asList(
            "abstract",     "assert",        "boolean",      "break",           "byte",
            "case",         "catch",         "char",         "class",           "const",
            "continue",     "default",       "do",           "double",          "else",
            "enum",         "extends",       "false",        "final",           "finally",
            "float",        "for",           "goto",         "if",              "implements",
            "import",       "instanceof",    "int",          "interface",       "long",
            "native",       "new",           "null",         "package",         "private",
            "protected",    "public",        "return",       "short",           "static",
            "strictfp",     "super",         "switch",       "synchronized",    "this",
            "throw",        "throws",        "transient",    "true",            "try",
            "void",         "volatile",      "while"
    ));
    
    /**
     * Validate identifier.
     *
     * @param identifier the identifier
     * @param pattern the pattern
     * @return true, if successful
     */
    private static boolean validateIdentifier(String identifier, Pattern pattern) {
        if (!pattern.matcher(identifier).matches()) {
            return false;
        }
        for (String part : identifier.split("\\.")) {
            if (RESERVED_KEYWORDS.contains(part)) {
                return false;
            }
        }
        return identifier.length() > 0;
    }
    
    /**
     * Validate class name.
     *
     * @param name the name
     * @return true, if successful
     */
    public static boolean validateClassName(String name) {
        return validateIdentifier(name, CLASS_NAME_PATTERN);
    }
    
    /**
     * Validate namespace.
     *
     * @param namespace the namespace
     * @return true, if successful
     */
    public static boolean validateNamespace(String namespace) {
        return validateIdentifier(namespace, NAME_SPACE_PATTERN);
    }
    
    /**
     * Validate enum symbol.
     *
     * @param enumSymbol the enum symbol
     * @return true, if successful
     */
    public static boolean validateEnumSymbol(String enumSymbol) {
        return validateIdentifier(enumSymbol, CLASS_NAME_PATTERN);
    }
    
    /**
     * Validate class name or throw exception.
     *
     * @param name the name
     */
    public static void validateClassNameOrThrowException(String name) {
        if (!validateClassName(name)) {
            throw new IllegalArgumentException("Class name is not valid. '" + name + "' is not a valid identifier.");
        }
    }
    
    /**
     * Validate namespace or throw exception.
     *
     * @param namespace the namespace
     */
    public static void validateNamespaceOrThrowException(String namespace) {
        if (!validateNamespace(namespace)) {
            throw new IllegalArgumentException("Namespace is not valid. '" + namespace + "' is not a valid identifier.");
        }
    }

    /**
     * Validate enum symbol or throw exception.
     *
     * @param enumSymbol the enum symbol
     */
    public static void validateEnumSymbolOrThrowException(String enumSymbol) {
        if (!validateEnumSymbol(enumSymbol)) {
            throw new IllegalArgumentException("Enum symbol is not valid. '" + enumSymbol + "' is not a valid identifier.");
        }
    }
    
}
