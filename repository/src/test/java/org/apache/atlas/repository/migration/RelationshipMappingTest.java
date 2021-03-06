/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.repository.migration;

import com.google.inject.Inject;
import org.apache.atlas.TestModules;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.typedef.AtlasRelationshipDef;
import org.apache.atlas.repository.Constants;
import org.apache.atlas.repository.impexp.ZipFileResourceTestUtils;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.store.AtlasTypeDefStore;
import org.apache.atlas.type.AtlasRelationshipType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.jcodings.util.Hash;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.apache.atlas.repository.impexp.ZipFileResourceTestUtils.loadModelFromJson;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = TestModules.TestOnlyModule.class)
public class RelationshipMappingTest {

    @Inject
    private AtlasTypeDefStore typeDefStore;

    @Inject
    private AtlasTypeRegistry typeRegistry;

    @BeforeClass
    public void setup() throws IOException, AtlasBaseException {
        loadModelFromJson("0000-Area0/0010-base_model.json", typeDefStore, typeRegistry);
        loadModelFromJson("1000-Hadoop/1030-hive_model.json", typeDefStore, typeRegistry);
    }

    @Test
    public void createLookup() {
        Map<String, String> cache = RelationshipCacheGenerator.get(typeRegistry);
        assertEquals(cache.size(), getLegacyAttributeCount() - 1);
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            assertTrue(StringUtils.isNotEmpty(entry.getKey()));
            assertTrue(entry.getKey().startsWith(Constants.INTERNAL_PROPERTY_KEY_PREFIX), entry.getKey());
        }
    }

    private int getLegacyAttributeCount() {
        int count = 0;
        for (AtlasRelationshipType rt : typeRegistry.getAllRelationshipTypes()) {
            AtlasRelationshipDef rd = rt.getRelationshipDef();
            if(rd.getEndDef1().getIsLegacyAttribute()) {
                count++;
            }

            if(rd.getEndDef2().getIsLegacyAttribute()) {
                count++;
            }
        }

        return count;
    }
}
