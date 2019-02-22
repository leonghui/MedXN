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
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import com.google.common.collect.ImmutableList;
import org.hl7.fhir.r4.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FhirQueryClient {
    private final FhirContext context = FhirContext.forDstu3();
    private final IGenericClient client;
    private final String FHIR_SERVER_URL;
    private final String cacheFolder = System.getProperty("user.dir") + File.separator + "tmp";

    private FhirQueryClient(String url, Integer timeout) {
        FHIR_SERVER_URL = url;
        client = context.newRestfulGenericClient(FHIR_SERVER_URL);
        int TIMEOUT_SEC = timeout;
        context.getRestfulClientFactory().setSocketTimeout(TIMEOUT_SEC * 1000);
    }

    public static FhirQueryClient createFhirQueryClient(String url, Integer timeout) {
        return new FhirQueryClient(url, timeout);
    }

    private ImmutableList<? extends Resource> getAllResources(String className) {
        ImmutableList<? extends Resource> resources = null;

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

    private ImmutableList<? extends Resource> queryResources(String className) {
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
    private ImmutableList<? extends Resource> readCachedResources(String className) {
        Path path = getCachedFilePath(className);

        ImmutableList<Resource> resources = null;

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

    private void writeCachedResources(String className, ImmutableList<? extends Resource> resources) {

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

    @SuppressWarnings("unchecked")
    public ImmutableList<Medication> getAllMedications() {
        return (ImmutableList<Medication>) getAllResources("Medication");
    }

    public ImmutableList<MedicationKnowledge> getAllMedicationKnowledge() {
        return (ImmutableList<MedicationKnowledge>) getAllResources("MedicationKnowledge");
    }

    @SuppressWarnings("unchecked")
    public ImmutableList<Substance> getAllSubstances() {
        return (ImmutableList<Substance>) getAllResources("Substance");
    }

    public String getServerUrl() {
        return FHIR_SERVER_URL;
    }

    public FhirContext getContext() {
        return context;
    }

    private Path getCachedFilePath(String className) {
        return Paths.get(cacheFolder + File.separator + className + ".json");
    }

    public Map<String, String> getDosageFormMap() {
        Map<String, String> dosageFormMap = new LinkedHashMap<>();

        getAllMedications().parallelStream()
                .map(Medication::getForm)
                .map(CodeableConcept::getCodingFirstRep)
                .forEach(coding ->
                        dosageFormMap.putIfAbsent(coding.getCode(), coding.getDisplay())
                );

        return dosageFormMap;
    }
}