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

public class FqnKey implements Serializable {

    private static final long serialVersionUID = -2705987324288726672L;
    
    private Integer id;
    private Fqn fqn;
    private boolean isLocalFqn;
    
    public FqnKey() {
    }

    public FqnKey(Integer id) {
        super();
        this.isLocalFqn = true;
        this.id = id;
    }
    
    public FqnKey(Fqn fqn) {
        super();
        this.isLocalFqn = false;
        this.fqn = fqn;
    }

    public boolean isLocalFqn() {
        return isLocalFqn;
    }

    public Integer getId() {
        return id;
    }

    public Fqn getFqn() {
        return fqn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (isLocalFqn ? 1231 : 1237);
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
        FqnKey other = (FqnKey) obj;
        if (fqn == null) {
            if (other.fqn != null)
                return false;
        } else if (!fqn.equals(other.fqn))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (isLocalFqn != other.isLocalFqn)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FqnKey [isLocalFqn=");
        builder.append(isLocalFqn);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }

}
