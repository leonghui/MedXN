/*******************************************************************************
 * Copyright (c) 2018-2019. Leong Hui Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.ohnlp.medxn.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hl7.fhir.r4.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FhirQueryClient {
    private FhirContext context = FhirContext.forR4();
    private IGenericClient client;
    private final String cacheFolder = System.getProperty("user.dir") + File.separator + "tmp";

    private FhirQueryClient(String url, Integer timeout) {
        context.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
        context.getRestfulClientFactory().setSocketTimeout(timeout * 1000);

        client = context.newRestfulGenericClient(url);
    }

    public void destroy() {
        context = null;
        client = null;
    }

    public static FhirQueryClient createFhirQueryClient(String url, Integer timeout) {
        return new FhirQueryClient(url, timeout);
    }

    private List<? extends Resource> getAllResources(String className) {
        List<? extends Resource> resources = null;

        Path path = getCachedFilePath(className);

        if (Files.exists(path)) {
            if (Files.isReadable(path)) {
                resources = readCachedResources(className);

                int tryCount = 0;
                // retry if time-out errors
                while (resources.isEmpty() && tryCount < 3) {
                    System.out.println("WARN: Empty " + className + " bundle, retrying...");

                    resources = queryResources(className);
                    writeCachedResources(className, resources);
                    tryCount++;
                }
            }
        } else {
            try {
                Files.createDirectories(Paths.get(cacheFolder));
                Files.createFile(path);
            } catch (IOException ioe) {
                System.out.println(ioe.getClass());
                System.out.println("ERROR: Creating " + path.toString());
            }
            resources = queryResources(className);
            writeCachedResources(className, resources);
        }

        return resources;

    }

    private List<? extends Resource> queryResources(String className) {
        List<Resource> resources = new ArrayList<>();

        try {
            int QUERY_SIZE = 10000;
            Bundle response = client.search()
                    .forResource(className)
                    .returnBundle(Bundle.class)
                    .count(QUERY_SIZE)
                    .execute();

            // handle paged response
            do {
                for (Bundle.BundleEntryComponent entry : response.getEntry()) {
                    resources.add(entry.getResource());
                }
                if (response.getLink(Bundle.LINK_NEXT) != null) {
                    response = client.loadPage().next(response).execute();
                } else {
                    response = null;
                }
            } while (response != null);

        } catch (FhirClientConnectionException eof) {
            System.out.println(eof.getClass());
            System.out.println("ERROR: FHIR endpoint unreachable or query timed-out.");
            System.out.println("Please check FHIR_SERVER_URL, increase TIMEOUT_SEC, or reduce QUERY_SIZE.");
        }

        return ImmutableList.copyOf(resources);
    }


    @SuppressWarnings("UnstableApiUsage")
    private List<? extends Resource> readCachedResources(String className) {
        Path path = getCachedFilePath(className);

        List<Resource> resources = null;

        try {

            BufferedReader reader = Files.newBufferedReader(path);

            Bundle bundle = context.newJsonParser().parseResource(Bundle.class, reader);

            resources = bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .collect(ImmutableList.toImmutableList());

            reader.close();

        } catch (IOException ioe) {
            System.out.println(ioe.getClass());
            System.out.println("ERROR: Reading from " + path.toString());
        }

        return resources;
    }

    private void writeCachedResources(String className, List<? extends Resource> resources) {

        Path path = getCachedFilePath(className);

        try {
            BufferedWriter writer = Files.newBufferedWriter(path);

            Bundle bundle = new Bundle();

            resources.forEach(resource -> bundle.addEntry().setResource(resource));

            context.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(bundle, writer);

            writer.flush();
            writer.close();

        } catch (IOException ioe) {
            System.out.println(ioe.getClass());
            System.out.println("ERROR: Writing to " + path.toString());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public Map<String, Medication> getAllMedications() {
        return getAllResources("Medication").stream()
                .map(Medication.class::cast)
                .collect(ImmutableMap.toImmutableMap(
                        medication -> medication.getCode().getCodingFirstRep().getDisplay(),
                        medication -> medication
                ));
    }

    @SuppressWarnings("UnstableApiUsage")
    public Map<String, MedicationKnowledge> getAllMedicationKnowledge() {
        return getAllResources("MedicationKnowledge").stream()
                .map(MedicationKnowledge.class::cast)
                .collect(ImmutableMap.toImmutableMap(
                        medicationKnowledge -> medicationKnowledge.getCode().getCodingFirstRep().getDisplay(),
                        medicationKnowledge -> medicationKnowledge
                ));
    }

    @SuppressWarnings("UnstableApiUsage")
    public Map<String, Substance> getAllSubstances() {
        return getAllResources("Substance").stream()
                .map(Substance.class::cast)
                .collect(ImmutableMap.toImmutableMap(
                        substance -> substance.getCode().getCodingFirstRep().getDisplay(),
                        substance -> substance
                ));
    }

    private Path getCachedFilePath(String className) {
        return Paths.get(cacheFolder + File.separator + className + ".json");
    }
}