package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.itextpdf.text.DocumentException;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileRequestDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FilesServiceClient;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.Exceptions.UnprocessableEntityException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.PrescriptionPdfGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class PrescriptionServiceImplTest {

    @Autowired
    private PrescriptionServiceImpl prescriptionService;

    @MockBean
    private VisitRepo visitRepo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Test
    void createPrescription_success() throws DocumentException {
        String visitId = "v1";
        PrescriptionResponseDTO request = new PrescriptionResponseDTO();
        Visit visit = Visit.builder().visitId(visitId).build();
        FileResponseDTO fileResponse = new FileResponseDTO();
        fileResponse.setFileId("file123");
        fileResponse.setFileName("prescription.pdf");

        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.just(visit));
        when(visitRepo.save(any())).thenReturn(Mono.just(visit));
        when(filesServiceClient.addFile(any(FileRequestDTO.class))).thenReturn(Mono.just(fileResponse));

        byte[] fakePdf = "pdf".getBytes();
        try (MockedStatic<PrescriptionPdfGenerator> mocked = mockStatic(PrescriptionPdfGenerator.class)) {
            mocked.when(() -> PrescriptionPdfGenerator.generatePrescriptionPdf(any(PrescriptionResponseDTO.class)))
                    .thenReturn(fakePdf);

            StepVerifier.create(prescriptionService.createPrescription(visitId, request))
                    .expectNextMatches(resp -> resp.getPrescriptionId() != null)
                    .verifyComplete();
        }

        verify(visitRepo).findByVisitId(visitId);
        verify(filesServiceClient).addFile(any());
        verify(visitRepo).save(any());
    }

    @Test
    void createPrescription_visitNotFound() {
        String visitId = "missing";
        PrescriptionResponseDTO req = new PrescriptionResponseDTO();
        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.empty());

        StepVerifier.create(prescriptionService.createPrescription(visitId, req))
                .expectErrorMatches(t -> t instanceof NotFoundException &&
                        t.getMessage().contains("Visit not found"))
                .verify();

        verify(filesServiceClient, never()).addFile(any());
    }

    @Test
    void createPrescription_fileServiceEmpty() throws DocumentException {
        String visitId = "v2";
        Visit visit = Visit.builder().visitId(visitId).build();
        PrescriptionResponseDTO req = new PrescriptionResponseDTO();

        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.just(visit));
        when(filesServiceClient.addFile(any())).thenReturn(Mono.empty());

        byte[] fakePdf = "pdf".getBytes();
        try (MockedStatic<PrescriptionPdfGenerator> mocked = mockStatic(PrescriptionPdfGenerator.class)) {
            mocked.when(() -> PrescriptionPdfGenerator.generatePrescriptionPdf(any())).thenReturn(fakePdf);

            StepVerifier.create(prescriptionService.createPrescription(visitId, req))
                    .expectErrorMatches(t -> t instanceof UnprocessableEntityException &&
                            t.getMessage().contains("File service"))
                    .verify();
        }
    }

    @Test
    void getPrescriptionPdf_success() {
        String visitId = "v3";
        Visit visit = Visit.builder().visitId(visitId).prescriptionFileId("file123").build();
        FileResponseDTO file = new FileResponseDTO();
        file.setFileId("file123");
        file.setFileData("data".getBytes());

        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.just(visit));
        when(filesServiceClient.getFile("file123")).thenReturn(Mono.just(file));

        StepVerifier.create(prescriptionService.getPrescriptionPdf(visitId))
                .expectNextMatches(bytes -> bytes.length > 0)
                .verifyComplete();

        verify(visitRepo).findByVisitId(visitId);
        verify(filesServiceClient).getFile("file123");
    }

    @Test
    void getPrescriptionPdf_visitNotFound() {
        String visitId = "missing";
        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.empty());

        StepVerifier.create(prescriptionService.getPrescriptionPdf(visitId))
                .expectErrorMatches(t -> t instanceof NotFoundException &&
                        t.getMessage().contains("Visit not found"))
                .verify();

        verify(filesServiceClient, never()).getFile(anyString());
    }

    @Test
    void getPrescriptionPdf_fileNotFound() {
        String visitId = "v4";
        Visit visit = Visit.builder().visitId(visitId).prescriptionFileId("file999").build();

        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.just(visit));
        when(filesServiceClient.getFile("file999")).thenReturn(Mono.empty());

        StepVerifier.create(prescriptionService.getPrescriptionPdf(visitId))
                .expectErrorMatches(t -> t instanceof NotFoundException &&
                        t.getMessage().contains("Prescription file not found: file999"))
                .verify();

        verify(visitRepo).findByVisitId(visitId);
        verify(filesServiceClient).getFile("file999");
    }

    @Test
    void getPrescriptionPdf_fileServiceError() {
        String visitId = "v5";
        Visit visit = Visit.builder().visitId(visitId).prescriptionFileId("fileError").build();

        when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.just(visit));
        when(filesServiceClient.getFile("fileError"))
                .thenReturn(Mono.error(new RuntimeException("File service failure")));

        StepVerifier.create(prescriptionService.getPrescriptionPdf(visitId))
                .expectErrorMatches(t -> t instanceof UnprocessableEntityException &&
                        t.getMessage().contains("Failed to retrieve prescription file"))
                .verify();

        verify(visitRepo).findByVisitId(visitId);
        verify(filesServiceClient).getFile("fileError");
    }
}
