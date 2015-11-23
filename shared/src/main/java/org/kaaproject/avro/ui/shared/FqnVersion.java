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

public class FqnVersion extends Fqn {

    private static final long serialVersionUID = 5169512001216622502L;
    
    private int version;
    
    public FqnVersion() {
        super();
    }
    
    public FqnVersion(Fqn fqn, int version) {
        super(fqn);
        this.version = version;
    }
    
    public FqnVersion(String fqnString, int version) {
        super(fqnString);
        this.version = version;
    }
    
    public FqnVersion(String namespace, String name, int version) {
        super(namespace, name);
        this.version = version;
    }
    
    public Fqn getFqn() {
        return new Fqn(this.namespace, this.name);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FqnVersion other = (FqnVersion) obj;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FqnVersion [version=");
        builder.append(version);
        builder.append(", toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
