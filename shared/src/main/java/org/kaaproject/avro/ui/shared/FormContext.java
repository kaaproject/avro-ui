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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FormContext implements Serializable {

    private static final long serialVersionUID = -3706030853085644429L;
    
    private int idSequence = 0;
    private Map<Fqn, RecordField> recordsMetadata = new HashMap<>();
    
    private RecordField rootRecord;
    
    private Map<FqnKey, Fqn> declaredTypes = new HashMap<>();    
    private Map<Fqn, FqnKey> fqnToKeyMap = new HashMap<>();    
    private Map<Integer, RecordField> typeHolders = new HashMap<>();
    private Map<Integer, RecordField> typeConsumers = new HashMap<>();    
    private Map<Fqn, List<Integer>> ctlTypes = new HashMap<>();
    private Map<Fqn, FqnVersion> ctlDependencies = new HashMap<>();
    private List<FqnVersion> ctlDependenciesList = new ArrayList<>();
    private boolean isCtlSchema = false;
    
    
    private transient List<DeclaredTypesListener> declaredTypesListeners = new ArrayList<>();
    
    private transient List<CtlDependenciesListener> ctlDependenciesListeners = new ArrayList<>();
    
    public FormContext() {
    }
    
    public FormContext(Map<Fqn, List<Integer>> ctlTypes) {
        this.ctlTypes.putAll(ctlTypes);
        this.isCtlSchema = true;
        for (Fqn fqn : this.ctlTypes.keySet()) {
            FqnKey key = new FqnKey(fqn);
            declaredTypes.put(key, fqn);
            fqnToKeyMap.put(fqn, key);
        }
    }
    
    protected int nextFieldId() {
        return idSequence++;
    }

    public void putRecordMetadata(String namespace, String name, RecordField field) {
        putRecordMetadata(new Fqn(namespace, name), field);
    }
    
    public void putRecordMetadata(Fqn fqn, RecordField field) {
        recordsMetadata.put(fqn, field);
    }
    
    public boolean containsRecordMetadata(String namespace, String name) {
        return containsRecordMetadata(new Fqn(namespace, name));
    }
    
    public boolean containsRecordMetadata(Fqn fqn) {
        return recordsMetadata.containsKey(fqn);
    }
    
    public RecordField getRecordMetadata(String namespace, String name) {
        return getRecordMetadata(new Fqn(namespace, name));
    }
    
    public RecordField getRecordMetadata(Fqn fqn) {
        return recordsMetadata.get(fqn);
    }
    
    public void setRootRecord(RecordField rootRecord) {
        this.rootRecord = rootRecord;
    }
    
    public RecordField getRootRecord() {
        return this.rootRecord;
    }
    
    public boolean isCtlSchema() {
        return isCtlSchema;
    }

    private FqnKey overridenCtlKey = null;
    
    public void updateTypeHolder(RecordField typeHolder) {
        FqnKey key = new FqnKey(typeHolder.getId());
        Fqn fqn = typeHolder.getDeclaredFqn();
        
        Map<FqnKey, List<RecordField>> affectedConsumers = new HashMap<>();
        
        if (isCtlSchema && typeHolder.isRoot()) {
            if (overridenCtlKey != null && !overridenCtlKey.getFqn().equals(fqn)) {
                declaredTypes.put(overridenCtlKey, overridenCtlKey.getFqn());
                fqnToKeyMap.put(overridenCtlKey.getFqn(), overridenCtlKey);
                List<RecordField> matchedConsumers = new ArrayList<>(); 
                for (RecordField consumer : typeConsumers.values()) {
                    if (key.equals(consumer.getConsumedFqnKey())) {
                        matchedConsumers.add(consumer);
                    }
                }
                affectedConsumers.put(overridenCtlKey, matchedConsumers);
                overridenCtlKey = null;
            }
            if (ctlTypes.containsKey(fqn)) {
                overridenCtlKey = new FqnKey(fqn);
                declaredTypes.remove(overridenCtlKey);                
                fqnToKeyMap.remove(fqn);
                List<RecordField> matchedConsumers = new ArrayList<>(); 
                for (RecordField consumer : typeConsumers.values()) {
                    if (overridenCtlKey.equals(consumer.getConsumedFqnKey())) {
                        matchedConsumers.add(consumer);
                    }
                }
                affectedConsumers.put(key, matchedConsumers);
            }
        }
        typeHolders.put(typeHolder.getId(), typeHolder);
        Fqn oldFqn = declaredTypes.put(key, fqn);
        if (oldFqn != null) {
            fqnToKeyMap.remove(oldFqn);
        }
        if (fqn != null) {
            fqnToKeyMap.put(fqn, key);
        }
        fireDeclaredTypesChanged();
        
        for (FqnKey keyToSet : affectedConsumers.keySet()) {
            List<RecordField> consumers = affectedConsumers.get(keyToSet);
            for (RecordField consumer : consumers) {
                consumer.updateConsumedFqnKey(keyToSet);
            }
        }
        
    }
    
    public Map<Integer, RecordField> getTypeHolders() {
        return typeHolders;
    }
    
    public boolean isFqnAlreadyDeclared(int id, Fqn fqn, boolean localOnly) {
        if (fqn != null) {
            FqnKey declaredKey = fqnToKeyMap.get(fqn);
            if (declaredKey != null) {
                if ((!declaredKey.isLocalFqn() && !localOnly) || 
                        (declaredKey.isLocalFqn() && declaredKey.getId().intValue() != id)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean checkIsVersionAvailable(Fqn fqn, Integer version) {
        if (fqn != null) {
            List<Integer> versions = ctlTypes.get(fqn);
            if (versions != null) {
                return !versions.contains(version);
            }
        }
        return true;
    }
    
    private FormField getRootParent(FormField formField) {
        FormField parent = formField;
        while (true) {
            if (parent.getParentField() != null) {
                parent = parent.getParentField();
            } else {
                return parent;
            }
        }
    }
    
    private boolean isAttachedToRoot(FormField formField) {
        FormField rootParent = getRootParent(formField);
        return rootParent.getId() == rootRecord.getId();
    }
    
    private List<RecordField> getAttachedTypeConsumers() {
        List<RecordField> attachedTypeConsumers = new ArrayList<>();
        for (RecordField typeConsumer : typeConsumers.values()) {
            if (isAttachedToRoot(typeConsumer)) {
                attachedTypeConsumers.add(typeConsumer);
            }
        }
        return attachedTypeConsumers; 
    }
    
    private Map<FqnKey, RecordField> getDetachedTypeHolders() {
        Map<FqnKey, RecordField> detachedTypeHolders = new HashMap<>();
        for (RecordField typeHolder : typeHolders.values()) {
            if (!isAttachedToRoot(typeHolder)) {
                detachedTypeHolders.put(new FqnKey(typeHolder.getId()), typeHolder);
            }
        }
        return detachedTypeHolders;
    }
    
    public boolean removeTypeHolder(RecordField typeHolder) {
        
        if (!typeHolders.containsKey(typeHolder.getId())) {
            return true;
        }
        
        FormField parent = typeHolder.getParentField();
        if (parent != null && parent instanceof UnionField) {
            ((UnionField)parent).setValue(null, true, false);
            parent.setParentField(null);
        }
        
        boolean foundDetachedReference = false;
        
        do {
            foundDetachedReference = false;
            List<RecordField> attachedTypeConsumers = getAttachedTypeConsumers();
            Map<FqnKey, RecordField> detachedTypeHolders = getDetachedTypeHolders();
            for (RecordField attachedTypeConsumer : attachedTypeConsumers) {
                FqnKey fqnKey = attachedTypeConsumer.getConsumedFqnKey();
                if (fqnKey != null) {
                    RecordField detachedTypeHolder = detachedTypeHolders.get(fqnKey);
                    if (detachedTypeHolder != null) {
                        foundDetachedReference = true;
                        switchTypeReference(detachedTypeHolder, attachedTypeConsumer);
                    }
                }
            }
        } while (foundDetachedReference);
        
        boolean removed = false;
        
        if (typeHolder.getParentField() == null || typeHolder.getParentField().getParentField() == null) {
            FqnKey key = new FqnKey(typeHolder.getId());
            typeHolders.remove(typeHolder.getId());
            Fqn fqn = declaredTypes.remove(key);
            if (fqn != null) {
                fqnToKeyMap.remove(fqn);
            }
            fireDeclaredTypesChanged();
            removed = true;
        } 
        orderSchemaTypes();
        return removed;
    }
    
    private void switchTypeReference(RecordField typeHolder, RecordField typeConsumer) {
        UnionField parentUnion = (UnionField)typeHolder.getParentField();
        if (parentUnion != null) {
            FormField reference = typeConsumer.clone(true);
            parentUnion.setValue(reference, true, false);
        }
        
        parentUnion = (UnionField)typeConsumer.getParentField();
        parentUnion.setValue(typeHolder);
    }
    
    public void orderSchemaTypes() {
        boolean typesSwitched = false;
        do {
            Set<Integer> checkedTypeHolders = new HashSet<>();
            checkedTypeHolders.addAll(typeHolders.keySet());
            
            Map<FqnKey, RecordField> consumers = new HashMap<>();
            
            for (FormField field : rootRecord) {
                if (field instanceof RecordField) {
                    RecordField recordField = (RecordField)field;
                    if (recordField.isTypeHolder()) {
                        checkedTypeHolders.remove(recordField.getId());
                    } else if (recordField.isTypeConsumer()) {
                        FqnKey key = recordField.getConsumedFqnKey();
                        if (key != null && key.isLocalFqn()) {
                            if (checkedTypeHolders.remove(key.getId())) {
                                consumers.put(key, recordField);
                            }
                        }
                    }
                }
            }
            
            typesSwitched = !consumers.isEmpty();
            for (FqnKey key : consumers.keySet()) {
                RecordField consumer = consumers.get(key);
                RecordField holder = typeHolders.get(key.getId());
                switchTypeReference(holder, consumer);
            }
        } while (typesSwitched);
    }

    public void registerTypeConsumer(RecordField consumer) {
        typeConsumers.put(consumer.getId(), consumer);
        updateCtlDependencies();
    }
    
    public void unregisterTypeConsumer(Integer id) {
        typeConsumers.remove(id);
        updateCtlDependencies();
    }
    
    public void updateCtlDependencies() {
        Set<Fqn> consumedFqns = new HashSet<>();
        for (RecordField typeConsumer : typeConsumers.values()) {
            FqnKey key = typeConsumer.getConsumedFqnKey();
            if (key != null && !key.isLocalFqn()) {
                Fqn fqn = key.getFqn();
                consumedFqns.add(fqn);
            }
        }
        Set<Fqn> toRemove = new HashSet<>();
        for (Fqn key : ctlDependencies.keySet()) {
            if (!consumedFqns.contains(key)) {
                toRemove.add(key);
            }
        }
        Set<Fqn> toAdd = new HashSet<>();
        for (Fqn key : consumedFqns) {
            if (!ctlDependencies.containsKey(key)) {
                toAdd.add(key);
            }
        }
        boolean changed = !toRemove.isEmpty() || !toAdd.isEmpty();
        
        for (Fqn key : toRemove) {
            ctlDependencies.remove(key);
        }
        for (Fqn key : toAdd) {
            FqnVersion fqnVersion = new FqnVersion(key, getMaxVersion(key));
            ctlDependencies.put(key, fqnVersion);
        }
        if (changed) {
            ctlDependenciesList.clear();
            ctlDependenciesList.addAll(ctlDependencies.values());
            Collections.sort(ctlDependenciesList);
        }
        
        fireCtlDependenciesChanged();
    }
    
    public List<FqnVersion> getCtlDependenciesList() {
        return ctlDependenciesList;
    }
    
    public void setCtlDependenciesList(List<FqnVersion> fqnVersions) {
        ctlDependencies.clear();
        ctlDependenciesList.clear();
        for (FqnVersion fqnVersion : fqnVersions) {
            ctlDependencies.put(fqnVersion.getFqn(), fqnVersion);
        }
        ctlDependenciesList.addAll(ctlDependencies.values());
        Collections.sort(ctlDependenciesList);
    }
    
    public Integer getMaxVersion(Fqn fqn) {
        List<Integer> versions = ctlTypes.get(fqn);
        if (versions != null) {
            return Collections.max(versions);
        }
        return Integer.valueOf(1);
    }
    
    public List<Integer> getAvailableVersions(Fqn fqn) {
        return ctlTypes.get(fqn);
    }
    
    protected FqnKey fqnToFqnKey(Fqn fqn) {
        return fqnToKeyMap.get(fqn);
    }
    
    public Map<FqnKey, Fqn> getDeclaredTypes() {
        return declaredTypes;
    }
    
    public boolean containsDeclaredType(FqnKey key) {
        return declaredTypes.containsKey(key);
    }
    
    public void addDeclaredTypesListener(DeclaredTypesListener listener) {
        declaredTypesListeners.add(listener);
    }
    
    public void removeDeclaredTypesListener(DeclaredTypesListener listener) {
        declaredTypesListeners.remove(listener);
    }
    
    public void addCtlDependenciesListener(CtlDependenciesListener listener) {
        ctlDependenciesListeners.add(listener);
    }
    
    public void removeCtlDependenciesListener(CtlDependenciesListener listener) {
        ctlDependenciesListeners.remove(listener);
    }
    
    private void fireDeclaredTypesChanged() {
        for (DeclaredTypesListener listener : declaredTypesListeners) {
            listener.onDeclaredTypesUpdated(declaredTypes);
        }
    }
    
    private void fireCtlDependenciesChanged() {
        for (CtlDependenciesListener listener : ctlDependenciesListeners) {
            listener.onCtlDependenciesUpdated(ctlDependenciesList);
        }
    }
    
    public static interface DeclaredTypesListener {
        
        public void onDeclaredTypesUpdated (Map<FqnKey, Fqn> declaredTypes); 
        
    }
    
    public static interface CtlDependenciesListener {
        
        public void onCtlDependenciesUpdated (List<FqnVersion> ctlDependenciesList); 
        
    }
    
}
