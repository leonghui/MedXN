package org.ohnlp.medxn.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import org.hl7.fhir.dstu3.model.*;

import java.util.ArrayList;
import java.util.List;

public class FhirQueryClient {
    private final FhirContext context = FhirContext.forDstu3();
    private final String FHIR_SERVER_URL = "http://<server>:<port>/baseDstu3";
    private final IGenericClient client = context.newRestfulGenericClient(FHIR_SERVER_URL);

    private FhirQueryClient() {
        int TIMEOUT_SEC = 60;
        context.getRestfulClientFactory().setSocketTimeout(TIMEOUT_SEC * 1000);
    }

    public static FhirQueryClient createFhirQueryClient() {
        return new FhirQueryClient();
    }

    private List<? extends Resource> getAllResources(String className) {
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
}