package me.moxun.dreamcatcher.connector.inspector.protocol.module;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;
import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsMethod;
import me.moxun.dreamcatcher.connector.json.ObjectMapper;
import me.moxun.dreamcatcher.connector.json.annotation.JsonProperty;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcPeer;
import me.moxun.dreamcatcher.connector.jsonrpc.JsonRpcResult;

/**
 * EXPERIMENTS
 * Created by moxun on 16/4/26.
 */
public class FileSystem implements ChromeDevtoolsDomain {

    private ObjectMapper mObjectMapper;
    private Context context;
    public static final String KEY_FS_ROOT = "FileSystem";
    private static boolean isFSEnabled = false;
    private List<String> textFile = new ArrayList<>();

    public static boolean isFSEnabled() {
        return isFSEnabled;
    }

    public FileSystem(Context context) {
        isFSEnabled = true;
        mObjectMapper = new ObjectMapper();
        this.context = context;

        textFile.add(".java");
        textFile.add(".txt");
        textFile.add(".html");
        textFile.add(".xml");
        textFile.add(".js");
        textFile.add(".json");
    }

    @ChromeDevtoolsMethod
    public void enable(JsonRpcPeer peer, JSONObject params) {

    }

    @ChromeDevtoolsMethod
    public void disable(JsonRpcPeer peer, JSONObject params) {

    }

    @ChromeDevtoolsMethod
    public JsonRpcResult requestFileSystemRoot(JsonRpcPeer peer, JSONObject params) {
        final RequestFileSystemRootRequest param = mObjectMapper.convertValue(params, RequestFileSystemRootRequest.class);
        if (param.origin.equals(KEY_FS_ROOT) && param.type.equals("persistent")) {
            return buildFileRoot();
        } else {
            return null;
        }
    }

    private RequestFileSystemRootResponse buildFileRoot() {
        RequestFileSystemRootResponse root = new RequestFileSystemRootResponse();
        root.errorCode = 0;
        Entry entry = new Entry();
        entry.name = "Data";
        entry.isDirectory = true;
        entry.url = context.getApplicationInfo().dataDir;
        root.root = entry;
        return root;
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult requestDirectoryContent(JsonRpcPeer peer, JSONObject params) {
        final RequestDirectoryContentRequest param = mObjectMapper.convertValue(params, RequestDirectoryContentRequest.class);
        File file = new File(param.url);
        if (file.exists() && file.isDirectory()) {
            final RequestDirectoryContentResponse response = new RequestDirectoryContentResponse();
            response.errorCode = 0;
            List<Entry> entries = new ArrayList<>();
            for (File f : file.listFiles()) {
                Entry entry = new Entry();
                entry.isDirectory = f.isDirectory();
                entry.url = f.getPath();
                entry.name = f.getName();
                entry.isTextFile = false;
                entry.resourceType = Page.ResourceType.OTHER;
                if (isImage(f)) {
                    entry.resourceType = Page.ResourceType.IMAGE;
                }
                if (isTextFile(f)) {
                    entry.resourceType = Page.ResourceType.DOCUMENT;
                    entry.isTextFile = isTextFile(f);
                }
                if (!entry.isDirectory) {
                    String[] strings = f.getName().split("\\.");
                    if (strings.length > 1) {
                        entry.mimeType = strings[strings.length - 1].toUpperCase();
                    } else {
                        entry.mimeType = "FILE";
                    }
                }
                entries.add(entry);
            }
            response.entries = entries;
            return response;
        }
        return null;
    }

    private boolean isTextFile(File file) {
        for (String s : textFile) {
            if (file.getName().toLowerCase().endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean isImage(File file) {
        String[] images = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        for (String s : images) {
            if (file.getName().toLowerCase().endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult requestMetadata(JsonRpcPeer peer, JSONObject params) {
        final RequestMetadataRequest param = mObjectMapper.convertValue(
                params,
                RequestMetadataRequest.class);
        File file = new File(param.url);
        if (file.exists()) {
            final RequestMetadataResponse response = new RequestMetadataResponse();
            response.errorCode = 0;
            Metadata meta = new Metadata();
            if (!file.isDirectory()) {
                meta.size = file.length();
            }
            meta.modificationTime = file.lastModified();
            response.metadata = meta;
            return response;
        } else {
            return null;
        }
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult requestFileContent(JsonRpcPeer peer, JSONObject params) {
        final RequestFileContentRequest param = mObjectMapper.convertValue(
                params,
                RequestFileContentRequest.class);
        final RequestFileContentResponse response = new RequestFileContentResponse();
        if (param.readAsText) {
            File file = new File(param.url);
            response.content = readFileAsText(file);
        }
        return response;
    }

    private String readFileAsText(File file) {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while ((s = br.readLine()) != null) {
                result = result + "\n" + s;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult deleteEntry(JsonRpcPeer peer, JSONObject params) {
        final DeleteEntryRequest param = mObjectMapper.convertValue(
                params,
                DeleteEntryRequest.class);
        final DeleteEntryResponse response = new DeleteEntryResponse();
        File file = new File(param.url);
        boolean success = file.delete();
        response.errorCode = success ? 0 : -1;
        return response;
    }

    public static class DeleteEntryRequest {
        @JsonProperty(required = true)
        public String url;
    }

    public static class DeleteEntryResponse implements JsonRpcResult {
        @JsonProperty(required = true)
        public int errorCode;
    }

    public static class RequestFileContentRequest {
        @JsonProperty(required = true)
        public String url;

        @JsonProperty(required = true)
        public boolean readAsText;

        @JsonProperty
        public Integer start;

        @JsonProperty
        public Integer end;

        @JsonProperty
        public String charset;
    }

    public static class RequestFileContentResponse implements JsonRpcResult {
        @JsonProperty(required = true)
        public int errorCode;

        @JsonProperty
        public String content;

        @JsonProperty
        public String charset;
    }

    public static class RequestMetadataRequest {
        @JsonProperty(required = true)
        public String url;
    }

    public static class RequestMetadataResponse implements JsonRpcResult {
        @JsonProperty(required = true)
        public int errorCode;

        @JsonProperty
        public Metadata metadata;
    }

    public static class Metadata {
        @JsonProperty(required = true)
        public double modificationTime;

        @JsonProperty(required = true)
        public double size;
    }

    public static class RequestDirectoryContentRequest {
        @JsonProperty(required = true)
        public String url;
    }

    public static class RequestDirectoryContentResponse implements JsonRpcResult {
        @JsonProperty(required = true)
        public int errorCode;

        @JsonProperty
        public List<Entry> entries;
    }

    public static class Entry {
        @JsonProperty(required = true)
        public String url;

        @JsonProperty(required = true)
        public String name;

        @JsonProperty(required = true)
        public boolean isDirectory;

        @JsonProperty
        public String mimeType;

        @JsonProperty
        public Page.ResourceType resourceType;

        @JsonProperty
        public Boolean isTextFile;
    }

    public static class RequestFileSystemRootRequest {
        @JsonProperty(required = true)
        public String origin;

        @JsonProperty(required = true)
        public String type;
    }

    public static class RequestFileSystemRootResponse implements JsonRpcResult {
        @JsonProperty(required = true)
        public int errorCode;

        @JsonProperty
        public Entry root;
    }
}
