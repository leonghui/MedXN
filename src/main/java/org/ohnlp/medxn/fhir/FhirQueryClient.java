package org.ohnlp.medxn.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.hl7.fhir.dstu3.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FhirQueryClient {
    private final FhirContext context = FhirContext.forDstu3();
    private final String FHIR_SERVER_URL = "http://<server>:<port>/baseDstu3";
    private final IGenericClient client = context.newRestfulGenericClient(FHIR_SERVER_URL);
    private final String cacheFolder = System.getProperty("user.dir") + File.separator + "tmp";

    private FhirQueryClient() {
        int TIMEOUT_SEC = 60;
        context.getRestfulClientFactory().setSocketTimeout(TIMEOUT_SEC * 1000);
    }

    public static FhirQueryClient createFhirQueryClient() {
        return new FhirQueryClient();
    }

    private List<? extends Resource> getAllResources(String className) {
        List<? extends Resource> resources = new ArrayList<>();

        Path path = getCachedFilePath(className);

        if (Files.exists(path)) {
            if (Files.isReadable(path)) {
                resources = readCachedResources(className);
            }
        } else {
            try {
                Files.createDirectories(Paths.get(cacheFolder));
                Files.createFile(path);
            } catch (IOException ioe) {
                System.out.println(ioe.getClass());
                System.out.println("ERROR: Check creating " + path.toString());
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


        return resources;
    }


    private List<? extends Resource> readCachedResources(String className) {
        List<Resource> resources = new ArrayList<>();

        Path path = getCachedFilePath(className);

        try {
            BufferedReader reader = Files.newBufferedReader(path);

            Bundle bundle = context.newJsonParser().parseResource(Bundle.class, reader);
            bundle.getEntry().forEach(entry -> resources.add(entry.getResource()));
            reader.close();

        } catch (IOException ioe) {
            System.out.println(ioe.getClass());
            System.out.println("ERROR: Check reading from " + path.toString());
        }

        return resources;
    }

    private void writeCachedResources(String className, List<? extends Resource> resources) {

        Path path = getCachedFilePath(className);

        try {
            BufferedWriter writer = Files.newBufferedWriter(path);

            Bundle bundle = new Bundle();

            resources.forEach( resource -> bundle.addEntry().setResource(resource));

            context.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(bundle, writer);

            writer.flush();
            writer.close();

        } catch (IOException ioe) {
            System.out.println(ioe.getClass());
            System.out.println("ERROR: Check writing to " + path.toString() );
        }
    }

    @SuppressWarnings("unchecked")
    public List<Medication> getAllMedications() {
        return (List<Medication>) getAllResources("Medication");
    }

    @SuppressWarnings("unchecked")
    public List<Substance> getAllSubstances() {
        return (List<Substance>) getAllResources("Substance");
    }

    public String getFhirServerUrl() {
        return FHIR_SERVER_URL;
    }

    private Path getCachedFilePath(String className) {
        return Paths.get(cacheFolder + File.separator + className + ".json");
    }

    public Table<String, String, String> getAllDosageForms() {

        // Data structure to store concept terms
        // rxCui, tty, term
        Table<String, String, String> doseFormTable = HashBasedTable.create();

        List<Medication> medications = getAllMedications();

        medications.forEach( medication -> {
            Coding formCode = medication.getForm().getCodingFirstRep();
            doseFormTable.put(formCode.getCode(), "DF", formCode.getDisplay());
        });

        return doseFormTable;
    }
}