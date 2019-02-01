package uk.gov.hmcts.reform.em.stitching.service.impl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.stitching.domain.BundleDocument;
import uk.gov.hmcts.reform.em.stitching.service.DmStoreDownloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
@Transactional
public class DmStoreDownloaderImpl implements DmStoreDownloader {

    private final OkHttpClient okHttpClient;

    private final AuthTokenGenerator authTokenGenerator;

    private String dmStoreAppBaseUrl;

    private final String dmStoreAppDocumentBinaryEndpointPattern = "/documents/%s/binary";

    public DmStoreDownloaderImpl(OkHttpClient okHttpClient,
                                 AuthTokenGenerator authTokenGenerator,
                                 @Value("${dm-store-app.base-url}") String dmStoreAppBaseUrl) {
        this.okHttpClient = okHttpClient;
        this.authTokenGenerator = authTokenGenerator;
        this.dmStoreAppBaseUrl = dmStoreAppBaseUrl;
    }

    @Override
    public Stream<File> downloadFiles(Stream<BundleDocument> bundleDocuments) {
        return bundleDocuments
            .parallel()
            .map(unchecked(this::downloadFile));
    }

    private File downloadFile(BundleDocument bundleDocument) throws DocumentTaskProcessingException {
        try {
            Request request = new Request.Builder()
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(dmStoreAppBaseUrl+String.format(dmStoreAppDocumentBinaryEndpointPattern, bundleDocument.getDocumentId()))
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                Path tempPath = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + bundleDocument.getDocumentId());

                try {
                    Files.copy(response.body().byteStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {
                    throw new DocumentTaskProcessingException("Could not copy the file to a temp location", e);
                }

                return tempPath.toFile();

            }
            else {
                throw new DocumentTaskProcessingException("Could not access the binary. HTTP response: " + response.code());
            }
        }
        catch (RuntimeException | IOException e) {
            throw new DocumentTaskProcessingException(String.format("Could not access the binary: %s", e.getMessage()), e);
        }
    }

}
