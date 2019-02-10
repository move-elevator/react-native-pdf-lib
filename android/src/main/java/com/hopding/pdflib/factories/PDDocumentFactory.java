package com.hopding.pdflib.factories;

import android.content.Context;
import android.content.res.AssetManager;

import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a PDDocument object and applies actions from a JSON
 * object to it, such as creating new pages or modifying existing
 * ones. PDDocuments can be created anew or from an existing document.
 */
public class PDDocumentFactory {

    private PDDocument document;
    private String path;
    private static AssetManager ASSET_MANAGER = null;

    public static void init(Context context){
        if (ASSET_MANAGER == null) {
            ASSET_MANAGER = context.getApplicationContext().getAssets();
        }
    }

    private PDDocumentFactory(PDDocument document, ReadableMap documentActions) {
        this.path     = documentActions.getString("path");
        this.document = document;
    }

            /* ----- Factory methods ----- */
    public static PDDocument create(ReadableMap documentActions) throws NoSuchKeyException, IOException {
        PDDocument document = new PDDocument();
        PDDocumentFactory factory = new PDDocumentFactory(document, documentActions);

        factory.addPages(documentActions.getArray("pages"));
        return document;
    }

    public static PDDocument modify(ReadableMap documentActions) throws NoSuchKeyException, IOException {
        String path = documentActions.getString("path");

        InputStream pdfStream = ASSET_MANAGER.open(path);

        PDDocument document = PDDocument.load(pdfStream);
        PDDocumentFactory factory = new PDDocumentFactory(document, documentActions);

        factory.modifyPages(documentActions.getArray("modifyPages"));
        factory.addPages(documentActions.getArray("pages"));
        return document;
    }

            /* ----- Document actions (based on JSON structures sent over bridge) ----- */
    private void addPages(ReadableArray pages) throws IOException {
        for(int i = 0; i < pages.size(); i++) {
            PDPage page = PDPageFactory.create(document, pages.getMap(i));
            document.addPage(page);
        }
    }

    private void modifyPages(ReadableArray pages) throws IOException {
        for(int i = 0; i < pages.size(); i++) {
            PDPageFactory.modify(document, pages.getMap(i));
        }
    }

            /* ----- Static utilities ----- */
    public static String write(PDDocument document, String path) throws IOException {
        document.save(path);
        document.close();
        return path;
    }
}
