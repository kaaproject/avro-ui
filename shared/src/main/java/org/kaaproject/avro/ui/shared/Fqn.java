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

import java.io.Serializable;

public class Fqn implements Serializable, Comparable<Fqn> {

    private static final long serialVersionUID = -2349658878589191275L;
    
    protected String namespace;
    protected String name;
    
    public Fqn() {
    }
    
    public Fqn(Fqn fqn) {
        this.namespace = fqn.getNamespace();
        this.name = fqn.getName();
    }
    
    public Fqn(String fqnString) {
        int index = fqnString.lastIndexOf('.');
        this.namespace = fqnString.substring(0, index);
        this.name = fqnString.substring(index+1, fqnString.length());
    }

    public Fqn(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getFqnString() {
        if (namespace != null && !namespace.isEmpty() &&
                name != null && !name.isEmpty()) {
            return namespace + "." + name;
        }
        return null;
    }
    
    @Override
    public int compareTo(Fqn o) {
        if (o == null) {
            return 1;
        }
        if (namespace == null) {
            return o.namespace == null ? 0 : -1;
        } else {
            if (o.namespace == null) {
                return 1;
            } else {
                int result = namespace.compareTo(o.namespace);
                if (result == 0) {
                    if (name == null) {
                        return o.name == null ? 0 : -1;
                    } else {
                        if (o.name == null) {
                            return 1;
                        } else {
                            return name.compareTo(o.name);
                        }
                    }
                } else {
                    return result;
                }
            }
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((namespace == null) ? 0 : namespace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Fqn other = (Fqn) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (namespace == null) {
            if (other.namespace != null)
                return false;
        } else if (!namespace.equals(other.namespace))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Fqn [namespace=");
        builder.append(namespace);
        builder.append(", name=");
        builder.append(name);
        builder.append("]");
        return builder.toString();
    }

}
