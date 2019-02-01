package uk.gov.hmcts.reform.em.stitching.service.impl;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.service.DmStoreUploader;

import java.io.File;
import java.io.IOException;

@Service
@Transactional
public class DmStoreUploaderImpl implements DmStoreUploader {

    private final OkHttpClient okHttpClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final SubjectResolver<User> userResolver;

    private final String dmStoreAppBaseUrl;

    private final String dmStoreUploadEndpoint = "/documents";

    public DmStoreUploaderImpl(OkHttpClient okHttpClient,
                               AuthTokenGenerator authTokenGenerator,
                               @Value("${dm-store-app.base-url}") String dmStoreAppBaseUrl,
                               SubjectResolver<User> userResolver) {
        this.okHttpClient = okHttpClient;
        this.authTokenGenerator = authTokenGenerator;
        this.dmStoreAppBaseUrl = dmStoreAppBaseUrl;
        this.userResolver = userResolver;
    }

    @Override
    public void uploadFile(File file, DocumentTask documentTask) throws DocumentTaskProcessingException {
        if (documentTask.getBundle().getStitchedDocId() != null) {
            uploadNewDocumentVersion(file, documentTask);
        } else {
            uploadNewDocument(file, documentTask);
        }
    }

    private void uploadNewDocument(File file, DocumentTask documentTask) throws DocumentTaskProcessingException {

        try {

            MultipartBody requestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("classification", "PUBLIC")
                    .addFormDataPart("files", file.getName(), RequestBody.create(MediaType.get("application/pdf"), file))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("user-id", getUserId(documentTask))
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(dmStoreAppBaseUrl + dmStoreUploadEndpoint)
                    .method("POST", requestBody)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {

                JSONObject jsonObject = new JSONObject(response.body().string());

                String[] split = jsonObject
                        .getJSONObject("_embedded")
                        .getJSONArray("documents")
                        .getJSONObject(0)
                        .getJSONObject("_links")
                        .getJSONObject("self")
                        .getString("href")
                        .split("\\/");

                String documentId = split[split.length - 1];

                documentTask.getBundle().setStitchedDocId(documentId);
                documentTask.getBundle().setStitchedDocumentURI(dmStoreAppBaseUrl + dmStoreUploadEndpoint + "/" + documentId);
            } else {
                throw new DocumentTaskProcessingException("Couldn't upload the file. Response code: " + response.code(), null);
            }

        } catch (RuntimeException | IOException e) {
            throw new DocumentTaskProcessingException(String.format("Couldn't upload the file:  %s", e.getMessage()), e);
        }
    }

    private void uploadNewDocumentVersion(File file, DocumentTask documentTask) throws DocumentTaskProcessingException {

        try {

            MultipartBody requestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.get("application/pdf"), file))
                    .build();

            Request request = new Request.Builder()
                    .addHeader("user-id", getUserId(documentTask))
                    .addHeader("user-roles", "caseworker")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .url(dmStoreAppBaseUrl + dmStoreUploadEndpoint + "/" + documentTask.getBundle().getStitchedDocId())
                    .method("POST", requestBody)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new DocumentTaskProcessingException("Couldn't upload the file. Response code: " + response.code(), null);
            }

        } catch (RuntimeException | IOException e) {
            throw new DocumentTaskProcessingException("Couldn't upload the file", e);
        }
    }

    private String getUserId(DocumentTask documentTask) {
        User user = userResolver.getTokenDetails(documentTask.getJwt());
        return user.getPrincipal();
    }

}
