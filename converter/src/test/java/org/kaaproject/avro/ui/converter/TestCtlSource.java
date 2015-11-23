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

package org.kaaproject.avro.ui.converter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.avro.ui.shared.Fqn;

public class TestCtlSource implements CtlSource {
    
    private Map<Fqn, List<Integer>> ctlTypes = new HashMap<>();
    {
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeA"), Arrays.asList(1,2,3,4,5));
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeB"), Arrays.asList(1,5));
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeC"), Arrays.asList(1,2,3,4,5));
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeD"), Arrays.asList(1,2,3,4,10,20));
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeE"), Arrays.asList(2,3,4,50));
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeF"), Arrays.asList(1,20,30,40,50));
        ctlTypes.put(new Fqn("org.kaaproject.ctl.TypeG"), Arrays.asList(1,22,35,47,51));
    }

    @Override
    public Map<Fqn, List<Integer>> getCtlTypes() {
        return ctlTypes;
    }

}