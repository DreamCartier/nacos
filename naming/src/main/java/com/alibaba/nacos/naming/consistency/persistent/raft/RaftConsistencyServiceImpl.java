/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.nacos.naming.consistency.persistent.raft;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.naming.consistency.DataListener;
import com.alibaba.nacos.naming.consistency.Datum;
import com.alibaba.nacos.naming.consistency.persistent.PersistentConsistencyService;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Use simplified Raft protocol to maintain the consistency status of Nacos cluster.
 *
 * @author nkorange
 * @since 1.0.0
 */
@Service
public class RaftConsistencyServiceImpl implements PersistentConsistencyService {

    @Autowired
    private RaftCore raftCore;

    @Override
    public void put(String key, Record value) throws NacosException {
        try {
            raftCore.signalPublish(key, value);
        } catch (Exception e) {
            Loggers.RAFT.error("Raft put failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Raft put failed, key:" + key + ", value:" + value);
        }
    }

    @Override
    public void remove(String key) throws NacosException {
        try {
            raftCore.signalDelete(key);
        } catch (Exception e) {
            Loggers.RAFT.error("Raft remove failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Raft remove failed, key:" + key);
        }
    }

    @Override
    public Datum get(String key) throws NacosException {
        return raftCore.getDatum(key);
    }

    @Override
    public void listen(String key, DataListener listener) throws NacosException {
        raftCore.listen(key, listener);
    }

    @Override
    public void unlisten(String key, DataListener listener) throws NacosException {
        raftCore.unlisten(key, listener);
    }

    @Override
    public boolean isResponsible(String key) {
        return false;
    }

    @Override
    public String getResponsibleServer(String key) {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return raftCore.isInitialized();
    }

    public void onPut(Datum datum, RaftPeer source) throws NacosException {
        try {
            raftCore.onPublish(datum, source);
        } catch (Exception e) {
            Loggers.RAFT.error("Raft onPut failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Raft onPut failed, datum:" + datum + ", source: " + source);
        }
    }

    public void onRemove(Datum datum, RaftPeer source) throws NacosException {
        try {
            raftCore.onDelete(datum, source);
        } catch (Exception e) {
            Loggers.RAFT.error("Raft onRemove failed.", e);
            throw new NacosException(NacosException.SERVER_ERROR, "Raft onRemove failed, datum:" + datum + ", source: " + source);
        }
    }
}